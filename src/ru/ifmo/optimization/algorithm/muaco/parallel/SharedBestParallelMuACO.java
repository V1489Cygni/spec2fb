package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.util.ArrayList;

import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;

public class SharedBestParallelMuACO <Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends ParallelMuACO<Instance, MutationType> {

	private FitInstance<Instance> bestThreadInstances[];
	
	public SharedBestParallelMuACO(MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(config, taskFactory);
		bestThreadInstances = new FitInstance[numberOfThreads];
		algorithms = new ArrayList<MuACO<Instance, MutationType>>();
		for (int i = 0; i < numberOfThreads; i++) {
			algorithms.add(new SharedBestMuACO<Instance, MutationType>(config, taskFactory.createTask(), bestThreadInstances, i));
		}
	}
}
