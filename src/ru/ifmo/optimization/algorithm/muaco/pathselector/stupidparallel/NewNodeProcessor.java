package ru.ifmo.optimization.algorithm.muaco.pathselector.stupidparallel;

import java.util.ArrayList;

import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class NewNodeProcessor<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> implements Runnable {

	private ArrayList<MutatedInstanceBundle<Instance, MutationType>> data;
	private AbstractOptimizationTask<Instance> task;
	
	public NewNodeProcessor(ArrayList<MutatedInstanceBundle<Instance, MutationType>> data, AbstractOptimizationTask<Instance> task) {
		this.data = data;
		this.task = task;
	}
	
	@Override
	public void run() {
		for (MutatedInstanceBundle<Instance, MutationType> bundle : data) {
			bundle.setFitInstance(task.getFitInstance(bundle.getMutatedInstanceMetaData().getInstance()));
		}
	}
	
	protected FitInstance<Instance> applyFitness(Instance originalInstance, double sourceFitness, 
			MutatedInstanceMetaData<Instance, MutationType> mutatedInstanceMetaData, double currentBestFitness) {
		return task.getFitInstance(mutatedInstanceMetaData.getInstance());
	}
}
