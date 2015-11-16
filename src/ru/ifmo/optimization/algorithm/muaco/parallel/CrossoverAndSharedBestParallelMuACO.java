package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class CrossoverAndSharedBestParallelMuACO<Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends ParallelMuACO<Instance, MutationType> {

	protected FitInstance<Instance> bestThreadInstances[];
	protected AbstractTaskFactory<Instance> taskFactory;
	List<Instance> initialSolutions = new ArrayList<Instance>();
	private Random random = new Random();
	private boolean useSharedBestCache;
	
	public CrossoverAndSharedBestParallelMuACO(
			MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(config, taskFactory);
		bestThreadInstances = new FitInstance[numberOfThreads];
		algorithms = new ArrayList<MuACO<Instance, MutationType>>();
		this.taskFactory = taskFactory;
		
		useSharedBestCache = Boolean.parseBoolean(config.getProperty("use-shared-best-cache"));
		
		String initialSolutionsDir = config.getInitialSolutionsDir();
		if (initialSolutionsDir != null) {
			for (FSM fsm : loadInitialSolutions(config.getProperty("initial-solutions-dir"))) {
				initialSolutions.add((Instance) fsm);
			}
		}
		
		for (int i = 0; i < numberOfThreads; i++) {
			algorithms.add(createMuACO(config, i));
		}
	}
	
	protected MuACO<Instance, MutationType> createMuACO(MuACOConfig<Instance, MutationType> config, int threadId) {
		if (initialSolutions.isEmpty()) {
			if (useSharedBestCache) {
				return new CrossoverAndSharedBestMuACO<Instance, MutationType>(config, taskFactory.createTask(), bestThreadInstances, threadId);
			} else {
				return new CrossoverMuACO<Instance, MutationType>(config, taskFactory.createTask(), bestThreadInstances, threadId);
			}
		} else {
			if (useSharedBestCache) {
				return new CrossoverAndSharedBestMuACO<Instance, MutationType>(config, taskFactory.createTask(), 
					bestThreadInstances, threadId, initialSolutions.get(
							initialSolutions.size() == numberOfThreads ? threadId : random.nextInt(initialSolutions.size())
							));
			} else {
				return new CrossoverMuACO<Instance, MutationType>(config, taskFactory.createTask(), 
						bestThreadInstances, threadId, initialSolutions.get(
							initialSolutions.size() == numberOfThreads ? threadId : random.nextInt(initialSolutions.size())
							));
			}
		}
	}
	
	protected AbstractOptimizationTask<Instance> createTask() {
		return taskFactory.createTask();
	}
}