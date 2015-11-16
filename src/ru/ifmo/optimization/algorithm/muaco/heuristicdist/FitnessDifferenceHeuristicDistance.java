package ru.ifmo.optimization.algorithm.muaco.heuristicdist;

import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.instance.Constructable;

public class FitnessDifferenceHeuristicDistance<Instance extends Constructable<Instance>> implements HeuristicDistance<Instance>{

	@Override
	public double getHeuristicDistance(Node<Instance> from, Node<Instance> to) {
		return Math.max(0.001, to.getFitness() - from.getFitness());
	}
}
