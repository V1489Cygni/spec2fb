package ru.ifmo.optimization.algorithm.muaco.parallel.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.parallel.InteractingAlgorithm;
import ru.ifmo.optimization.instance.Constructable;

public class RingTopology<Instance extends Constructable<Instance>> implements ParallelAlgorithmTopology<Instance> {

	@Override
	public void setTopology(List<InteractingAlgorithm<Instance>> algorithms) {
		Collections.shuffle(algorithms);
		for (int i = 0; i < algorithms.size() - 1; i++) {
			List<InteractingAlgorithm<Instance>> neighbors = new ArrayList<InteractingAlgorithm<Instance>>();
			neighbors.add(algorithms.get(i + 1));
			algorithms.get(i).setOtherAlgorithms(neighbors);
		}
		List<InteractingAlgorithm<Instance>> neighbors = new ArrayList<InteractingAlgorithm<Instance>>();
		neighbors.add(algorithms.get(0));
		algorithms.get(algorithms.size() - 1).setOtherAlgorithms(neighbors);
	}

}
