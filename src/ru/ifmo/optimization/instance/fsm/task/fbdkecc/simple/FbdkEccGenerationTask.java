package ru.ifmo.optimization.instance.fsm.task.fbdkecc.simple;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import ru.ifmo.bool.ComplianceChecker;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.comparator.MaxSingleObjectiveComparator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.random.RandomProvider;

public class FbdkEccGenerationTask extends AbstractAutomatonTask {

	protected AutomatonTest[] tests;
	protected ArrayList<String> events;
	protected String[] outputs;
	protected String[] actionsForEvolving;
	protected String[] actions;
	
	public FbdkEccGenerationTask(AbstractTaskConfig config) {
		this.config = config;
		desiredFitness = config.getDesiredFitness();
		desiredNumberOfStates = Integer.parseInt(config.getProperty("desired-number-of-states"));
		comparator = new MaxSingleObjectiveComparator();
		readTests(config.getProperty("tests"));
	}
	
	protected void readTests(String filename) {
		Scanner in = null;
		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int numberOfTests = in.nextInt();
		in.nextLine();
		tests = new AutomatonTest[numberOfTests];
		
		Set<String> eventsSet = new HashSet<String>();
		Set<String> outputsSet = new HashSet<String>();
		
		for (int i = 0; i < numberOfTests; i++) {
			String[] input = in.nextLine().split(";");
			String[] output = in.nextLine().split(";");
			
			for (int j = 0; j < input.length; j++) {
				input[j] = input[j].trim();
				eventsSet.add(input[j]);
			}
			for (int j = 0; j < output.length; j++) {
				output[j] = output[j].trim();
				outputsSet.add(output[j]);
			}
			tests[i] = new AutomatonTest(input, output);
		}
		
		events = new ArrayList<String>();
		events.addAll(eventsSet);
		FSM.EVENTS = events;
		actions = outputsSet.toArray(new String[1]);
		actionsForEvolving = new String[]{"1"}; // for now, allow only one action at each state
		if (ComplianceChecker.getComplianceChecker() == null) {
			try {
				ComplianceChecker.createComplianceChecker(events);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public List<String> getEvents() {
		return events;
	}

	@Override
	public String[] getActions() {
		return actionsForEvolving;
	}

	@Override
	public AutomatonTaskConstraints getConstraints() {
		return new AutomatonTaskConstraints();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	public MooreMachine labelFSM(FSM fsm) {
		Map<String, Integer>[] table = new Map[fsm.getNumberOfStates()];
		
		for (int i = 0; i < fsm.getNumberOfStates(); i++) {
			table[i] = new HashMap<String, Integer>();
		}
		
		for (AutomatonTest test : tests) {
			int currentState = fsm.getInitialState();
			String[] outputs = test.getOutput();
			int outputIndex = 0;

			String output = outputs[outputIndex++];
			if (table[currentState].containsKey(output)) {
				table[currentState].put(output, table[currentState].get(output) + 1);
			} else {
				table[currentState].put(output, 1);
			}
			
			for (String event : test.getInput()) {
				currentState = fsm.getTransition(currentState, events.indexOf(event)).getEndState();
				if (currentState == -1) {
					break;
				}
				output = outputs[outputIndex++];
				if (table[currentState].containsKey(output)) {
					table[currentState].put(output, table[currentState].get(output) + 1);
				} else {
					table[currentState].put(output, 1);
				}
			}
		}
		
		String[] stateLabels = new String[fsm.getNumberOfStates()];
		for (int i = 0; i < fsm.getNumberOfStates(); i++) {
			Map<String, Integer> map = table[i];
			int max = Integer.MIN_VALUE;
			String action = null;
			for (Entry<String, Integer> e : map.entrySet()) {
				if (e.getValue() > max) {
					max = e.getValue();
					action = e.getKey();
				}
			}
			stateLabels[i] = action;
		}
		
		return new MooreMachine(fsm, stateLabels);
	}
	
	public FsmMetaData runFsmOnTest(MooreMachine m, int testId) {
		AutomatonTest test = tests[testId];
		Set<Transition> visitedTransitions = new HashSet<Transition>();
		List<String> answers = new ArrayList<String>();
		
		int state = m.getFSM().getInitialState();
		answers.add(m.getStateLabel(state));
		for (String event : test.getInput()) {
			Transition t = m.getFSM().getTransition(state, events.indexOf(event));
			visitedTransitions.add(t);
			state = t.getEndState();
			
			if (state == -1) {
				break;
			}
			
			answers.add(m.getStateLabel(state));
		}
		String answerArray[] = answers.toArray(new String[0]);

		double f = Math.max(test.getOutput().length, answerArray.length) == 0 
					? 1.0
					: test.getLevenshteinDistance(answerArray) / Math.max(test.getOutput().length, answerArray.length);
		return new FsmMetaData(m.getFSM(), visitedTransitions, f);
	}
	
	@Override
	public FitInstance<FSM> getFitInstance(FSM fsm) {
		InstanceMetaData<FSM> fsmMetaData = getInstanceMetaData(fsm);
		return new FitInstance<FSM>(fsmMetaData.getInstance(), fsmMetaData.getFitness());
	}
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM fsm) {
		numberOfFitnessEvaluations++;
		MooreMachine m = labelFSM(fsm);
		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
		double f1 = 0;
		for (int i = 0; i < tests.length; i++) {
			FsmMetaData fsmRunData = runFsmOnTest(m, i);
			if (fsmRunData == null) {
				continue;
			}
			f1 += 1.0 - fsmRunData.getFitness();
			visitedTransitions.addAll(fsmRunData.getVisitedTransitions());
		}
		
		fsm.markUsedTransitions(visitedTransitions);
		
		
		double fitness = f1 / (double)tests.length;
		FsmMetaData result = new FsmMetaData(fsm, visitedTransitions, fitness);
		result.setStateLabels(m.getStateLabels());
		return result;
	}

	@Override
	public double correctFitness(double fitness, FSM cachedInstance, FSM trueInstance) {
		return 0;
	}

	@Override
	public Comparator<Double> getComparator() {
		return null;
	}
	
	public static void main(String args[]) {
		RandomProvider.initialize(1, 1);
		RandomProvider.register();
		FbdkEccGenerationTask t = new FbdkEccGenerationTask(new AbstractTaskConfig("pnp.properties"));		
		InitialFSMGenerator ifg = new InitialFSMGenerator();
//		FSM fsm = ifg.createInstance(t);
//		System.out.println(t.getFitInstance(fsm, new FitnessValue(0)));
		System.out.println(ComplianceChecker.getComplianceChecker().checkCompliancy("REQ [!vac]", "REQ [vcHome & c1Home & c2Home]"));
	}
	

}
