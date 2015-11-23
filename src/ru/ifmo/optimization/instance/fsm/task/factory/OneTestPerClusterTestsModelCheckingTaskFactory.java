package ru.ifmo.optimization.instance.fsm.task.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.ClusteringTestsModelCheckingTask;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class OneTestPerClusterTestsModelCheckingTaskFactory extends ClusteringTestsModelCheckingTaskFactory {
	
	private List<Integer> hardTestIds = new ArrayList<Integer>();
	private double minImportanceFactor;
	private double maxImportanceFactor;
	
	private class Pair implements Comparable<Pair> {

		private double value;
		private int id;
		
		public Pair(double value, int id) {
			this.value = value;
			this.id = id;
		}
		
		@Override
		public int compareTo(Pair o) {
			return Double.compare(value, o.value);
		}
		
	}

 	public OneTestPerClusterTestsModelCheckingTaskFactory(
			AbstractTaskConfig config, int numberOfClusters,
			double minImportanceFactor, double maxImportanceFactor, 
			List<AutomatonTest> tests, int[] clusters, List<double[]> points) {
		super(config, numberOfClusters, 0.5 * (maxImportanceFactor + minImportanceFactor), tests, clusters);
		
		this.minImportanceFactor = minImportanceFactor;
		this.maxImportanceFactor = maxImportanceFactor;
		
		testGroups.clear();
		for (int clusterId = 0; clusterId < tests.size(); clusterId++) {
			testGroups.add(new ArrayList<AutomatonTest>());
			testGroups.get(testGroups.size() - 1).add(tests.get(clusterId));
		}
		
		for (int clusterId = 0; clusterId < numberOfClusters; clusterId++) {
			List<Integer> testIds = new ArrayList<Integer>();
			for (int testId = 0; testId < clusters.length; testId++) {
				if (clusters[testId] == clusterId) {
					testIds.add(testId);
				}
			}
			double[] cumulativeTestPerformance = new double[testIds.size()];
			Arrays.fill(cumulativeTestPerformance, 0);
			
			for (int i = 0; i < testIds.size(); i++) {
				for (double[] testResult : points) {
					cumulativeTestPerformance[i] += testResult[testIds.get(i)];
				}
			}
			
			double minValue = Double.MAX_VALUE;
			int hardestTestId = -1;
			for (int i = 0; i < cumulativeTestPerformance.length; i++) {
				if (cumulativeTestPerformance[i] < minValue) {
					minValue = cumulativeTestPerformance[i];
					hardestTestId = testIds.get(i);
				}
			}
			hardTestIds.add(hardestTestId);
		}
//		double[] cumulativeTestPerformance = new double[tests.size()];
//		List<Pair> cumulativeTestPerformance = new ArrayList<Pair>();
//		for (int i = 0; i < tests.size(); i++) {
//			cumulativeTestPerformance.add(new Pair(0, i));
//		}
//		
//		for (int i = 0; i < tests.size(); i++) {
//			for (double[] testResult : points) {
//				cumulativeTestPerformance.get(i).value += testResult[i];
//			}
//		}
//
//		Collections.sort(cumulativeTestPerformance);
//		
//		for (int i = 0; i < numberOfClusters; i++) {
//			hardTestIds.add(cumulativeTestPerformance.get(i).id);
//		}
//		
		for (Integer hardTestId : hardTestIds) {
			System.out.println("Hard test #" + hardTestId + ": " + tests.get(hardTestId));
		}
	}
	
	public AbstractOptimizationTask<FSM> createTask(int threadId) {
		List<Double> groupCost = new ArrayList<Double>();
		
		double factor = minImportanceFactor + threadId * (maxImportanceFactor - minImportanceFactor) / ((double)numberOfClusters);
		System.out.println("Thread #" + threadId + ": importance factor = " + factor);
		for (int i = 0; i < tests.size(); i++) {
			double sum = tests.size() + numberOfClusters * (factor - 1.0);
			groupCost.add(hardTestIds.contains(i) ? factor / sum : 1.0 / sum);
			System.out.print("EccToSmv " + i + ": w=" + groupCost.get(groupCost.size() - 1));
		}
		System.out.println();

		return new ClusteringTestsModelCheckingTask(config, testGroups, groupCost); 
	}
}
