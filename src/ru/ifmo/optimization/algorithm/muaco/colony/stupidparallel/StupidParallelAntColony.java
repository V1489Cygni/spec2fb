package ru.ifmo.optimization.algorithm.muaco.colony.stupidparallel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.ant.stupidparallel.StupidParallelAnt;
import ru.ifmo.optimization.algorithm.muaco.ant.stupidparallel.StupidParallelAntFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.AntColony;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.pathselector.stupidparallel.MutatedInstanceBundle;
import ru.ifmo.optimization.algorithm.muaco.pathselector.stupidparallel.NewNodeProcessor;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class StupidParallelAntColony <Instance extends Constructable<Instance>, 
		MutationType extends InstanceMutation<Instance>> extends AntColony<Instance, MutationType> {
	
	protected List<StupidParallelAnt<Instance, MutationType>> ants;
	private int numberOfThreads;
	
	public StupidParallelAntColony(SearchGraph<Instance, MutationType> graph,
			List<Node<Instance>> startNodes,
			StupidParallelAntFactory<Instance, MutationType> antFactory, AntStats stats,
			AbstractOptimizationTask<Instance> task, int antStagnationParameter, int numberOfThreads) {
		super(graph, startNodes, antFactory, stats, task, antStagnationParameter);
		ants = new ArrayList<StupidParallelAnt<Instance, MutationType>>();
		int i = 0;
		for (Node<Instance> node: startNodes) {
			ants.add((StupidParallelAnt<Instance, MutationType>) antFactory.createAnt(i++, startNodes.size(), node, graph.getNodeInstance(node), graph));
		}
		this.numberOfThreads = numberOfThreads;
	}

	@Override
	public List<Path> run() {
//		System.out.println("Colony started...");
		List<Path> paths = new ArrayList<Path>();
		int[] antSteps = new int[ants.size()];
		double[] antBestFitness = new double[ants.size()];
		Arrays.fill(antSteps, 0);
		Arrays.fill(antBestFitness, Double.MIN_VALUE);
		
		main: while (true) {
			if (isLimitExceeded()) {
	    		break main;
	    	}
//			System.out.println("Preparing mutants...");
			ArrayList<MutatedInstanceBundle<Instance, MutationType>> mutants = new ArrayList<MutatedInstanceBundle<Instance, MutationType>>();
			for (int i = 0; i < ants.size(); i++) {
				if (antSteps[i] >= antStagnationParameter) {
					continue;
				}
				if (isLimitExceeded()) {
					break main;
				}
				StupidParallelAnt<Instance, MutationType> ant = ants.get(i);
				ant.prepareMutants(stats);
				mutants.addAll(ant.getMutants());
			}
			
//			System.out.println("Doing fitness computation with " + mutants.size() + " mutants...");
			//do all fitness calculation work in parallel
			int nodesPerWorker = Math.max(mutants.size() / numberOfThreads, 1);
	    	int start = 0;
	    	ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
	    	while (start < mutants.size()) {
	    		int finish = Math.min(start + nodesPerWorker, mutants.size());
	    		ArrayList<MutatedInstanceBundle<Instance, MutationType>> data = new ArrayList<MutatedInstanceBundle<Instance, MutationType>>();
	    		for (int j = start; j < finish; j++) {
	    			data.add(mutants.get(j));
	    		}
	    		executor.execute(new NewNodeProcessor(data, task));
	    		start = finish;
	    	}
	    	executor.shutdown();
	    	try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	
//	    	System.out.println("Postprocessing...");
	    	for (int i = 0; i < ants.size(); i++) {
	    		if (antSteps[i] >= antStagnationParameter) {
					continue;
				}
	    		StupidParallelAnt ant = ants.get(i);
	    		boolean foundOptimum = ant.doPostProcessing(stats);
	    		if (foundOptimum) {
					foundGlobalOptimum = true;
					paths.add(ant.getPath());
					return paths;
				}
	    		if (ant.getPath().getCurrentFitness() > antBestFitness[i]) {
					antBestFitness[i] = ant.getPath().getBestFitness();
				} else {
					antSteps[i]++;
				}
	    	}
	    	if (isLimitExceeded()) {
	    		break main;
	    	}
			
			for (int i = 0; i < ants.size(); i++) {
				if (antSteps[i] < antStagnationParameter) {
					continue main;
				}
			}
			break;
		}
		
		for (int i = 0; i < ants.size(); i++) {
			if (ants.get(i).getPath().getEdges().size() > 0) {
				paths.add(ants.get(i).getPath());
			}
		}
		
		System.out.println("Colony finished");
		return paths;
	}
	
}
