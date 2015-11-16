package ru.ifmo.optimization.algorithm.muaco.ant.current;

import ru.ifmo.optimization.algorithm.muaco.ant.AbstractAnt;
import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.pathselector.AbstractPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Pair;

public class CurrentAnt<Instance extends Constructable<Instance>,
				MutationType extends InstanceMutation<Instance>> extends AbstractAnt<Instance, MutationType> {
	public CurrentAnt(int antId, int numberOfAnts, Node<Instance> start, 
			Instance startInstance, 
			AbstractPathSelector<Instance, MutationType> pathSelector, 
			AbstractOptimizationTask<Instance> task,
			PheromoneUpdater<Instance, MutationType> pheromoneUpdater) {
		super(antId, numberOfAnts, start, startInstance, pathSelector, task, pheromoneUpdater);
	}

	@Override
	public boolean step(SearchGraph<Instance, MutationType> graph, AntStats stats) {
		if (isLimitExceeded()) {
			return false;
		}
		Pair<Edge, Instance> p = pathSelector.nextEdge(graph, node, instance, stats.getBestFitness(), antId, numberOfAnts);
		
		if (p == null) {
			stopNow = true;
			return false;
		}
		Edge edge = p.first;
		if (edge == null || p.second == null) {
			return false;
		}
		instance = p.second;
		node = edge.getDest();
		
		node.incrementNumberOfVisits();
		path.add(edge);
		
		double fitness = node.getFitness();
		if (fitness >= stats.getBestFitness()) {
			if (fitness > stats.getBestFitness()) {
				System.out.println("current = " + fitness + "; best = " + fitness + "; size = " + graph.getNumberOfNodes());
				stats.setBest(node, instance, System.currentTimeMillis());
				stats.setLastBestFitnessColonyIterationNumber(stats.getColonyIterationNumber());
				stats.addHistory(task.getNumberOfFitnessEvaluations(), fitness);
			}
			if (fitness >= task.getDesiredFitness()) {
				return true;
			}
		}
		return false;
	}
}
