package ru.ifmo.optimization.algorithm.rmhc.factory;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.algorithm.rmhc.RandomMutationHillClimber;
import ru.ifmo.optimization.algorithm.rmhc.config.RmhcConfig;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.fsm.algorithm.factory.OptimizationAlgorithmFactory;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;

public class RandomMutationHillClimberFactory extends OptimizationAlgorithmFactory<FSM> {
	
	public RandomMutationHillClimberFactory(OptimizationRunnerConfig runnerConfig) {
		super(runnerConfig);
	}

	@Override
	public AbstractOptimizationAlgorithm<FSM> createOptimizationAlgorithm(AbstractTaskFactory<FSM> taskFactory) {
		return new RandomMutationHillClimber<FSM, FsmMutation>(
				taskFactory, new RmhcConfig("rmhc.properties").getMutators(taskFactory.createTask()),
				(FSM) new InitialFSMGenerator().createInstance(taskFactory.createTask()));
	}
}
