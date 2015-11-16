package ru.ifmo.optimization.instance.multimaskefsm.mutation;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;

public class DestinationStateMutation extends MultiMaskEfsmMutation {

	private int eventId;
	private int inputVarsId;
	private int newEndState;
	
	public DestinationStateMutation(int state, int eventId, int transitionGroup, int inputVarsId, int newEndState) {
		super(state, eventId, transitionGroup);
		this.inputVarsId = inputVarsId;
		this.newEndState = newEndState;
	}

	@Override
	public void apply(MultiMaskEfsmSkeleton instance) {
		instance.getState(state).getTransitionGroup(eventId, transitionGroup).setNewState(inputVarsId, newEndState);
	}
	
	@Override
	public String toString() {
		return super.toString() + "; e" + inputVarsId + ": " + newEndState;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DestinationStateMutation)) {
			return false;
		}
		DestinationStateMutation o = (DestinationStateMutation)obj;
		return o.state == state &&
				o.eventId == eventId && 
				o.transitionGroup == transitionGroup &&
				o.inputVarsId == inputVarsId &&
				o.newEndState == newEndState;
	}
}
