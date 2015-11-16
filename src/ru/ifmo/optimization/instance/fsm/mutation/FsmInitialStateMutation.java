package ru.ifmo.optimization.instance.fsm.mutation;

import ru.ifmo.optimization.instance.fsm.FSM;

public class FsmInitialStateMutation extends FsmMutation {

	private int newInitialState;
	
	public FsmInitialStateMutation(int newInitialState) {
		this.newInitialState = newInitialState;
	}
	
	@Override
	public boolean equals(Object arg0) {
		FsmInitialStateMutation other = (FsmInitialStateMutation)arg0;
		return newInitialState == other.newInitialState;
	}
	
	@Override
	public int hashCode() {
		return ("" + newInitialState).hashCode();
	}
	
	@Override
	public void apply(FSM instance) {
		instance.setInitialState(newInitialState);
	}
	
	@Override
	public String toString() {
		return "new initial state = " + newInitialState;
	}

}
