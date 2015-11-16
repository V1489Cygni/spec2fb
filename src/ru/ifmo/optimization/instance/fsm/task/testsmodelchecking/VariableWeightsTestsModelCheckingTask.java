package ru.ifmo.optimization.instance.fsm.task.testsmodelchecking;

import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class VariableWeightsTestsModelCheckingTask extends TestsModelCheckingTask {

	public VariableWeightsTestsModelCheckingTask(AbstractTaskConfig config, double ltlCost, double testsCost) {
		super(config);
		this.ltlCost = ltlCost;
		this.testsCost = testsCost;
	}
	
	

}
