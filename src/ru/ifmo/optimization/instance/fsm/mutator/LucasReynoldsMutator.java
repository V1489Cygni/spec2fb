package ru.ifmo.optimization.instance.fsm.mutator;

import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;

public class LucasReynoldsMutator implements Mutator<FSM, FsmMutation> {
	private String[] actions;
	private double mutationProbability;
	
	public LucasReynoldsMutator(String[] actions, AutomatonTaskConstraints constraints, AbstractAutomatonTask task) {
		this.actions = actions;
		this.mutationProbability = 2.0 / ((double)task.getDesiredNumberOfStates() * task.getEvents().size());
	}

	private MutatedInstanceMetaData<FSM, FsmMutation> mutateTransitionFunction(FSM individual) {
		MutationCollection<FsmMutation> mutations = new MutationCollection<FsmMutation>();
		FSM mutated = new FSM(individual);
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		//make at least one mutation
		int st = random.nextInt(individual.getNumberOfStates());
		int ev = random.nextInt(individual.getNumberOfEvents());
		int newSt = random.nextInt(individual.getNumberOfStates());
		
		//make sure we changed something
		while (newSt == mutated.transitions[st][ev].getEndState()) {
			newSt = random.nextInt(individual.getNumberOfStates());
		}
		mutations.add(new FsmTransitionMutation(st, ev, newSt, mutated.getTransition(st, ev).getAction()));
		mutated.transitions[st][ev].setEndState(newSt);
		
		
		//mutate other transitions with a small probability
		for (int state = 0; state < individual.getNumberOfStates(); state++) {
			for (int event = 0; event < individual.getNumberOfEvents(); event++) {
				if (random.nextDouble() >= mutationProbability) {
					continue;
				}
				if (state == st && event == ev) {
					continue;
				}
				
				int newState = random.nextInt(individual.getNumberOfStates());
				//make sure we changed something
				while (true) {
					if (newState == mutated.transitions[state][event].getEndState()) {
						newState = random.nextInt(individual.getNumberOfStates());
						continue;
					}
					break;
				}
				mutations.add(new FsmTransitionMutation(state, event, newState, mutated
						.getTransition(state, event).getAction()));
				mutated.transitions[state][event].setEndState(newState);
			}
		}
		return new MutatedInstanceMetaData<FSM, FsmMutation>(mutated, mutations);
	}
	
	private MutatedInstanceMetaData<FSM, FsmMutation> mutateActionsFunction(FSM individual) {
		MutationCollection<FsmMutation> mutations = new MutationCollection<FsmMutation>();
		FSM mutated = new FSM(individual);
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		int st = random.nextInt(individual.getNumberOfStates());
		int ev = random.nextInt(individual.getNumberOfEvents());
		
		String newAct = actions[random.nextInt(actions.length)];
		//make sure we changed something
		while (true) {
			if (newAct.equals(mutated.transitions[st][ev].getAction())) {
				newAct = actions[random.nextInt(actions.length)];
				continue;
			}			
			break;
		}
		mutations.add(new FsmTransitionMutation(st, ev, 
				mutated.transitions[st][ev].getEndState(), newAct));
		mutated.transitions[st][ev].setAction(newAct);
		
		for (int state = 0; state < individual.getNumberOfStates(); state++) {
			for (int event = 0; event < individual.getNumberOfEvents(); event++) {
				if (random.nextDouble() >= mutationProbability) {
					continue;
				}
				
				if (state == st && event == ev) {
					continue;
				}
				
				String newAction = actions[random.nextInt(actions.length)];
				//make sure we changed something
				while (true) {
					if (newAction.equals(mutated.transitions[state][event].getAction())) {
						newAction = actions[random.nextInt(actions.length)];
						continue;
					}
					break;
				}
				mutations.add(new FsmTransitionMutation(state, event,
						mutated.transitions[state][event].getEndState(),
						newAction));
				mutated.transitions[state][event].setAction(newAction);
			}
		}
		return new MutatedInstanceMetaData<FSM, FsmMutation>(mutated, mutations);
	}
	
	@Override
	public MutatedInstanceMetaData<FSM, FsmMutation> apply(FSM individual) {
		//if there is only one action in the actions set there is no point in changing it
		if (actions.length == 1) {
			return mutateTransitionFunction(individual);
		} 
		if (ThreadLocalRandom.current().nextBoolean()) {
			return mutateTransitionFunction(individual);
		}
		return mutateActionsFunction(individual);
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
