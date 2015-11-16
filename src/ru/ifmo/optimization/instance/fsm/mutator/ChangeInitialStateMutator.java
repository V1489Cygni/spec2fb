package ru.ifmo.optimization.instance.fsm.mutator;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmInitialStateMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.random.RandomProvider;

public class ChangeInitialStateMutator implements Mutator<FSM, FsmMutation> {

	private double probability;
	
	public double probability() {
		return probability;
	}
	
	@Override
	public MutatedInstanceMetaData<FSM, FsmMutation> apply(FSM instance) {
		int newInitialState = RandomProvider.getInstance().nextInt(instance.getNumberOfStates());
		while (newInitialState == instance.getInitialState()) {
			newInitialState = RandomProvider.getInstance().nextInt(instance.getNumberOfStates());
		}
		FsmInitialStateMutation mutation = new FsmInitialStateMutation(newInitialState);
		return new MutatedInstanceMetaData<FSM, FsmMutation>(instance, new MutationCollection<FsmMutation>(mutation));
	}

	@Override
	public FSM applySimple(FSM individual) {
		return apply(individual).getInstance();
	}
	
	@Override
	public void setProbability(double probability) {
		this.probability = probability;
	}
}
