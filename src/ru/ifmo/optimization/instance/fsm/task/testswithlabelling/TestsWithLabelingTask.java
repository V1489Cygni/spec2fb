package ru.ifmo.optimization.instance.fsm.task.testswithlabelling;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ifmo.bool.ComplianceChecker;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class TestsWithLabelingTask extends TestsTask {
	
	public TestsWithLabelingTask(AbstractTaskConfig config) {
		super(config);
		
		events = new ArrayList<String>();
		String eventArray[] = testsProperties.getProperty("events").split(" ");
		for (int i = 0; i < eventArray.length; i++) {
			events.add(eventArray[i]);
		}
		Collections.sort(events);
		
		actions = testsProperties.getProperty("actions").split(",");
		
		if (useTests) {
			readTests(testsProperties.getProperty("tests"));
			int minNumberOfOutputActions = Integer.parseInt(testsProperties.getProperty("min-number-of-actions"));
			int maxNumberOfOutputActions = Integer.parseInt(testsProperties.getProperty("max-number-of-actions"));
			actionsForEvolving = new String[maxNumberOfOutputActions - minNumberOfOutputActions + 1];
			for (int i = 0; i < actionsForEvolving.length; i++) {
				actionsForEvolving[i] = "" + (i + minNumberOfOutputActions);
			}
		} else if (useScenarios) {
			loadScenarios(testsProperties.getProperty("scenarios"));
			actionsForEvolving = new String[]{"1"};
		}
		outputs = new String[tests.length];
		
		if (ComplianceChecker.getComplianceChecker() == null) {
			try {
				ComplianceChecker.createComplianceChecker(events);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	
	protected FSM labelFSM(FSM fsm, boolean scenarios) {
		FSM result = new FSM(fsm);
		LabellingTable table = new LabellingTable(fsm.getNumberOfStates(), fsm.getNumberOfEvents());
		for (int i = 0; i < tests.length; i++) {
			editLabellingTable(fsm, tests[i], table, scenarios);
		}
		
		for (int i = 0; i < fsm.getNumberOfStates(); i++) {
			for (int j = 0; j < fsm.getNumberOfEvents(); j++) {
				result.transitions[i][j].setAction(table.getActions(i, j));
			}
		}
		return result;
	}
	
	private void editLabellingTable(FSM fsm, AutomatonTest test, LabellingTable table, boolean scenarios) {
		int currentState = fsm.getInitialState();
		int counter = 0;
		int i = 0; 
		for (String event : test.getInput()) {
			int eventIndex = events.indexOf(event);
			
			Transition t = fsm.transitions[currentState][eventIndex];
			if (t == null) {
				return;
			}
			if (t.getEndState() == -1) {
				return;
			}
			
			if (scenarios) {
				table.add(currentState, eventIndex, test.getOutput()[i]);
			} else {
				StringBuilder sequence = new StringBuilder();
				int numberOfActions = Integer.parseInt(t.getAction());
				for (int j = 0; j < numberOfActions; j++) {
					if (counter < test.getOutput().length) {
						sequence.append(test.getOutput()[counter++]);
					} else {
						sequence.append("??");
					}
				}
				table.add(currentState, eventIndex, sequence.toString());
			}
			currentState = t.getEndState();
			i++;
		}
	}

	@Override
	public FitInstance<FSM> getFitInstance(FSM fsm) {
		InstanceMetaData<FSM> fsmMetaData = getInstanceMetaData(fsm);
		return new FitInstance<FSM>(fsmMetaData.getInstance(), fsmMetaData.getFitness());
	}
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM fsm) {
		numberOfFitnessEvaluations++;
		FSM labelled = labelFSM(fsm, false);
		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
		double f1 = 0;
		int numberOfSuccesses = 0; 
		for (int i = 0; i < tests.length; i++) {
			FsmMetaData fsmRunData = runFsmOnTest(labelled, tests[i], false);
			if (fsmRunData == null) {
				continue;
			}
			if (fsmRunData.getFitness() < 1e-5) {
				numberOfSuccesses++;
			}
			f1 += 1.0 - fsmRunData.getFitness();
			visitedTransitions.addAll(fsmRunData.getVisitedTransitions());
		}
		
		int numberOfIncompliantTransitions = 0;
		fsm.markUsedTransitions(visitedTransitions);
		
		double incompliantTransitionsRatio = visitedTransitions.isEmpty() ? 0 : (double)numberOfIncompliantTransitions / (double)visitedTransitions.size();
		
		if (numberOfSuccesses == tests.length) {
			double fitness = (20.0 + 0.01 * (100.0 - visitedTransitions.size())) * (1.0 - incompliantTransitionsRatio);
			return new FsmMetaData(fsm, visitedTransitions, fitness);
		}
		double fitness = (10.0 * (f1 / (double)tests.length) + 0.01 * (100.0 - visitedTransitions.size())) * (1.0 - incompliantTransitionsRatio);
		return new FsmMetaData(fsm, visitedTransitions, fitness);
	}
	
	@Override
	public void reset() {
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
