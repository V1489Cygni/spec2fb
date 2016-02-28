package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.task.factory.ClusteringTestsModelCheckingTaskFactory;
import ru.ifmo.optimization.instance.fsm.task.factory.RandomClusteringTestsModelCheckingTaskFactory;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.TestsModelCheckingTask;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class KmeansClusteringParallelMuACO<Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends CrossoverAndSharedBestParallelMuACO<Instance, MutationType> {

	protected MuACOConfig<Instance, MutationType> muacoConfig;
	protected double importanceFactor;
	protected int numberOfClusters;
	protected double maxSamplingTime;
	protected int maxSamplingPoints;
	protected AbstractOptimizationTask<Instance> task;
	protected boolean oneSamplingIteration;
	protected boolean discardSatForInitialSampling;
	protected int numberOfSamplingThreads;
	
	public KmeansClusteringParallelMuACO(
			MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(config, taskFactory);
		this.muacoConfig = config;
		importanceFactor = Double.parseDouble(config.getProperty("importance-factor"));
		numberOfClusters = Integer.parseInt(config.getProperty("number-of-clusters"));
		maxSamplingTime = Double.parseDouble(config.getProperty("max-sampling-time"));
		maxSamplingPoints = Integer.parseInt(config.getProperty("max-sampling-points"));
		task = taskFactory.createTask();
		oneSamplingIteration = Boolean.parseBoolean(config.getProperty("one-sampling-iteration", "true"));
		discardSatForInitialSampling = Boolean.parseBoolean(config.getProperty("discard-sat-for-initial-sampling", "false"));
		numberOfSamplingThreads = Integer.parseInt(config.getProperty("number-of-sampling-threads", "0"));
	}
	
	protected void performClustering(int iteration) {
		long start = System.currentTimeMillis();
		System.out.println("Staring clustering at iteration #" + iteration);
		List<Instance> solutions = new ArrayList<Instance>();
		
		if (!discardSatForInitialSampling) {
			for (MuACO<Instance, MutationType> muaco : algorithms) {
				muaco.printSortedBestSolutions(maxSamplingPoints, "ff-points-" + iteration);
				solutions.add(muaco.getBestSolution());
			}
		} else {
			for (int i = 0; i < numberOfThreads - numberOfSamplingThreads; i++) {
				solutions.add(algorithms.get(i).getBestSolution());
			}
			for (int i = numberOfThreads - numberOfSamplingThreads; i < numberOfThreads; i++) {
				algorithms.get(i).printSortedBestSolutions(maxSamplingPoints, "ff-points-" + iteration);
				if (initialSolutions.isEmpty()) {
					solutions.add(algorithms.get(i).getBestSolution());
				} else {
					solutions.add(initialSolutions.get(ThreadLocalRandom.current().nextInt(initialSolutions.size())));
				}
			}
		}
		
		RandomClusteringTestsModelCheckingTaskFactory factory = null;
		
		int ntests = ((TestsModelCheckingTask)task).getNumberOfTests();
		int clusters[] = new int[ntests];
		try {
			Process p = Runtime.getRuntime().exec("run-clustering " + iteration + " " + numberOfClusters);
			p.waitFor();
			
			Scanner in = new Scanner(new File("clusters-" + iteration));
			
			for (int i = 0; i < ntests; i++) {
				clusters[i] = in.nextInt();
			}
			in.close();
			
			for (int c : clusters) {
				System.out.print(c + " ");
			}
			System.out.println();
			
			factory = new ClusteringTestsModelCheckingTaskFactory(
					taskFactory.getConfig(), numberOfClusters, importanceFactor,
					((TestsModelCheckingTask)taskFactory.createTask()).getTests(), clusters);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			
			factory = new RandomClusteringTestsModelCheckingTaskFactory(taskFactory.getConfig(), numberOfClusters, 1,
					((TestsModelCheckingTask)taskFactory.createTask()).getTests());
		}
		
		algorithms.clear();
		
		if (iteration == 0 && discardSatForInitialSampling && !initialSolutions.isEmpty()) {
			for (int i = 0; i < numberOfThreads; i++) {
				if (numberOfThreads == numberOfClusters) {
					algorithms.add(new CrossoverAndSharedBestMuACO<Instance, MutationType>(
							muacoConfig, (AbstractOptimizationTask<Instance>) factory.createTask(i),
							bestThreadInstances, i, initialSolutions.get(ThreadLocalRandom.current().nextInt(initialSolutions.size()))));
				} else {
					algorithms.add(new CrossoverAndSharedBestMuACO<Instance, MutationType>(
							muacoConfig, (AbstractOptimizationTask<Instance>) factory.createTask(
									ThreadLocalRandom.current().nextInt(numberOfClusters)), 
									bestThreadInstances, i, initialSolutions.get(ThreadLocalRandom.current().nextInt(initialSolutions.size()))));
				}
			}
		} else {
			for (int i = 0; i < numberOfThreads; i++) {
				if (numberOfThreads == numberOfClusters) {
					algorithms.add(new CrossoverAndSharedBestMuACO<Instance, MutationType>(
							muacoConfig, (AbstractOptimizationTask<Instance>) factory.createTask(i),
							bestThreadInstances, i, solutions.get(i)));
				} else {
					algorithms.add(new CrossoverAndSharedBestMuACO<Instance, MutationType>(
							muacoConfig, (AbstractOptimizationTask<Instance>) factory.createTask(
									ThreadLocalRandom.current().nextInt(numberOfClusters)), 
									bestThreadInstances, i, solutions.get(i)));
				}
			}
		}
		
		System.out.println("Finished clustering on iteration #" + iteration + ", time elapsed=" + (System.currentTimeMillis() - start) / 1000.0 + " s.");
	}
	
	@Override
	public InstanceMetaData<Instance> runAlgorithm() {
		int iteration = 0;
		while (true) {
			double tmp = OptimizationAlgorithmCutoff.getInstance().getMaxRunTime();
			OptimizationAlgorithmCutoff.getInstance().setMaxRunTime(
					OptimizationAlgorithmCutoff.getInstance().getCurrentRunTimeSeconds() + maxSamplingTime);
			
			
			if (iteration == 0 && discardSatForInitialSampling) {
				for (int i = 0; i < numberOfSamplingThreads; i++) {
					algorithms.remove(algorithms.size() - 1);
				}
				
				for (int i = 0; i < numberOfSamplingThreads; i++) {
					algorithms.add(new MuACO<Instance, MutationType>(muacoConfig, taskFactory));
				}
			}
			
			InstanceMetaData<Instance> result = super.runAlgorithm();

			if (result.getFitness() >= task.getDesiredFitness()) {
				if (task.getFitInstance(result.getInstance()).getFitness() < 2) {
					throw new RuntimeException("Wrong solution with fitness = " + task.getFitInstance(result.getInstance()).getFitness());
				}
				return result;
			}

			performClustering(iteration++);
			OptimizationAlgorithmCutoff.getInstance().setMaxRunTime(tmp);
			
			if (oneSamplingIteration) {
				result = super.runAlgorithm();
				if (task.getFitInstance(result.getInstance()).getFitness() < 2) {
					throw new RuntimeException("Wrong solution with weighted fitness = " + result.getFitness() + 
							" and true fitness = " + task.getFitInstance(result.getInstance()).getFitness());
				}
				return result;
			}
		}
	}
}