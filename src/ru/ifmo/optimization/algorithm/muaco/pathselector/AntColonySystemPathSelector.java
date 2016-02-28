package ru.ifmo.optimization.algorithm.muaco.pathselector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.pathselector.config.PathSelectorConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Pair;

public class AntColonySystemPathSelector<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>>
				extends HeuristicAntPathSelector<Instance, MutationType> {
	private double greedyChoiceProbability;
	
	public AntColonySystemPathSelector(AbstractOptimizationTask<Instance> task,
			List<Mutator<Instance, MutationType>> mutators, PathSelectorConfig config, 
			AntStats antStats) {
		super(task, mutators, config, antStats);
		greedyChoiceProbability = config.getDoubleProperty("greedy-choice-probability");
	}
	
	@Override
	public Pair<Edge, Instance> nextEdge(SearchGraph<Instance, MutationType> graph, Node<Instance> node,
			Instance instance, double currentBestFitness,
			int antNumber, int numberOfAnts) {
		Pair<Edge, Instance> result = super.nextEdge(graph, node, instance, currentBestFitness, antNumber, numberOfAnts);
		if (result == null) {
			return result;
		}
		if (result.first == null || result.second == null) {
			return result;
		}
		if (ThreadLocalRandom.current().nextDouble() < greedyChoiceProbability) {
			double mutationFitness = result.first.getDest().getFitness();
			Edge bestChild = node.getBestChild();
			if (bestChild.getDest().getFitness() > mutationFitness) {
				return new Pair<Edge, Instance>(bestChild,
						(Instance) graph.getNodeInstance(bestChild.getDest()));
			}
		}
		return result;
	}
	

}
