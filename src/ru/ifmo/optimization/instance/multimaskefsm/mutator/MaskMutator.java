package ru.ifmo.optimization.instance.multimaskefsm.mutator;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.MaskMutation;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.MultiMaskEfsmMutation;
import ru.ifmo.random.RandomProvider;

public class MaskMutator implements Mutator<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> {
	
	private double probability;
	
	public MaskMutator(double probability) {
		this.probability = probability;
	}
	
	public double probability() {
		return probability;
	}

	@Override
	public MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> apply(
			MultiMaskEfsmSkeleton individual) {
		MultiMaskEfsmSkeleton mutated = new MultiMaskEfsmSkeleton(individual);
		int state = RandomProvider.getInstance().nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
		int eventId = RandomProvider.getInstance().nextInt(MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT);
		int transitionGroup = RandomProvider.getInstance().nextInt(MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT);
		
		int meaningfulPredicatesCount = individual.getState(state).
				getTransitionGroup(eventId, transitionGroup).getMeaningfulPredicatesCount();
		
		int setToFalsePredicateId = mutated.getState(state).getTransitionGroup(eventId, transitionGroup).
				getMeaningfulPredicateIds().get(RandomProvider.getInstance().nextInt(meaningfulPredicatesCount));
		
		int setToTruePredicateId = mutated.getState(state).getTransitionGroup(eventId, transitionGroup).
					getUnmeaningfulPredicateIds().get(RandomProvider.getInstance().nextInt(
					MultiMaskEfsmSkeleton.PREDICATE_COUNT - meaningfulPredicatesCount));
		
		MultiMaskEfsmMutation mutation = new MaskMutation(state, eventId, transitionGroup, setToFalsePredicateId, setToTruePredicateId);
		mutation.apply(mutated);
		
		return new MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation>(mutated,
				new MutationCollection<MultiMaskEfsmMutation>(mutation));
	}

	@Override
	public MultiMaskEfsmSkeleton applySimple(MultiMaskEfsmSkeleton individual) {
		return apply(individual).getInstance();
	}

	
	@Override
	public void setProbability(double probability) {
		this.probability = probability;
	}
}
