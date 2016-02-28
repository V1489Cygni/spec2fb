package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.factory.ClusteringTestsModelCheckingTaskFactory;
import ru.ifmo.optimization.instance.fsm.task.factory.OneTestPerClusterTestsModelCheckingTaskFactory;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.TestsModelCheckingTask;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RandomProjection;

public class NoInterruptionKmeansClusteringParallelMuACO<Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends KmeansClusteringParallelMuACO<Instance, MutationType> {
	
	protected int numberOfSamplingIterations;

	
	private enum TestClusteringAlgorithm {
		GROUP,
		ONE
	}
	
	private TestClusteringAlgorithm testClusteringAlgorithm;
	
	private String stringValue(double[] values) {
		StringBuilder sb = new StringBuilder();
		for (double v : values) {
			sb.append(v);
		}
		return sb.toString();
	}
	
	public class TestClusterer implements Callable<InstanceMetaData<Instance>> {
		
		List<double[]> points = new ArrayList<double[]>();
		
		public ClusteringTestsModelCheckingTaskFactory createFactory(AbstractTaskConfig config, int numberOfClusters, 
				double importanceFactor, List<AutomatonTest> tests, int[] clusters) {
			 return new ClusteringTestsModelCheckingTaskFactory(
						config, numberOfClusters, importanceFactor,
						tests, clusters);
		}
		
		@Override
		public InstanceMetaData<Instance> call() throws Exception {
			int iteration = 0;
			
			AbstractTaskConfig config = taskFactory.getConfig();
			List<AutomatonTest> tests = ((TestsModelCheckingTask)taskFactory.createTask()).getTests();
			while (true) {
				if (Thread.currentThread().isInterrupted()) {
					return null;
				}
				try {
					Thread.sleep((long)maxSamplingTime * 1000);
				} catch (InterruptedException e) {
					return null;
				}
				
				if (oneSamplingIteration && iteration > 0) {
					continue;
				}
				
				if (iteration >= numberOfSamplingIterations) {
					continue;
				}
				
				long start = System.currentTimeMillis();
				System.out.println("Starting clustering at iteration #" + iteration);
				List<Instance> solutions = new ArrayList<Instance>();
				
				Set<String> set = new HashSet<String>();
				
				if (!discardSatForInitialSampling) {
					System.out.println("Started collecting solutions for clustering");
					for (MuACO<Instance, MutationType> muaco : algorithms) {
						List<double[]> algPoints = muaco.getPoints(maxSamplingPoints);
						for (double[] point : algPoints) {
							String s = stringValue(point);
							if (set.contains(s)) {
								continue;
							} else {
								points.add(point);
								set.add(s);
							}
						}
						
						solutions.add(muaco.getBestSolution());
					}
					System.out.println("Collected solutions for clustering");
					System.out.println("Finished printing solutions, got " + points.size() + " unique attributes");
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
				
				System.out.println("Made data, time elapsed = " + (System.currentTimeMillis() - start) / 1000.0 + " sec.");
				
				FastVector attributes = new FastVector(points.size());
				for (int i = 0; i < points.size(); i++) {
					attributes.addElement(new Attribute("" + i));
				}

				Instances instances = new Instances("instances", attributes, tests.size());
				for (int i = 0; i < tests.size(); i++) {
					double[] data = new double[points.size()];
					for (int j = 0; j < data.length; j++) {
						data[j] = points.get(j)[i];
					}
					weka.core.Instance instance = new weka.core.Instance(1, data);
					instances.add(instance);
				}
				System.out.println("Built " + instances.numInstances() + " instances");
				
				ClusteringTestsModelCheckingTaskFactory factory = null;
				int ntests = ((TestsModelCheckingTask)task).getNumberOfTests();
				int clusters[] = new int[ntests];
				
				RandomProjection filter = new RandomProjection();
				filter.setNumberOfAttributes(30);
				filter.setInputFormat(instances);
				instances = Filter.useFilter(instances, filter);
				System.out.println("After filtering: " + instances.numAttributes() + " attributes");
				
				SimpleKMeans clusterer = new SimpleKMeans();
				
//				DensityBasedClusterer clusterer = new EM();
				clusterer.setNumClusters(numberOfClusters);
				System.out.println("Starting clustering...");
				clusterer.buildClusterer(instances);	
				System.out.println("Built clusterer! Time elapsed = " + (System.currentTimeMillis() - start) / 1000.0 + " sec.");
				System.out.println("Sum squared error = " + clusterer.getSquaredError());
				
				for (int i = 0; i < tests.size(); i++) {
					clusters[i] = clusterer.clusterInstance(instances.instance(i));
				}
				
				for (int c : clusters) {
					System.out.print(c + " ");
				}
				System.out.println();
				
				System.out.println("Creating task factory...");
				factory = createFactory(config, numberOfClusters, importanceFactor, tests, clusters);
				System.out.println("Created task factory");

				
				System.out.println("Substituting tasks");
				for (int i = 0; i < numberOfThreads; i++) {
					if (numberOfThreads == numberOfClusters) {
						algorithms.get(i).setTask((AbstractOptimizationTask<Instance>) factory.createTask(i));
					} else {
						algorithms.get(i).setTask((AbstractOptimizationTask<Instance>) factory.createTask(
								ThreadLocalRandom.current().nextInt(numberOfClusters)));
					}
				}
				
				iteration++;
				System.out.println("Finished clustering on iteration #" + iteration + ", time elapsed=" + (System.currentTimeMillis() - start) / 1000.0 + " s.");
			}
		}
	}
	
