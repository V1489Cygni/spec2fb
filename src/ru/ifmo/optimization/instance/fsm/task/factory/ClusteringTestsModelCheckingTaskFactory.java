package ru.ifmo.optimization.instance.fsm.task.factory;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class ClusteringTestsModelCheckingTaskFactory extends RandomClusteringTestsModelCheckingTaskFactory {

	public ClusteringTestsModelCheckingTaskFactory(AbstractTaskConfig config,
			int numberOfClusters, double importanceFactor,
			List<AutomatonTest> tests, int[] clusters) {
		super(config, numberOfClusters, importanceFactor, tests);
		
		testGroups.clear();
		
		for (int clusterId = 0; clusterId < numberOfClusters; clusterId++) {
			testGroups.add(new ArrayList<AutomatonTest>());
		}
		
		for (int testId = 0; testId < tests.size(); testId++) {
			int cluster = clusters[testId];
			testGroups.get(cluster).add(tests.get(testId));
		}
	}
}
