package ru.ifmo.optimization.algorithm.muaco.parallel.topology;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.parallel.InteractingAlgorithm;
import ru.ifmo.optimization.instance.Constructable;

public class AllToFirstTopology<Instance extends Constructable<Instance>> implements ParallelAlgorithmTopology<Instance> {

	@Override
	public void setTopology(List<InteractingAlgorithm<Instance>> algorithms) {
		List<InteractingAlgorithm<Instance>> neighbors = new ArrayList<InteractingAlgorithm<Instance>>();
		neighbors.add(algorithms.get(0));
		for (int i = 1; i < algorithms.size(); i++) {
			algorithms.get(i).setOtherAlgorithms(neighbors);
		}
	}

}
