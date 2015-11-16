package ru.ifmo.optimization.instance.fsm.task.testsmodelchecking;

import java.util.List;

import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class ClusteringTestsModelCheckingTask extends TestsModelCheckingTask{

	public ClusteringTestsModelCheckingTask(AbstractTaskConfig config, List<List<AutomatonTest>> testGroups, List<Double> groupCost) {
		super(config);

		this.testGroups = testGroups;
		this.groupCost = groupCost;
	}
}
