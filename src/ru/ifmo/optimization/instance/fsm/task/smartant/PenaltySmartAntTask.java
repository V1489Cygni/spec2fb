package ru.ifmo.optimization.instance.fsm.task.smartant;

import java.util.HashSet;
import java.util.Set;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.util.Pair;

public class PenaltySmartAntTask extends SmartAntTask {

	public PenaltySmartAntTask(AbstractTaskConfig config) {
		super(config);
	}
	
	@Override
	public FitInstance<FSM> getFitInstance(FSM automaton) {
		numberOfFitnessEvaluations++;
		antTask.reset();
		Set<Integer> visitedStates = new HashSet<Integer>();
		int numberOfEatenApples = 0;
		int numberOfSteps = 0;
		FSM dfa = new FSM(automaton);
		currentState = automaton.getInitialState();
		visitedStates.add(currentState);
		for (; numberOfSteps < maxNumberOfSteps; numberOfSteps++) {
			numberOfEatenApples += makeSimpleMove(automaton);
			visitedStates.add(currentState);
			if (numberOfEatenApples == SmartAnt.NUMBER_OF_APPLES) {
				break;
			}
		}
		double fitness = 1.0 / (numberOfSteps - 2.0 * numberOfEatenApples);
		
		//FIXME make not null
		return null;
//		return new SimpleFsmMetaData(dfa, fitness);
	}
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM automaton) {
		numberOfFitnessEvaluations++;
		antTask.reset();
		int numberOfEatenApples = 0;
		int numberOfSteps = 0;
		FSM dfa = new FSM(automaton);
		Set<Transition> visitedTransitions = new HashSet<Transition>();
		Set<Integer> visitedStates = new HashSet<Integer>();
		currentState = automaton.getInitialState();
		for (int i = 0; i < maxNumberOfSteps; i++) {
			Pair<Integer, Transition> p = makeMove(automaton);
			visitedTransitions.add(p.second);
			numberOfEatenApples += p.first;
			numberOfSteps = i;
			visitedStates.add(currentState);
			if (numberOfEatenApples == SmartAnt.NUMBER_OF_APPLES) {
				break;
			}
		}
		double fitness = 1.0 / (numberOfSteps - 2.0 * numberOfEatenApples);
		return new FsmMetaData(dfa, visitedTransitions, fitness);
	}
	
	
}
