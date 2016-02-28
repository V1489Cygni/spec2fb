package ru.ifmo.optimization.instance.multimaskefsm.mutator;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.OutputAction;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.MultiMaskEfsmMutation;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.SetFixedActionIdMutation;
import ru.ifmo.optimization.instance.multimaskefsm.task.ExactEccGenerator;

public class SetFixedActionIdMutator implements Mutator<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> {

	private double probability;
	private int precalculatedActionsCount;
	
	public SetFixedActionIdMutator(double probability) {
		this.probability = probability;
		ExactEccGenerator generator = new ExactEccGenerator();
        List<OutputAction> precalculatedActions = generator.calculateActions();
        precalculatedActionsCount = precalculatedActions.size();
	}
	
	@Override
	public MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> apply(
			MultiMaskEfsmSkeleton individual) {
		MultiMaskEfsmSkeleton mutated = new MultiMaskEfsmSkeleton(individual);
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		int state = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
		int actionNumber = random.nextInt(mutated.getState(state).getNumberOfOutputActions());
		
		int fixedActionId = -1;
		if (mutated.getState(state).getFixedActionId(actionNumber) == -1) {
			fixedActionId = random.nextInt(precalculatedActionsCount);
		} else {
			fixedActionId = random.nextBoolean() ? random.nextInt(precalculatedActionsCount) : -1;	
		}
		
		SetFixedActionIdMutation mutation = new SetFixedActionIdMutation(state, -1, -1, actionNumber, fixedActionId);
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
