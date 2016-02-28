package ru.ifmo.optimization.instance.multimaskefsm.mutator;

import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.DestinationStateMutation;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.MultiMaskEfsmMutation;

public class DestinationStateMutator implements Mutator<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> {
	
	private double probability;
	
	public DestinationStateMutator(double probability) {
		this.probability = probability;
	}
	
	public double probability() {
		return probability;
	}

	@Override
	public MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> apply(
			MultiMaskEfsmSkeleton individual) {
		MultiMaskEfsmSkeleton mutated = new MultiMaskEfsmSkeleton(individual);

		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		if (individual.getDefinedTransitionsCount() == 0) {
			return new MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation>(
					mutated, new MutationCollection<MultiMaskEfsmMutation>());
		}
		
		int state = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
		int transitionGroup = random.nextInt(MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT);
		int eventId = random.nextInt(MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT);
		int inputVarsId = random.nextInt(individual.getState(state).getTransitionGroup(eventId, transitionGroup).getTransitionsCount());
		
		while (individual.getState(state).getTransitionGroup(eventId, transitionGroup).getNewState(inputVarsId) == -1) {
			state = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
			eventId = random.nextInt(MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT);
			transitionGroup = random.nextInt(MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT);
			inputVarsId = random.nextInt(individual.getState(state).getTransitionGroup(eventId, transitionGroup).getTransitionsCount());
		}
		
		int newState = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
		while (newState == mutated.getState(state).getTransitionGroup(eventId, transitionGroup).getNewState(inputVarsId)) {
			newState = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
		}
		MultiMaskEfsmMutation mutation = new DestinationStateMutation(state, eventId, transitionGroup, inputVarsId, newState);
		mutation.apply(mutated);
		return new MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation>(
				mutated, new MutationCollection<MultiMaskEfsmMutation>(mutation));
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
