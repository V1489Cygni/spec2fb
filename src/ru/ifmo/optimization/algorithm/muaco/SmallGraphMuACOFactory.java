package ru.ifmo.optimization.algorithm.muaco;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.algorithm.rmhc.config.RmhcBoosterConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.fsm.algorithm.factory.OptimizationAlgorithmFactory;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;

public class SmallGraphMuACOFactory<Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends OptimizationAlgorithmFactory<Instance> {

	public SmallGraphMuACOFactory(OptimizationRunnerConfig runnerConfig) {
		super(runnerConfig);
	}
	
	@Override
	public AbstractOptimizationAlgorithm<Instance> createOptimizationAlgorithm(AbstractTaskFactory<Instance> taskFactory) {
		return new SmallGraphMuACO<Instance, MutationType>(new MuACOConfig<Instance, MutationType>("muaco.properties"),
				taskFactory);
	}
}