package ru.ifmo.optimization.algorithm.genetic.factory;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.algorithm.genetic.GeneticAlgorithm;
import ru.ifmo.optimization.algorithm.genetic.config.GeneticAlgorithmConfig;
import ru.ifmo.optimization.instance.Checkable;
import ru.ifmo.optimization.instance.fsm.algorithm.factory.OptimizationAlgorithmFactory;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;

public class GeneticAlgorithmFactory<Instance extends Checkable<Instance, MutationType>, MutationType extends InstanceMutation<Instance>> 
		extends OptimizationAlgorithmFactory<Instance> {
	public GeneticAlgorithmFactory(OptimizationRunnerConfig runnerConfig) {
		super(runnerConfig);
	}

	@Override
	public AbstractOptimizationAlgorithm<Instance> createOptimizationAlgorithm(AbstractTaskFactory<Instance> taskFactory) {
		return new GeneticAlgorithm<Instance, MutationType>(new GeneticAlgorithmConfig("genetic.properties"), taskFactory);	
      }
		
}
