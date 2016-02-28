package ru.ifmo.optimization.instance.fsm.mutator.efsm;

import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;
import ru.ifmo.util.Util;

public class ChangeNumberOfActionsMutator implements Mutator<FSM, FsmMutation> {
	private String[] actions;
	
	public ChangeNumberOfActionsMutator(String[] actions) {
		this.actions = actions;
	}
	
	@Override
	public MutatedInstanceMetaData<FSM, FsmMutation> apply(FSM individual) {
		FSM mutated = new FSM(individual);
		
		if (!Util.hasTransitions(mutated)) {
			return null;
		}
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		int state = random.nextInt(mutated.getNumberOfStates());
		while (Util.numberOfExistingTransitions(mutated.transitions[state]) == 0) {
			state = random.nextInt(mutated.getNumberOfStates());
		}
		
		int event = random.nextInt(mutated.getNumberOfEvents());
		while (mutated.transitions[state][event].getEndState() == -1) {
			event = random.nextInt(mutated.getNumberOfEvents());
		}
		
		int outputSize = Integer.parseInt(mutated.transitions[state][event].getAction());
		int newOutputSize;
		if (random.nextBoolean()) {
			newOutputSize = Math.min(outputSize + 1, actions.length);
		} else {
			newOutputSize = Math.max(0, outputSize - 1);
		}
	
		String newAction = newOutputSize + "";
		mutated.transitions[state][event].setAction(newAction);
		FsmTransitionMutation mutation = new FsmTransitionMutation(state, event, mutated.transitions[state][event].getEndState(), newAction, false);
		return new MutatedInstanceMetaData<FSM, FsmMutation>(
				mutated, new MutationCollection<FsmMutation>(mutation));
	}
	
	@Override
	public FSM applySimple(FSM individual) {
		return (FSM) apply(individual).getInstance();
	}
	
	private double probability;
	
	public double probability() {
		return probability;
	}
	
	@Override
	public void setProbability(double probability) {
		this.probability = probability;
	}
}
