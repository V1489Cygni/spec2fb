package ru.ifmo.optimization.instance.multimaskefsm.mutation;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;

public class ChangeNumberOfActionsMutation extends MultiMaskEfsmMutation {

	private int newValue;
	
	public ChangeNumberOfActionsMutation(int state, int eventId, int transitionGroup, int newValue) {
		super(state, eventId, transitionGroup);
		this.newValue = newValue;
	}

	@Override
	public void apply(MultiMaskEfsmSkeleton instance) {
		instance.getState(state).setNumberOfOutputActions(newValue);
	}
	
	@Override
	public String toString() {
		return state + "(" + newValue + ")";
	}

}
