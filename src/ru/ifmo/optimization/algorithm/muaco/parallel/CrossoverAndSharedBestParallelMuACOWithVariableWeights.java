package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.fsm.task.factory.VariableWeightsModelCheckingTaskFactory;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class CrossoverAndSharedBestParallelMuACOWithVariableWeights<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> 
	extends CrossoverAndSharedBestParallelMuACO<Instance, MutationType> {

	public CrossoverAndSharedBestParallelMuACOWithVariableWeights(
			MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(config, taskFactory);
		
		VariableWeightsModelCheckingTaskFactory factory = new VariableWeightsModelCheckingTaskFactory(taskFactory.getConfig());
		algorithms.clear();
		for (int i = 0; i < numberOfThreads; i++) {
			double ltlCost = 1.0 + (ThreadLocalRandom.current().nextBoolean() 
					? 0.5 * ThreadLocalRandom.current().nextDouble() 
							: -0.5 * ThreadLocalRandom.current().nextDouble()); 
			double testsCost =  2.0 - ltlCost;
			System.out.println(i + ": ltlCost=" + ltlCost + ", testsCost=" + testsCost);
			algorithms.add(new CrossoverAndSharedBestMuACO<Instance, MutationType>(
					config, (AbstractOptimizationTask<Instance>) factory.createTask(ltlCost, testsCost), bestThreadInstances, i));
		}
	}
}
