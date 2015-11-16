package ru.ifmo.optimization.algorithm.muaco.heuristicdist;

import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.instance.Constructable;

public interface HeuristicDistance<Instance extends Constructable<Instance>> {
	double getHeuristicDistance(Node<Instance> from, Node<Instance> to);
}
