package ru.ifmo.optimization.algorithm.muaco.parallel;

import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class CrossoverMuACO<Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends CrossoverAndSharedBestMuACO<Instance, MutationType> {

public CrossoverMuACO(MuACOConfig<Instance, MutationType> config,
		AbstractOptimizationTask<Instance> createTask,
		FitInstance<Instance>[] bestThreadInstances, int i,
		Instance instance) {
	super(config, createTask, bestThreadInstances, i, instance);
}

public CrossoverMuACO(MuACOConfig<Instance, MutationType> config,
		AbstractOptimizationTask<Instance> createTask,
		FitInstance<Instance>[] bestThreadInstances, int i) {
	super(config, createTask, bestThreadInstances, i);
}


@Override
protected FitInstance<Instance> getInitialSolutionForRestart() {
	return task.getFitInstance(randomInstance());
}

}