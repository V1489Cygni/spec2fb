package ru.ifmo.optimization.instance.fsm.task.testswithlabelling.errors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.TestsModelCheckingTask;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class TestsWithErrorsTask extends TestsModelCheckingTask {
	
	private int numberOfErrorsInTests;
	
	private class ExtendedFsmMetaData extends FsmMetaData {
		private int numberOfErrors;
		
		public ExtendedFsmMetaData(FSM fsm,
				Collection<Transition> visitedTransitions, double fitness, int numberOfErrors) {
			super(fsm, visitedTransitions, fitness);
			this.numberOfErrors = numberOfErrors;
		}
		
		public int getNumberOfErrors() {
			return numberOfErrors;
		}
	}

	public TestsWithErrorsTask(AbstractTaskConfig config) {
		super(config);
		numberOfErrorsInTests = Integer.parseInt(config.getProperty("number-of-errors"));
	}
	
	protected ExtendedFsmMetaData runFsmOnTest(FSM fsm, int testIndex, boolean scenario) {
		int currentState = fsm.getInitialState();

		Set<Transition> visitedTransitions = new HashSet<Transition>();
		List<String> answers = new ArrayList<String>();
		int numberOfErrors = 0;
		for (int i = 0; i < tests[testIndex].getInput().length; i++) {
			int eventIndex = events.indexOf(tests[testIndex].getInput()[i]);
			String answer = fsm.transitions[currentState][eventIndex].getAction();
			int nextState = fsm.transitions[currentState][eventIndex].getEndState();
			if (nextState == -1) {
				numberOfErrors += tests[testIndex].getOutput().length - i + 1;
				break;
			}
			
			Transition tr = new Transition(currentState, nextState, tests[testIndex].getInput()[i], answer);
			visitedTransitions.add(tr);
			currentState = nextState;
			
			answers.add(answer);
			if (!answer.equals(tests[testIndex].getOutput()[i])) {
				numberOfErrors++;
			}
		}
		String answerArray[] = answers.toArray(new String[0]);

		double f = Math.max(tests[testIndex].getOutput().length, answerArray.length) == 0 
					? 1.0
					: tests[testIndex].getLevenshteinDistance(answerArray) / Math.max(tests[testIndex].getOutput().length, answerArray.length);
		return new ExtendedFsmMetaData(fsm, visitedTransitions, f, numberOfErrors);
	}
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM fsm) {
		numberOfFitnessEvaluations++;
		FSM labelled = labelFSM(fsm, false);
		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
		double f1 = 0;
		int numberOfErrors = 0;
		for (int i = 0; i < tests.length; i++) {
			ExtendedFsmMetaData fsmRunData = runFsmOnTest(labelled, i, true);
			if (fsmRunData == null) {
				continue;
			}
			f1 += 1.0 - fsmRunData.getFitness();
			visitedTransitions.addAll(fsmRunData.getVisitedTransitions());
			numberOfErrors += fsmRunData.getNumberOfErrors();
		}
		
		fsm.markUsedTransitions(visitedTransitions);	
		
		double fitness = f1 / (double)tests.length;
		
		if (numberOfErrors <= numberOfErrorsInTests) {
			OptimizationAlgorithmCutoff.getInstance().terminateNow();
		}
		return new FsmMetaData(fsm, visitedTransitions, fitness);
	}
}
