package ru.ifmo.optimization.instance.fsm.mutator;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.random.RandomProvider;

public class MultipleFinalStateMutator implements Mutator<FSM, FsmMutation>{

	private double mutationProbability;
	
	public MultipleFinalStateMutator(AbstractAutomatonTask task) {
		this.mutationProbability = 1.0 / ((double)task.getDesiredNumberOfStates() * task.getEvents().size());
	}

	@Override
	public MutatedInstanceMetaData<FSM, FsmMutation> apply(FSM individual) {
		MutationCollection<FsmMutation> mutations = new MutationCollection<FsmMutation>();
		FSM mutated = new FSM(individual);
		
		for (int state = 0; state < individual.getNumberOfStates(); state++) {
			for (int event = 0; event < individual.getNumberOfEvents(); event++) {
				if (RandomProvider.getInstance().nextDouble() > mutationProbability) {
					continue;
				}
				int newState = RandomProvider.getInstance().nextInt(individual.getNumberOfStates());
				if (newState == mutated.transitions[state][event].getEndState()) {
					continue;
				}
				mutations.add(new FsmTransitionMutation(state, event, newState, mutated
						.getTransition(state, event).getAction(), false));
				mutated.transitions[state][event].setEndState(newState);
			}
		}
		return new MutatedInstanceMetaData<FSM, FsmMutation>(mutated, mutations);
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
