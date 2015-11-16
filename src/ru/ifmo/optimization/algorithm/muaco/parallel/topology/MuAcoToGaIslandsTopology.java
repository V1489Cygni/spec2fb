package ru.ifmo.optimization.algorithm.muaco.parallel.topology;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.parallel.InteractingAlgorithm;
import ru.ifmo.optimization.instance.Constructable;

public class MuAcoToGaIslandsTopology<Instance extends Constructable<Instance>> implements ParallelAlgorithmTopology<Instance> {
	
	private int numberOfGas;
	
	public MuAcoToGaIslandsTopology(int numberOfGas) {
		this.numberOfGas = numberOfGas;
	}
	
	@Override
	public void setTopology(List<InteractingAlgorithm<Instance>> algorithms) {
		int numberOfMuAcosPerGa = (algorithms.size() - numberOfGas) / numberOfGas;

		int ga = 0;
		for (int i = numberOfGas; i < algorithms.size(); i+=numberOfMuAcosPerGa) {
			for (int j = i; j < i + numberOfMuAcosPerGa; j++) {
				List<InteractingAlgorithm<Instance>> neighbors = new ArrayList<InteractingAlgorithm<Instance>>();
				neighbors.add(algorithms.get(ga));
				algorithms.get(j).setOtherAlgorithms(neighbors);
			}
			ga++;
		}
	}


}
