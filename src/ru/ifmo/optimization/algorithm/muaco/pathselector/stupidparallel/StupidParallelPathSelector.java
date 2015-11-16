package ru.ifmo.optimization.algorithm.muaco.pathselector.stupidparallel;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.muaco.pathselector.HeuristicAntPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pathselector.config.PathSelectorConfig;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.random.RandomProvider;
import ru.ifmo.util.Pair;

public class StupidParallelPathSelector<Instance extends Constructable<Instance>, 
		MutationType extends InstanceMutation<Instance>> extends HeuristicAntPathSelector<Instance, MutationType> {

	public StupidParallelPathSelector(AbstractOptimizationTask<Instance> task,
			List<Mutator<Instance, MutationType>> mutators,
			PathSelectorConfig config, AntStats antStats) {
		super(task, mutators, config, antStats);
		// TODO Auto-generated constructor stub
	}

	public Pair<Edge, Instance> prepareMutants(
			SearchGraph<Instance, MutationType> graph, Node<Instance> node, 
			Instance instance, double currentBestFitness,
			int antNumber, int numberOfAnts, ArrayList<MutatedInstanceBundle<Instance, MutationType>> mutants) {
		if ((RandomProvider.getInstance().nextDouble() < newMutationProbability || !node.hasChildren()) && node.getNumberOfChildren() < maxNumberOfMutations) {
			mutants.addAll(makeMutants(graph, node, instance, currentBestFitness));
			return null;
    	}
    	Edge edge = rouletteEdgeSelector.select(node.getEdges(), antNumber, numberOfAnts);
    	return new Pair<Edge, Instance>(edge, (Instance) graph.getNodeInstance(edge.getDest()));
	}
	
	private ArrayList<MutatedInstanceBundle<Instance, MutationType>> makeMutants(SearchGraph<Instance, MutationType> graph, Node<Instance> node, 
			Instance instance, double currentBestFitness) {
		ArrayList<MutatedInstanceBundle<Instance, MutationType>> mutants = new ArrayList<MutatedInstanceBundle<Instance, MutationType>>();
		double localBestFitness = Double.MIN_VALUE;
    	Edge result = null;
    	Instance resultInstance = null;
    	int numberOfMutationsToMake = Math.min(numberOfMutationsPerStep, maxNumberOfMutations - node.getChildren().size());
    	for (int i = 0; i < numberOfMutationsToMake; i++) {
    		MutatedInstanceMetaData<Instance, MutationType> mutated = mutateInstance(instance);
    		if (mutated == null) {
    			continue;
    		}
    		Node<Instance> old = graph.getNode(mutated.getInstance());
    		
    		//if this is a new node
    		if (old == null) {
    			mutants.add(new MutatedInstanceBundle(mutated, instance));
    		} else {
    			RunStats.N_CACHE_HITS++;
    			//if this is an old node
    			Edge edge = node.getChild(mutated);
    			if (edge == null) {
    				edge = graph.addEdge(node, mutated.getMutations(), old);
    			}
    			if (old.getFitness() >= localBestFitness) {
    				result = edge;
    				resultInstance = (Instance) graph.getNodeInstance(old);
    				localBestFitness = old.getFitness();
    			}
    		}
    	}
    	return mutants;
	}
}
