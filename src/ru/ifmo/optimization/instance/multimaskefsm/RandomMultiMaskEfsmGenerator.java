package ru.ifmo.optimization.instance.multimaskefsm;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.instance.InstanceGenerator;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class RandomMultiMaskEfsmGenerator implements InstanceGenerator {

	@Override
	public MultiMaskEfsmSkeleton createInstance(AbstractOptimizationTask task) {
		State[] states = new State[MultiMaskEfsmSkeleton.STATE_COUNT];
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < states.length; i++) {
			states[i] = new State();
//			for (int j = 0; j < states[i].getNumberOfOutputActions(); j++) {
//				states[i].setFixedActionId(j, RandomProvider.getInstance().nextInt(((MultiMaskTask)task).getPrecalculatedActionsCount()));
//			}
			
			int[] meaningfulPredicateCount = new int[MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT];
			for (int j = 0; j < meaningfulPredicateCount.length; j++) {
				meaningfulPredicateCount[j] = Math.max(1, random.nextInt(MultiMaskEfsmSkeleton.MEANINGFUL_PREDICATES_COUNT + 1));
			}
			
			for (String inputEvent : MultiMaskEfsmSkeleton.INPUT_EVENTS.keySet()) {
				for (int j = 0; j < MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT; j++) {
					TransitionGroup tg = new TransitionGroup(meaningfulPredicateCount[j]); 

					//set random mask
					Set<Integer> meaningfulPredicates = new HashSet<Integer>();


					while (meaningfulPredicates.size() < meaningfulPredicateCount[j]) {
						meaningfulPredicates.add(random.nextInt(MultiMaskEfsmSkeleton.PREDICATE_COUNT));
					}
					for (Integer predicateId : meaningfulPredicates) {
						tg.setMaskElement(predicateId, true);
					}

					//set new state for each transition
					for (int k = 0; k < tg.getTransitionsCount(); k++) {
						tg.setNewState(k, random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT + 1) - 1);
					}
					states[i].addTransitionGroup(inputEvent, tg);
				}
			}
		}
		
		return new MultiMaskEfsmSkeleton(states);
	}
	
}