	public class OneTestPerClusterTestClusterer extends TestClusterer {
		
		@Override
		public ClusteringTestsModelCheckingTaskFactory createFactory(AbstractTaskConfig config, int numberOfClusters, 
				double importanceFactor, List<AutomatonTest> tests, int[] clusters) {
			
			double minImportanceFactor = Double.parseDouble(muacoConfig.getProperty("min-importance-factor"));
			double maxImportanceFactor = Double.parseDouble(muacoConfig.getProperty("max-importance-factor"));
			return new OneTestPerClusterTestsModelCheckingTaskFactory(
						config, numberOfClusters, minImportanceFactor, maxImportanceFactor,
						tests, clusters, points);
		}
	}

	public NoInterruptionKmeansClusteringParallelMuACO(
			MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(config, taskFactory);
		numberOfSamplingIterations = Integer.parseInt(config.getProperty("n-sampling-iterations", "1"));
		bestThreadInstances = new FitInstance[numberOfThreads];
		algorithms = new ArrayList<MuACO<Instance, MutationType>>();
		this.taskFactory = taskFactory;
		
		String initialSolutionsDir = config.getInitialSolutionsDir();
		if (initialSolutionsDir != null) {
			for (FSM fsm : loadInitialSolutions(config.getProperty("initial-solutions-dir"))) {
				initialSolutions.add((Instance) fsm);
			}
		}
		
		for (int i = 0; i < numberOfThreads; i++) {
			algorithms.add(createMuACO(config, i));
		}
		
		testClusteringAlgorithm = TestClusteringAlgorithm.valueOf(config.getProperty("test-clustering"));
	}
	
	private TestClusterer createTestClusterer() {
		switch (testClusteringAlgorithm) {
		case GROUP:
			return new TestClusterer();
		case ONE:
			return new OneTestPerClusterTestClusterer();
		}
		return null;
	}
	
	@Override
	public InstanceMetaData<Instance> runAlgorithm() {
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads + 1);
		try {
			System.out.println(algorithms.size());
			
			List<Callable<InstanceMetaData<Instance>>> callables = new ArrayList<Callable<InstanceMetaData<Instance>>>();
			callables.addAll(algorithms);
			
			callables.add(createTestClusterer());
			
			InstanceMetaData<Instance> result = executor.invokeAny(callables);						
			
			System.out.println("Main: one of threads finished");
			int numberOfFitnessEvaluations = 0;
			for (MuACO<Instance, MutationType> algorithm : algorithms) {
				numberOfFitnessEvaluations += algorithm.getNumberOfFitnessEvaluations();
			}
			System.out.println("Main: shutting down");
			executor.shutdownNow();
			System.out.println("Main: awaiting termination");
			executor.awaitTermination(1, TimeUnit.SECONDS);
			result.setNumberOfFitnessEvaluations(numberOfFitnessEvaluations);
			System.out.println("Main: exiting");

			double trueFitness = taskFactory.createTask().getFitInstance(result.getInstance()).getFitness();

			if (trueFitness < 2) {
				throw new RuntimeException("Wrong solution with weighted fitness = " + result.getFitness() + " and true fitness = " + trueFitness);
			}

			return result;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
}
