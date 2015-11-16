package ru.ifmo.optimization.algorithm.muaco.pathselector.stupidparallel;

import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public class MutatedInstanceBundle<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> {
	private Instance originalInstance;
	private MutatedInstanceMetaData<Instance, MutationType> mutated;
	private FitInstance<Instance> fitInstance;
	
	public MutatedInstanceBundle(MutatedInstanceMetaData<Instance, MutationType> mutated, Instance originalInstance) {
		this.mutated = mutated;
		this.originalInstance = originalInstance;
	}
	
	public MutatedInstanceMetaData<Instance, MutationType> getMutatedInstanceMetaData() {
		return mutated;
	}
	
	public Instance getOriginalInstance() {
		return originalInstance;
	}
	
	public FitInstance<Instance> getFitInstance() { 
		return fitInstance;
	}
	
	public void setFitInstance(FitInstance<Instance> fitInstance) {
		this.fitInstance = fitInstance;
	}
}
