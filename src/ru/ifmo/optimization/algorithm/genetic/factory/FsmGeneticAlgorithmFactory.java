package ru.ifmo.optimization.algorithm.genetic.factory;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.algorithm.genetic.FsmGeneticAlgorithm;
import ru.ifmo.optimization.algorithm.genetic.config.GeneticAlgorithmConfig;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.algorithm.factory.OptimizationAlgorithmFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;

public class FsmGeneticAlgorithmFactory extends OptimizationAlgorithmFactory<FSM> {
	public FsmGeneticAlgorithmFactory(OptimizationRunnerConfig runnerConfig) {
		super(runnerConfig);
	}

	@Override
	public AbstractOptimizationAlgorithm<FSM> createOptimizationAlgorithm(AbstractTaskFactory<FSM> taskFactory) {
		return new FsmGeneticAlgorithm(new GeneticAlgorithmConfig("genetic.properties"), taskFactory);	
      }

}
