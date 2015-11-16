package ru.ifmo.optimization.algorithm.muaco.parallel.topology;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.parallel.InteractingAlgorithm;
import ru.ifmo.optimization.instance.Constructable;

public interface ParallelAlgorithmTopology<Instance extends Constructable<Instance>> {
	void setTopology(List<InteractingAlgorithm<Instance>> algorithms);
}
