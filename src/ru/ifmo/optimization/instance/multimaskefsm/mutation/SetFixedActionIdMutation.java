package ru.ifmo.optimization.instance.multimaskefsm.mutation;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;

public class SetFixedActionIdMutation extends MultiMaskEfsmMutation {

	private int actionNumber;
	private int fixedActionId;
	
	public SetFixedActionIdMutation(int state, int eventId, int transitionGroup, int actionNumber, int fixedActionId) {
		super(state, eventId, transitionGroup);
		this.actionNumber = actionNumber;
		this.fixedActionId = fixedActionId;
	}

	@Override
	public void apply(MultiMaskEfsmSkeleton instance) {
		instance.setFixedActionId(state, actionNumber, fixedActionId);
	}

	@Override
	public String toString() {
		return state + "(" + actionNumber + "-" + fixedActionId + ")";
	}
}
