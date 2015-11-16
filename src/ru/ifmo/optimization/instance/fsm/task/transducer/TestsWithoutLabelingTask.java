package ru.ifmo.optimization.instance.fsm.task.transducer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.comparator.MaxSingleObjectiveComparator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.SimpleFsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.TestsTask;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.util.StringUtils;

public class TestsWithoutLabelingTask extends TestsTask {

	public TestsWithoutLabelingTask(AbstractTaskConfig config) {
		super(config);
		comparator = new MaxSingleObjectiveComparator();
		
		Properties testsConfig = new Properties();
		try {
			testsConfig.load(new FileInputStream(new File("problem.properties")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		events = new ArrayList<String>();
		String eventArray[] = testsConfig.getProperty("events").split(" ");
		for (int i = 0; i < eventArray.length; i++) {
			events.add(eventArray[i]);
		}
		Collections.sort(events);
		actionsForEvolving = testsConfig.getProperty("actions").split(",");
		
		if (useTests) {
			readTests(testsConfig.getProperty("tests"));
			actionsForEvolving = testsConfig.getProperty("actions").split(",");
		} else if (useScenarios) {
			loadScenarios(testsConfig.getProperty("scenarios"));
			actionsForEvolving = new String[]{"1"};
		}
		outputs = new String[tests.length];
		
		
	}
	
	@Override
	protected FsmMetaData runFsmOnTest(FSM fsm, AutomatonTest test, boolean scenario) {
		int currentState = fsm.getInitialState();

		Set<Transition> visitedTransitions = new HashSet<Transition>();
		StringBuilder answers = new StringBuilder();
		for (int i = 0; i < test.getInput().length; i++) {
			int eventIndex = events.indexOf(test.getInput()[i]);
			String answer = fsm.transitions[currentState][eventIndex].getAction();
			int nextState = fsm.transitions[currentState][eventIndex].getEndState();
			if (nextState == -1) {
				break;
			}
			Transition tr = new Transition(currentState, nextState, test.getInput()[i], answer);
			visitedTransitions.add(tr);
			currentState = nextState;
			answers.append(answer);
		}
		String answer = answers.toString();

		double f = Math.max(test.getOutputString().length(), answer.length()) == 0 
					? 1.0
					: (double)StringUtils.levenshteinDistance(test.getOutputString(), answer) / Math.max(test.getOutputString().length(), answer.length());
		return new FsmMetaData(fsm, visitedTransitions, f);
	}
	
	@Override
	public FitInstance<FSM> getFitInstance(FSM fsm) {
		numberOfFitnessEvaluations++;
		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
		double f1 = 0;
		for (int i = 0; i < tests.length; i++) {
			FsmMetaData fsmRunData = runFsmOnTest(fsm, tests[i], false);
			f1 += 1.0 - fsmRunData.getFitness();
			visitedTransitions.addAll(fsmRunData.getVisitedTransitions());
		}
		fsm.clearUsedTransitions();
		fsm.markUsedTransitions(visitedTransitions);
		
		FitInstance<FSM> result = new SimpleFsmMetaData(fsm, 100.0 * (f1 / (double)tests.length), fsm.getUsedTransitions());
		return result;
	}
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM fsm) {
		numberOfFitnessEvaluations++;
		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
		double f1 = 0;
		for (int i = 0; i < tests.length; i++) {
			FsmMetaData fsmRunData = runFsmOnTest(fsm, tests[i], false);
			f1 += 1.0 - fsmRunData.getFitness();
			visitedTransitions.addAll(fsmRunData.getVisitedTransitions());
		}
		fsm.clearUsedTransitions();
		fsm.markUsedTransitions(visitedTransitions);
		InstanceMetaData<FSM> result =  new FsmMetaData(fsm, visitedTransitions, 
				100.0 * (f1 / (double)tests.length));
		return result;
	}
	
	@Override
	public String[] getActions() {
		return actionsForEvolving;
	}
	
	@Override
	public ArrayList<String> getEvents() {
		return events;
	}

	@Override
	public Comparator<Double> getComparator() {
		return comparator;
	}
	
	@Override
	public double correctFitness(double fitness, FSM cachedInstance, FSM trueInstance) {
		return 0;
	}
}
