package ru.ifmo.optimization.instance.fsm.task.fbdkecc.simple;

import ru.ifmo.optimization.instance.fsm.FSM;

public class MooreMachine {
	protected FSM fsm;
	protected String[] stateLabels;
	
	public MooreMachine(FSM fsm, String[] stateLabels) {
		this.fsm = fsm;
		this.stateLabels = stateLabels;
	}
	
	public FSM getFSM() {
		return fsm;
	}

	public String[] getStateLabels() {
		return stateLabels;
	}
	
	public String getStateLabel(int stateId) {
		return stateLabels[stateId];
	}

}
