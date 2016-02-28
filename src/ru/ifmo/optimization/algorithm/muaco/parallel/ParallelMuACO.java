package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;

public class ParallelMuACO<Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends AbstractOptimizationAlgorithm<Instance> {
	
	protected int numberOfThreads;
	protected List<MuACO<Instance, MutationType>> algorithms;

	public ParallelMuACO(MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(taskFactory);
		this.numberOfThreads = config.getNumberOfThreads();
		algorithms = new ArrayList<MuACO<Instance, MutationType>>();
		
		String initialSolutionsDir = config.getInitialSolutionsDir();
		List<Instance> initialSolutions = new ArrayList<Instance>();
		if (initialSolutionsDir != null) {
			for (FSM fsm : loadInitialSolutions(config.getProperty("initial-solutions-dir"))) {
				initialSolutions.add((Instance) fsm);
			}
		}

		long start = System.currentTimeMillis();
		Random random = new Random();
		for (int i = 0; i < numberOfThreads; i++) {
			if (initialSolutions.isEmpty()) {
				algorithms.add(new MuACO<Instance, MutationType>(config, taskFactory));
			} else {
				System.out.println("Thread # " + i + " is starting with external initial solution");
				algorithms.add(new MuACO<Instance, MutationType>(config, taskFactory, 
						initialSolutions.get(random.nextInt(initialSolutions.size()))));
			}
		}
		System.out.println("Time elapsed for starting threads = " + (System.currentTimeMillis() - start) / 1000.0 + " s.");
	}
	
	protected List<FSM> loadInitialSolutions(String dir) {
		List<FSM> result = new ArrayList<FSM>();
		
		File directory = new File(dir);
		for (String file : directory.list()) {
			try {
				result.add(FSM.loadFromGV(dir + "/" + file, ((AbstractAutomatonTask)task).getDesiredNumberOfStates()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}

	@Override
	public InstanceMetaData<Instance> runAlgorithm() {
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
	
		try {
			System.out.println(algorithms.size());
			InstanceMetaData<Instance> result = executor.invokeAny(algorithms);						
			
			System.out.println("Main: one of threads finished");
			int numberOfFitnessEvaluations = 0;
			for (MuACO<Instance, MutationType> algorithm : algorithms) {
				numberOfFitnessEvaluations += algorithm.getNumberOfFitnessEvaluations();
			}
			System.out.println("Main: shutting down");
			executor.shutdownNow();
			System.out.println("Main: awaiting termination");
			executor.awaitTermination(1, TimeUnit.SECONDS);
			result.setNumberOfFitnessEvaluations(numberOfFitnessEvaluations);
			System.out.println("Main: exiting");
			return result;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
}
