package ru.ifmo.optimization.algorithm.muaco.parallel;

import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.fsm.task.factory.RandomClusteringTestsModelCheckingTaskFactory;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.TestsModelCheckingTask;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class RandomClusteringParallelMuACO<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> 
	extends CrossoverAndSharedBestParallelMuACO<Instance, MutationType> {

	public RandomClusteringParallelMuACO(MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(config, taskFactory);
		
		int importanceFactor = Integer.parseInt(config.getProperty("importance-factor"));
		int numberOfClusters = Integer.parseInt(config.getProperty("number-of-clusters"));
		

		RandomClusteringTestsModelCheckingTaskFactory factory = new RandomClusteringTestsModelCheckingTaskFactory(taskFactory.getConfig(), numberOfClusters, importanceFactor,
				((TestsModelCheckingTask)taskFactory.createTask()).getTests());
	
		
		algorithms.clear();
		
		for (int i = 0; i < numberOfThreads; i++) {
			algorithms.add(new CrossoverAndSharedBestMuACO<Instance, MutationType>(config, (AbstractOptimizationTask<Instance>) factory.createTask(i), bestThreadInstances, i));
		}
	}
}
