package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.ifmo.bool.ComplianceChecker;
import ru.ifmo.ctddev.genetic.transducer.io.FileFormatException;
import ru.ifmo.ctddev.genetic.transducer.io.ITestsReader;
import ru.ifmo.ctddev.genetic.transducer.io.OneGroupTestsReader;
import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.algorithm.muaco.ga.InteractingGA;
import ru.ifmo.optimization.algorithm.muaco.parallel.topology.AllToFirstTopology;
import ru.ifmo.optimization.algorithm.muaco.parallel.topology.FullGraphTopology;
import ru.ifmo.optimization.algorithm.muaco.parallel.topology.MuAcoToGaIslandsTopology;
import ru.ifmo.optimization.algorithm.muaco.parallel.topology.MuAcoToGaTopology;
import ru.ifmo.optimization.algorithm.muaco.parallel.topology.ParallelAlgorithmTopology;
import ru.ifmo.optimization.algorithm.muaco.parallel.topology.RingTopology;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;

public class ParallelMuAcoGeneticAlgorithm <Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends AbstractOptimizationAlgorithm<Instance> {
	
	private enum Topology {
		FULL_GRAPH,
		RING,
		ALL_TO_FIRST,
		MUACO_TO_GA,
		MUACO_TO_GA_ISLANDS
	}
	
	private ITestsReader reader = null;
	private MuACOConfig<Instance, MutationType> config;
	private AbstractTaskFactory<Instance> taskFactory;
	protected int numberOfThreads;
	private List<InteractingAlgorithm<Instance>> gas = new ArrayList<InteractingAlgorithm<Instance>>();
	private List<InteractingAlgorithm<Instance>> muacos = new ArrayList<InteractingAlgorithm<Instance>>();
	List<InteractingAlgorithm<Instance>> algorithms = new ArrayList<InteractingAlgorithm<Instance>>();
	
	public ParallelMuAcoGeneticAlgorithm(
			AbstractTaskFactory<Instance> taskFactory) {
		super(taskFactory);
		this.taskFactory = taskFactory;
		
		config = new MuACOConfig<Instance, MutationType>("muaco.properties");
		
		int numberOfGAs = Integer.parseInt(config.getProperty("number-of-gas"));
		numberOfThreads = config.getNumberOfThreads();
		
		if (numberOfGAs > numberOfThreads) {
			throw new RuntimeException("Number of GAs (" + numberOfGAs + ") is greater than the total number of threads (" + numberOfThreads + ")");
		}			
		 
		try {
			reader = new OneGroupTestsReader(new File("problem.xml"), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			ComplianceChecker.createComplianceChecker(FSM.EVENTS);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < numberOfGAs; i++) {
			gas.add(createGeneticAlgorithm());
		}
		
		for (int i = numberOfGAs; i < numberOfThreads; i++) {
			muacos.add(createMuACO());
		}
		
		algorithms.addAll(gas);
		algorithms.addAll(muacos);
		
		Topology topologyType = Topology.valueOf(config.getProperty("topology"));
		ParallelAlgorithmTopology<Instance> topology = null;
		switch (topologyType) {
		case FULL_GRAPH:
			topology = new FullGraphTopology<Instance>();
			break;
		case RING:
			topology = new RingTopology<Instance>();
			break;
		case ALL_TO_FIRST:
			topology = new AllToFirstTopology<Instance>();
			break;
		case MUACO_TO_GA:
			topology = new MuAcoToGaTopology<Instance>(gas.size());
			break;
		case MUACO_TO_GA_ISLANDS:
			topology = new MuAcoToGaIslandsTopology<Instance>(gas.size());
			break;
		}

		topology.setTopology(algorithms);
	}

	protected InteractingAlgorithm<Instance> createGeneticAlgorithm() {
		int migrationPeriod = Integer.parseInt(config.getProperty("migration-period", "1"));
		return new InteractingGA<Instance>(reader.getAlgorithmParameters(), reader.getSetOfInputs(), 
				reader.getSetOfOutputs(), reader.getGroups(), 
				-1, -1, false, false, 1, taskFactory, migrationPeriod);
	}
	
	protected InteractingAlgorithm<Instance> createMuACO() {
		return new InteractingMuACO<Instance, MutationType>(config, taskFactory);
	}


	@Override
	public InstanceMetaData<Instance> runAlgorithm() {
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
		
		try {
			System.out.println(algorithms.size());
			InstanceMetaData<Instance> result = executor.invokeAny(algorithms);
			System.out.println("Main: one of threads finished");
			int numberOfFitnessEvaluations = 0;
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
