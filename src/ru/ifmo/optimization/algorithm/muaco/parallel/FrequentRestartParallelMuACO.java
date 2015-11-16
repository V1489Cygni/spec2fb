package ru.ifmo.optimization.algorithm.muaco.parallel;

import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;

public class FrequentRestartParallelMuACO <Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends CrossoverAndSharedBestParallelMuACO<Instance, MutationType> {

	public FrequentRestartParallelMuACO(
			MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(config, taskFactory);
	}
	
	@Override
	protected MuACO<Instance, MutationType> createMuACO(
			MuACOConfig<Instance, MutationType> config, int threadId) {
		return new FrequentRestartCrossoverAndSharedBestMuACO<Instance, MutationType>(config, taskFactory.createTask(), bestThreadInstances, threadId);
	}
	
}