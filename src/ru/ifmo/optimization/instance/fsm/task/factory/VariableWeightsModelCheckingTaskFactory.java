package ru.ifmo.optimization.instance.fsm.task.factory;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.VariableWeightsTestsModelCheckingTask;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class VariableWeightsModelCheckingTaskFactory extends AbstractTaskFactory<FSM> {

	public VariableWeightsModelCheckingTaskFactory(AbstractTaskConfig config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractOptimizationTask<FSM> createTask() {
		return new VariableWeightsTestsModelCheckingTask(config, 1.0, 1.0);
	}
	
	public AbstractOptimizationTask<FSM> createTask(double ltlCost, double testsCost) {
		return new VariableWeightsTestsModelCheckingTask(config, ltlCost, testsCost);
	}
}
