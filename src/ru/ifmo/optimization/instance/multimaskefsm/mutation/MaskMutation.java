package ru.ifmo.optimization.instance.multimaskefsm.mutation;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;

public class MaskMutation extends MultiMaskEfsmMutation {

	private int setToFalseMaskElementId;
	private int setToTrueMaskElementId;
	
	public MaskMutation(int state, int eventId, int transitionGroup, int setToFalseMaskElementId, int setToTrueMaskElementId) {
		super(state, eventId, transitionGroup);
		this.setToFalseMaskElementId = setToFalseMaskElementId;
		this.setToTrueMaskElementId = setToTrueMaskElementId;
	}

	@Override
	public void apply(MultiMaskEfsmSkeleton instance) {
		instance.getState(state).getTransitionGroup(eventId, transitionGroup).setMaskElement(setToFalseMaskElementId, false);
		instance.getState(state).getTransitionGroup(eventId, transitionGroup).setMaskElement(setToTrueMaskElementId, true);
	}
	
	@Override
	public String toString() {
		return super.toString() + "; f=" + setToFalseMaskElementId + ", t=" + setToTrueMaskElementId;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MaskMutation)) {
			return false;
		}
		MaskMutation o = (MaskMutation)obj;
		return o.state == state &&
				o.eventId == eventId &&
				o.transitionGroup == transitionGroup &&
				o.setToFalseMaskElementId == setToFalseMaskElementId &&
				o.setToTrueMaskElementId == setToTrueMaskElementId;
	}
}
