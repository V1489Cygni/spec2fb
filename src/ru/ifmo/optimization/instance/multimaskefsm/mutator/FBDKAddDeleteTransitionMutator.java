package ru.ifmo.optimization.instance.multimaskefsm.mutator;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.TransitionGroup;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.DestinationStateMutation;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.MultiMaskEfsmMutation;

public class FBDKAddDeleteTransitionMutator implements Mutator<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> {
	
	private double probability;
	
	public FBDKAddDeleteTransitionMutator(double probability, double mutationProbability) {
		this.probability = probability;
		this.mutationProbability = mutationProbability;
	}
	
	public double probability() {
		return probability;
	}

	private double mutationProbability;
	
	@Override
	public MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> apply(
			MultiMaskEfsmSkeleton individual) {
		
		MultiMaskEfsmSkeleton mutated = new MultiMaskEfsmSkeleton(individual);
		MutationCollection<MultiMaskEfsmMutation> mutations = new MutationCollection<MultiMaskEfsmMutation>();
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		for (int stateId = 0; stateId < MultiMaskEfsmSkeleton.STATE_COUNT; stateId++) {
			if (random.nextDouble() >= mutationProbability) {
				continue;
			}
			int eventId = random.nextInt(MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT);
			int transitionGroupId = random.nextInt(MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT);
			TransitionGroup tg = individual.getState(stateId).getTransitionGroup(eventId, transitionGroupId);
			if (random.nextDouble() < 0.1 && tg.hasUndefinedTransitions()) {
				//add transition
				List<Integer> undefinedTransitionIds = tg.getUndefinedTransitionIds();
				int transitionToAdd = undefinedTransitionIds.get(random.nextInt(undefinedTransitionIds.size()));
				int newState = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
				
				MultiMaskEfsmMutation mutation = new DestinationStateMutation(stateId, eventId, transitionGroupId, transitionToAdd, newState);
				mutations.add(mutation);
				mutation.apply(mutated);
			} else if (tg.hasTransitions()){
				//delete transition
				List<Integer> definedTransitionIds = tg.getDefinedTransitionIds();
				int transitionToDelete = definedTransitionIds.get(random.nextInt(definedTransitionIds.size()));
				MultiMaskEfsmMutation mutation = new DestinationStateMutation(stateId, eventId, transitionGroupId, transitionToDelete, -1);
				mutations.add(mutation);
				mutation.apply(mutated);
			}
		}
		
		return new MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation>(mutated, mutations);
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
