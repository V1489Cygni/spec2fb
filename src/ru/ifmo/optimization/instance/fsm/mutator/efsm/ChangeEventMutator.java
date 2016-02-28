package ru.ifmo.optimization.instance.fsm.mutator.efsm;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;
import ru.ifmo.util.Util;

public class ChangeEventMutator implements Mutator<FSM, FsmMutation> {

	private List<String> events;
	
	public ChangeEventMutator(List<String> events) {
		this.events = events;
	}
	
	@Override
	public MutatedInstanceMetaData<FSM, FsmMutation> apply(FSM individual) {
		FSM mutated = new FSM(individual);
		
		if (!Util.hasTransitions(mutated)) {
			return null;
		}
		
		MutationCollection<FsmMutation> mutations = new MutationCollection<FsmMutation>();
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		int state = random.nextInt(mutated.getNumberOfStates());
		while (Util.numberOfExistingTransitions(mutated.transitions[state]) == 0) {
			state = random.nextInt(mutated.getNumberOfStates());
		}
		
		int event = random.nextInt(mutated.getNumberOfEvents());
		while (mutated.transitions[state][event].getEndState() == -1) {
			event = random.nextInt(mutated.getNumberOfEvents());
		}
		
		Transition transition = mutated.getTransition(state, event);
		
		int newInputId = random.nextInt(events.size());
		while (newInputId == event) {
			newInputId = random.nextInt(events.size());
		}
		
		mutated.transitions[state][newInputId].setEndState(transition.getEndState());
		mutated.transitions[state][newInputId].setAction(transition.getAction());

		mutated.transitions[state][event].setEndState(-1);
		mutated.transitions[state][event].setAction("");

		mutations.add(new FsmTransitionMutation(state, event, -1, ""));
		mutations.add(new FsmTransitionMutation(state, newInputId, 
				mutated.transitions[state][newInputId].getEndState(),
				mutated.transitions[state][newInputId].getAction()));

		return Util.makeCompliantFSM(new MutatedInstanceMetaData<FSM, FsmMutation>(mutated, mutations));
	}

	@Override
	public FSM applySimple(FSM individual) {
		return apply(individual).getInstance();
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
