package ru.ifmo.optimization.instance.fsm;

import java.util.List;

import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class CanonicalFSMGenerator extends InitialFSMGenerator {
	@Override
	public FSM createInstance(AbstractOptimizationTask t) {
		return super.createInstance(t).getCanonicalFSM();
	}

	@Override
	public FSM createInstance(int numberOfStates, List<String> events,
			String[] actions, AutomatonTaskConstraints constraints) {
		return super.createInstance(numberOfStates, events, actions, constraints).getCanonicalFSM();
	}
}
