package ru.ifmo.optimization.algorithm.muaco.startnodesselector;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public class GlobalRouletteStartNodesSelector<Instance extends Constructable<Instance>, 
				MutationType extends InstanceMutation<Instance>> extends StartNodesSelector<Instance, MutationType> {
	@Override
	public List<Node<Instance>> getStartNodes(SearchGraph<Instance, MutationType> graph, int numberOfAnts,
			List<Path> paths, Path bestPath, Path iterationBestPath) {
//		return graph.getNodesWithRoulette(numberOfAnts);
		return null;
	}
}
