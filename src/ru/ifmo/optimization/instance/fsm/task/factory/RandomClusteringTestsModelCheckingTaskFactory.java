package ru.ifmo.optimization.instance.fsm.task.factory;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.ClusteringTestsModelCheckingTask;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class RandomClusteringTestsModelCheckingTaskFactory extends AbstractTaskFactory<FSM> {

	protected int numberOfClusters;
	protected List<List<AutomatonTest>> testGroups = new ArrayList<List<AutomatonTest>>();
	protected double importanceFactor;
	protected List<AutomatonTest> tests;
	
	public RandomClusteringTestsModelCheckingTaskFactory(AbstractTaskConfig config, int numberOfClusters, double importanceFactor, List<AutomatonTest> tests) {
		super(config);
		
		this.numberOfClusters = numberOfClusters;
		this.importanceFactor = importanceFactor;
		this.tests = tests;
	}

	@Override
	public AbstractOptimizationTask<FSM> createTask() {
		List<Double> groupCost = new ArrayList<Double>();
		for (int i = 0; i < numberOfClusters; i++) {
			groupCost.add(1.0 / numberOfClusters);
		}
		
		return new ClusteringTestsModelCheckingTask(config, testGroups, groupCost);
	}
	
	public AbstractOptimizationTask<FSM> createTask(int threadId) {
		List<Double> groupCost = new ArrayList<Double>();
		
		double sum = numberOfClusters - 1 + importanceFactor;
		for (int i = 0; i < numberOfClusters; i++) {
			groupCost.add(i == threadId ? importanceFactor / sum : 1.0 / sum);
		}
		
		for (int clusterId = 0; clusterId < numberOfClusters; clusterId++) {
			System.out.print("cluster " + clusterId + "(w=" + groupCost.get(clusterId) + ", size=" + testGroups.get(clusterId).size() + "); ");
		}
		System.out.println();

		return new ClusteringTestsModelCheckingTask(config, testGroups, groupCost); 
	}
}
