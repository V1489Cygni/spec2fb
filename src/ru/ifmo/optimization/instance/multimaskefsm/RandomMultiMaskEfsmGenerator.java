package ru.ifmo.optimization.instance.multimaskefsm;

import java.util.HashSet;
import java.util.Set;

import ru.ifmo.optimization.instance.InstanceGenerator;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.random.RandomProvider;

public class RandomMultiMaskEfsmGenerator implements InstanceGenerator {

	@Override
	public MultiMaskEfsmSkeleton createInstance(AbstractOptimizationTask task) {
		return createInstance();
	}

	public MultiMaskEfsmSkeleton createInstance() {
		State[] states = new State[MultiMaskEfsmSkeleton.STATE_COUNT];
		for (int i = 0; i < states.length; i++) {
			states[i] = new State();
			
			int[] meaningfulPredicateCount = new int[MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT];
			for (int j = 0; j < meaningfulPredicateCount.length; j++) {
				meaningfulPredicateCount[j] = Math.max(1, RandomProvider.getInstance().nextInt(MultiMaskEfsmSkeleton.MEANINGFUL_PREDICATES_COUNT + 1));
			}
			
			for (String inputEvent : MultiMaskEfsmSkeleton.INPUT_EVENTS.keySet()) {
				for (int j = 0; j < MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT; j++) {
					TransitionGroup tg = new TransitionGroup(meaningfulPredicateCount[j]); 

					//set random mask
					Set<Integer> meaningfulPredicates = new HashSet<Integer>();


					while (meaningfulPredicates.size() < meaningfulPredicateCount[j]) {
						meaningfulPredicates.add(RandomProvider.getInstance().nextInt(MultiMaskEfsmSkeleton.PREDICATE_COUNT));
					}
					for (Integer predicateId : meaningfulPredicates) {
						tg.setMaskElement(predicateId, true);
					}

					//set new state for each transition
					for (int k = 0; k < tg.getTransitionsCount(); k++) {
						tg.setNewState(k, RandomProvider.getInstance().nextInt(MultiMaskEfsmSkeleton.STATE_COUNT + 1) - 1);
					}
					states[i].addTransitionGroup(inputEvent, tg);
				}
			}
		}
		
		return new MultiMaskEfsmSkeleton(states);
	}
	
}
