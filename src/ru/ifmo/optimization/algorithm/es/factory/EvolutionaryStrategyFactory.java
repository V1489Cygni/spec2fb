package ru.ifmo.optimization.algorithm.es.factory;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.algorithm.es.AdaptiveIncreaseProbabilityEvolutionaryStrategy;
import ru.ifmo.optimization.algorithm.es.EvolutionaryStrategyWithCanonicalCache;
import ru.ifmo.optimization.algorithm.es.FixedLambdaEvolutionaryStrategy;
import ru.ifmo.optimization.algorithm.es.config.EvolutionaryStrategyConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.fsm.algorithm.factory.OptimizationAlgorithmFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;

public class EvolutionaryStrategyFactory<Instance extends Constructable<Instance>> extends OptimizationAlgorithmFactory<Instance> {
	public EvolutionaryStrategyFactory(OptimizationRunnerConfig runnerConfig) {
		super(runnerConfig);
	}

	@Override
	public AbstractOptimizationAlgorithm<Instance> createOptimizationAlgorithm(AbstractTaskFactory<Instance> taskFactory) {
		EvolutionaryStrategyConfig<Instance> config = new EvolutionaryStrategyConfig<Instance>("es.properties");
		
		FSM instance = new InitialFSMGenerator().createInstance(taskFactory.createTask());
		switch (config.getAdaptiveCriteria()) {
		case INCREASE_PROBABILITY:
			return new AdaptiveIncreaseProbabilityEvolutionaryStrategy(
					taskFactory, config.getMutators(taskFactory.createTask()), instance, config.lambda(), 
					config.getInitialSampleSize(), config.getDefaultLambda(), 
					config.getStatisticalThreshold(), config.doUseLazyFitnessCalculation(), 
					config.onePlusLambda(), config.getStagnationParameter());
		case DEFAULT:
			return new FixedLambdaEvolutionaryStrategy(taskFactory, config.getMutators(taskFactory.createTask()), instance, 
					config.lambda(), config.doUseLazyFitnessCalculation(), 
					config.onePlusLambda(), config.getStagnationParameter());
		case CANONICAL_ES:
			return new EvolutionaryStrategyWithCanonicalCache(taskFactory, config.getMutators(taskFactory.createTask()), instance,
					config.lambda(), config.doUseLazyFitnessCalculation(), config.onePlusLambda(), config.getStagnationParameter(), config.getMaxCanonicalCacheSize());
		default:
			return null;
		}
	}
}

