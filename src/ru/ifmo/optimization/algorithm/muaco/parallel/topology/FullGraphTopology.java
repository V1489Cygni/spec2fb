package ru.ifmo.optimization.algorithm.muaco.parallel.topology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.parallel.InteractingAlgorithm;
import ru.ifmo.optimization.instance.Constructable;

public class FullGraphTopology<Instance extends Constructable<Instance>> implements ParallelAlgorithmTopology<Instance> {

	@Override
	public void setTopology(List<InteractingAlgorithm<Instance>> algorithms) {
		for (InteractingAlgorithm<Instance> alg : algorithms) {
			List<InteractingAlgorithm<Instance>> neighbors = new ArrayList<InteractingAlgorithm<Instance>>();
			neighbors.addAll(algorithms);
			neighbors.remove(alg);
			alg.setOtherAlgorithms(neighbors);
		}
	}
}
