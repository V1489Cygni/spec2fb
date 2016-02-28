package ru.ifmo.optimization.algorithm.muaco.pathselector;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.muaco.pathselector.config.PathSelectorConfig;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Pair;

public class HeuristicAntPathSelector<Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends AbstractPathSelector<Instance, MutationType> {
	protected double newMutationProbability;	
	
	public HeuristicAntPathSelector(AbstractOptimizationTask<Instance> task, 
			List<Mutator<Instance, MutationType>> mutators, PathSelectorConfig config, 
			AntStats antStats) {
        super(task, mutators, config, antStats);
        numberOfMutationsPerStep = config.getIntProperty("numberOfMutationsPerStep");
        maxNumberOfMutations = task.getNeighborhoodSize();
        newMutationProbability = config.getDoubleProperty("newMutationProbability");
        
        boolean allWeightsAreZero = true;
        double sumMutatorWeight = 0;
        for (Mutator mutator : mutators) {
        	if (mutator.probability() > 0) {
        		sumMutatorWeight += mutator.probability();
        		allWeightsAreZero = false;
        	}
        }
        
        if (allWeightsAreZero) {
        	for (Mutator mutator : mutators) {
        		mutator.setProbability(1.0 / mutators.size());
        	}
        } else {
        	for (Mutator mutator : mutators) {
        		if (mutator.probability() > 0) {
        			mutator.setProbability(mutator.probability() / sumMutatorWeight);
        		}
        	}
        }
	}
	
	@Override
	public Pair<Edge, Instance> nextEdge(
			SearchGraph<Instance, MutationType> graph, Node<Instance> node, 
			Instance instance, double currentBestFitness,
			int antNumber, int numberOfAnts) {
		if ((ThreadLocalRandom.current().nextDouble() < newMutationProbability || !node.hasChildren()) && node.getNumberOfChildren() < maxNumberOfMutations) {
    		return bestMutation(graph, node, instance, currentBestFitness);
    	}
    	Edge edge = rouletteEdgeSelector.select(node.getEdges(), antNumber, numberOfAnts);
    	return new Pair<Edge, Instance>(edge, (Instance) graph.getNodeInstance(edge.getDest()));
	}
	
	protected FitInstance<Instance> applyFitness(Instance originalInstance, double sourceFitness, 
		MutatedInstanceMetaData<Instance, MutationType> mutatedInstanceMetaData, double currentBestFitness) {
		task.increaseNumberOfAttemptedFitnessEvaluations(1);
		return task.getFitInstance(mutatedInstanceMetaData.getInstance());
	}
	
	protected Pair<Edge, Instance> bestMutation(
			SearchGraph<Instance, MutationType> graph, Node<Instance> node, Instance instance, double currentBestFitness) {
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
    			Edge edge = dealWithNewNode(graph, mutated, instance, node, currentBestFitness, localBestFitness);
    			if (edge.getDest().getFitness() > localBestFitness) {
    				result = edge;
    				resultInstance = mutated.getInstance();
    				
    				localBestFitness = result.getDest().getFitness();
    				//if the ant found a globally acceptable solution, return it immediately
    				if (edge.getDest().getFitness() >= task.getDesiredFitness()) {
    					return new Pair<Edge, Instance>(result, resultInstance);
    				}
    			}
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
    	return new Pair<Edge, Instance>(result, resultInstance);
    }
	
	protected MutatedInstanceMetaData<Instance, MutationType> mutateInstance(Instance instance) {
		Mutator<Instance, MutationType> mutator = getMutator();
        MutatedInstanceMetaData<Instance, MutationType> result = mutator.apply(instance);
        return result;
    }
	
	protected Mutator<Instance, MutationType> getMutator() {
		Collections.sort(mutators, new Comparator<Mutator>() {
			@Override
			public int compare(Mutator arg0, Mutator arg1) {
				return Double.compare(arg0.probability(), arg1.probability());
			}
		});
		
		int size = mutators.size();
		double weight[] = new double[size];
		weight[0] = mutators.get(0).probability();

		for (int i = 1; i < size; i++) {
			weight[i] = weight[i - 1] + mutators.get(i).probability();
		}
		double p = weight[size - 1] * ThreadLocalRandom.current().nextDouble();
		int j = 0;

		while (p > weight[j]) {
			j++;
		}
		return mutators.get(j);
	}
	
	protected Edge dealWithNewNode(
			SearchGraph<Instance, MutationType> graph, MutatedInstanceMetaData<Instance, MutationType> mutated,
			Instance instance, Node<Instance> node,
			double currentBestFitness, double localBestFitness) {
		FitInstance<Instance> metaData = applyFitness(instance, node.getFitness(), mutated, currentBestFitness);
		Edge edge = graph.addNode(node, mutated.getMutations(), metaData, task.getNumberOfFitnessEvaluations());
		return edge;
		
	}
}
