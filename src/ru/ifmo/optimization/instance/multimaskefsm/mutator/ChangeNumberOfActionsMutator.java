package ru.ifmo.optimization.instance.multimaskefsm.mutator;

import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.ChangeNumberOfActionsMutation;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.MultiMaskEfsmMutation;

public class ChangeNumberOfActionsMutator implements Mutator<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> {

	private double probability;
	
	public ChangeNumberOfActionsMutator(double probability) {
		this.probability = probability;
	}
	
	@Override
	public MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> apply(
			MultiMaskEfsmSkeleton individual) {
		MultiMaskEfsmSkeleton mutated = new MultiMaskEfsmSkeleton(individual);
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		int state = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
		
		int oldValue = individual.getState(state).getNumberOfOutputActions();
		int newValue = -1;
		if (oldValue > 1) {
			if (oldValue < MultiMaskEfsmSkeleton.MAX_OUTPUT_ACTION_COUNT) {
				newValue = random.nextBoolean() ? oldValue + 1 : oldValue - 1;
			} else {
				newValue = oldValue - 1;
			}
		} else {
			newValue = oldValue + 1;
		}
		
		MultiMaskEfsmMutation mutation = new ChangeNumberOfActionsMutation(state, -1, -1, newValue);
		mutation.apply(mutated);
		
		return new MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation>(mutated,
				new MutationCollection<MultiMaskEfsmMutation>(mutation));
	}

	@Override
	public MultiMaskEfsmSkeleton applySimple(MultiMaskEfsmSkeleton individual) {
		return apply(individual).getInstance();
	}

	@Override
	public double probability() {
		return probability;
	}

	@Override
	public void setProbability(double probability) {
		this.probability = probability;
	}

}
