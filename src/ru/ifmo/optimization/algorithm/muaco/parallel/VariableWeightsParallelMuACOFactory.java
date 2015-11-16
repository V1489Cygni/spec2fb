package ru.ifmo.optimization.algorithm.muaco.parallel;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.algorithm.factory.OptimizationAlgorithmFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;

public class VariableWeightsParallelMuACOFactory extends OptimizationAlgorithmFactory<FSM> {

	public VariableWeightsParallelMuACOFactory(OptimizationRunnerConfig runnerConfig) {
		super(runnerConfig);
	}
	
	@Override
	public AbstractOptimizationAlgorithm<FSM> createOptimizationAlgorithm(AbstractTaskFactory<FSM> taskFactory) {
		return new VariableWeightsParallelMuACO(new MuACOConfig("muaco.properties"), taskFactory);
	}
}