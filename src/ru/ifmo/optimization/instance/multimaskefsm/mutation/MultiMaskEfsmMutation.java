package ru.ifmo.optimization.instance.multimaskefsm.mutation;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public abstract class MultiMaskEfsmMutation implements InstanceMutation<MultiMaskEfsmSkeleton> {

	protected int state;
	protected int eventId;
	protected int transitionGroup;
	
	public MultiMaskEfsmMutation(int state, int eventId, int transitionGroup) {
		this.state = state;
		this.eventId = eventId;
		this.transitionGroup = transitionGroup;
	}
	
	@Override
	public String toString() {
		return state + "(" + eventId + "," + transitionGroup + ")";
	}
}
