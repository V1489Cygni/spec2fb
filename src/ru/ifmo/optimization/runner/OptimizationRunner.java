package ru.ifmo.optimization.runner;

import java.io.File;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;
import ru.ifmo.random.RandomProvider;

/**
 * 
 * @author Daniil Chivilikhin
 *
 */
public class OptimizationRunner {	
	private AbstractOptimizationAlgorithm optimizationAlgorithm;
	private AbstractTaskFactory taskFactory;
	private OptimizationRunnerConfig config;
	private int randomSeed;
	
	public OptimizationRunner(OptimizationRunnerConfig config, int randomSeed) {
		this.config = config;
		taskFactory = config.getTaskFactory();
		this.randomSeed = randomSeed;
	}
	
	public void run() {
		String attemptsDirName = config.solutionDirName();
		File attemptsDir = new File(attemptsDirName);
		attemptsDir.mkdir();
		
		RandomProvider.initialize(new MuACOConfig("muaco.properties").getNumberOfThreads() + 1, randomSeed);
		for (int i = 0; i < config.numberOfExperiments(); i++) {
			OptimizationAlgorithmCutoff.getInstance().setCutoff(config.getMaxEvalutions(), config.getMaxRunTime(), System.currentTimeMillis());
			optimizationAlgorithm = config.getOptimizationAlgorithm(taskFactory);			
			String dirName = attemptsDirName + "/attempt" + i + "/";
			File dir = new File(dirName);
			dir.mkdir();
			long start = System.currentTimeMillis();
			InstanceMetaData best = optimizationAlgorithm.runAlgorithm();
			best.setTime((System.currentTimeMillis() - start) / 1000.0);
			best.setInstanceGenerationTime((best.getInstanceGenerationTime() - start) / 1000.0);
			best.setNumberOfCacheHits(RunStats.N_CACHE_HITS);
			best.setNumberOfCanonicalCacheHits(RunStats.N_CANONICAL_CACHE_HITS);
			best.setNumberOfLazySavedFitnessEvals(RunStats.N_SAVED_EVALS_LAZY);
			best.setSharedBundleHits(RunStats.GRAPH_BUNDLE_HITS.intValue());
			best.setCanonicalDistance(RunStats.ERROR);
			RunStats.reset();
			best.print(dirName);
		}
	}
}
