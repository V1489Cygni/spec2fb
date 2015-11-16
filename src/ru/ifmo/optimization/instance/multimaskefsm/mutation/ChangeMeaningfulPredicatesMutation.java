package ru.ifmo.optimization.instance.multimaskefsm.mutation;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;

public class ChangeMeaningfulPredicatesMutation extends MultiMaskEfsmMutation {

	private boolean add;
	private int predicate;
	
	public ChangeMeaningfulPredicatesMutation(int state, int eventId, int transitionGroup, boolean add, int predicate) {
		super(state, eventId, transitionGroup);
		this.add = add;
		this.predicate = predicate;
	}

	@Override
	public void apply(MultiMaskEfsmSkeleton instance) {
		if (add) {
			instance.getState(state).getTransitionGroup(eventId, transitionGroup).addPredicate(predicate);
		} else {
			instance.getState(state).getTransitionGroup(eventId, transitionGroup).removePredicate(predicate);
		}
	}

	
	@Override
	public String toString() {
		return state + "(" + eventId + "," + transitionGroup + "," + add + "," + predicate + ")";
	}
}
