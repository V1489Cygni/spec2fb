package ru.ifmo.optimization.algorithm.muaco.parallel.topology;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.parallel.InteractingAlgorithm;
import ru.ifmo.optimization.instance.Constructable;

public class MuAcoToGaTopology<Instance extends Constructable<Instance>> implements ParallelAlgorithmTopology<Instance> {

	private int numberOfGas;
	
	public MuAcoToGaTopology(int numberOfGas) {
		this.numberOfGas = numberOfGas;
	}
	
	@Override
	public void setTopology(List<InteractingAlgorithm<Instance>> algorithms) {
		List<InteractingAlgorithm<Instance>> neighbors = new ArrayList<InteractingAlgorithm<Instance>>();
		
		for (int i = 0; i < numberOfGas; i++) {
			neighbors.add(algorithms.get(i));
		}
		
		for (int i = numberOfGas; i < algorithms.size(); i++) {
			algorithms.get(i).setOtherAlgorithms(neighbors);
		}
	}

}
