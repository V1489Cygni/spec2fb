package ru.ifmo.optimization.algorithm.muaco.ant.stupidparallel;

import java.util.ArrayList;

import ru.ifmo.optimization.algorithm.muaco.ant.AbstractAnt;
import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.pathselector.stupidparallel.MutatedInstanceBundle;
import ru.ifmo.optimization.algorithm.muaco.pathselector.stupidparallel.StupidParallelPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Pair;

public class StupidParallelAnt<Instance extends Constructable<Instance>,
	MutationType extends InstanceMutation<Instance>> extends AbstractAnt<Instance, MutationType> {
	
	protected StupidParallelPathSelector<Instance, MutationType> pathSelector;
	private ArrayList<MutatedInstanceBundle<Instance, MutationType>> mutants = new ArrayList<MutatedInstanceBundle<Instance, MutationType>>();
	private SearchGraph<Instance, MutationType> graph;
	private double localBestFitness = Double.MIN_VALUE;
	
	public StupidParallelAnt(int antId, int numberOfAnts, Node<Instance> start,
			Instance startInstance,
			StupidParallelPathSelector<Instance, MutationType> pathSelector,
			AbstractOptimizationTask<Instance> task,
			PheromoneUpdater<Instance, MutationType> pheromoneUpdater) {
		super(antId, numberOfAnts, start, startInstance, pathSelector, task,
				pheromoneUpdater);
		this.pathSelector = pathSelector;
	}
	
	public StupidParallelAnt(int antId, int numberOfAnts, Node<Instance> start,
			Instance startInstance,
			StupidParallelPathSelector<Instance, MutationType> pathSelector,
			AbstractOptimizationTask<Instance> task,
			PheromoneUpdater<Instance, MutationType> pheromoneUpdater,
			SearchGraph<Instance, MutationType> graph) {
		this(antId, numberOfAnts, start, startInstance, pathSelector, task,
				pheromoneUpdater);
		this.graph = graph;
	}

	@Override
	public boolean step(SearchGraph<Instance, MutationType> graph, AntStats stats) {
		return false;
	}
	
	public void prepareMutants(AntStats stats) {
		mutants.clear();
		Pair<Edge, Instance> p = pathSelector.prepareMutants(graph, node, instance, stats.getBestFitness(), antId, numberOfAnts, mutants);
		if (p != null) {
			Edge edge = p.first;
			if (edge == null || p.second == null) {
				return;
			}
			instance = p.second;
			node = edge.getDest();
			
			node.incrementNumberOfVisits();
			path.add(edge);
		}
	}
	
	public ArrayList<MutatedInstanceBundle<Instance, MutationType>> getMutants() {
		return mutants;
	}
	
	public boolean doPostProcessing(AntStats stats) {
		Edge result = null;
    	Instance resultInstance = null;
		//perform post-processing
    	for (MutatedInstanceBundle<Instance, MutationType> bundle : mutants) {
    		Edge edge = graph.addNode(node, bundle.getMutatedInstanceMetaData().getMutations(), 
    				bundle.getFitInstance(), task.getNumberOfFitnessEvaluations());
    		if (edge.getDest().getFitness() > localBestFitness) {
				result = edge;
				resultInstance = bundle.getMutatedInstanceMetaData().getInstance();
				
				localBestFitness = result.getDest().getFitness();
				if (edge.getDest().getFitness() > stats.getBestFitness()) {
					System.out.println("current = " + edge.getDest().getFitness() + "; best = " + 
						edge.getDest().getFitness() + "; size = " + graph.getNumberOfNodes());
				}
				
				//if the ant found a globally acceptable solution, return it immediately
				if (edge.getDest().getFitness() >= task.getDesiredFitness()) {
					instance = resultInstance;
					node = result.getDest();
					node.incrementNumberOfVisits();
					path.add(result);
					stats.setBest(node, instance, System.currentTimeMillis());
					stats.setLastBestFitnessColonyIterationNumber(stats.getColonyIterationNumber());
					stats.addHistory(task.getNumberOfFitnessEvaluations(), edge.getDest().getFitness());
					return true;
				}
			}
    	}
    	if (result != null) {
    		instance = resultInstance;
    		node = result.getDest();
    		node.incrementNumberOfVisits();
    		path.add(result);
    	}
    	
    	double fitness = node.getFitness();
		if (fitness >= stats.getBestFitness()) {
			if (fitness > stats.getBestFitness()) {
				System.out.println("current = " + fitness + "; best = " + fitness + "; size = " + graph.getNumberOfNodes());
				stats.setBest(node, instance, System.currentTimeMillis());
				stats.setLastBestFitnessColonyIterationNumber(stats.getColonyIterationNumber());
				stats.addHistory(task.getNumberOfFitnessEvaluations(), fitness);
			}
		}
		return false;
	}
}
