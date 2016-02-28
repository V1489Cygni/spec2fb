package ru.ifmo.optimization.instance.multimaskefsm.mutator;

import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.ChangeMeaningfulPredicatesMutation;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.MultiMaskEfsmMutation;

public class ChangeMeaningfulPredicatesMutator implements Mutator<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> {

	private double probability;
	
	public ChangeMeaningfulPredicatesMutator(double probability) {
		this.probability = probability;
	}
	
	@Override
	public MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> apply(
			MultiMaskEfsmSkeleton individual) {
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		MultiMaskEfsmSkeleton mutated = new MultiMaskEfsmSkeleton(individual);
		int state = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
		int eventId = random.nextInt(MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT);
		int transitionGroup = random.nextInt(MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT);
		int meaningfulPredicatesCount = mutated.getState(state).getTransitionGroup(eventId, transitionGroup).getMeaningfulPredicatesCount();
		MultiMaskEfsmMutation mutation = null;
		
		if (meaningfulPredicatesCount == 1) {
			mutation = addPredicate(mutated, state, eventId, transitionGroup);
		} else if (meaningfulPredicatesCount == MultiMaskEfsmSkeleton.MEANINGFUL_PREDICATES_COUNT) {
			mutation = deletePredicate(mutated, state, eventId, transitionGroup);
		} else {
			if (random.nextBoolean()) {
				mutation = addPredicate(mutated, state, eventId, transitionGroup);
			} else {
				mutation = deletePredicate(mutated, state, eventId, transitionGroup);
			}
		}
		
		return new MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation>(
				mutated, new MutationCollection<MultiMaskEfsmMutation>(mutation)); 
	}
	
	private MultiMaskEfsmMutation deletePredicate(MultiMaskEfsmSkeleton mutated, int state, int eventId, int transitionGroup) {
		int predicateToDelete = mutated.getState(state).getTransitionGroup(eventId, transitionGroup).getMeaningfulPredicateIds().get(
				ThreadLocalRandom.current().nextInt(mutated.getState(state).
				getTransitionGroup(eventId, transitionGroup).getMeaningfulPredicatesCount()));
		mutated.getState(state).getTransitionGroup(eventId, transitionGroup).removePredicate(predicateToDelete);
		return new ChangeMeaningfulPredicatesMutation(state, eventId, transitionGroup, false, predicateToDelete);
	}
	
	private MultiMaskEfsmMutation addPredicate(MultiMaskEfsmSkeleton mutated, int state, int eventId, int transitionGroup) {
		int predicateToAdd = mutated.getState(state).getTransitionGroup(eventId, transitionGroup).getUnmeaningfulPredicateIds().get(
				ThreadLocalRandom.current().nextInt(
				mutated.getState(state).getTransitionGroup(eventId, transitionGroup).getUnmeaningfulPredicatesCount()));
		mutated.getState(state).getTransitionGroup(eventId, transitionGroup).addPredicate(predicateToAdd);
		return new ChangeMeaningfulPredicatesMutation(state, eventId, transitionGroup, true, predicateToAdd);
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
