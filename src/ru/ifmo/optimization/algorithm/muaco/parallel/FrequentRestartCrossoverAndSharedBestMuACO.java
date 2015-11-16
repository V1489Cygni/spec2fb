package ru.ifmo.optimization.algorithm.muaco.parallel;

import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class FrequentRestartCrossoverAndSharedBestMuACO <Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends CrossoverAndSharedBestMuACO<Instance, MutationType> {

	private int restartPeriod;
	
	public FrequentRestartCrossoverAndSharedBestMuACO(
			MuACOConfig<Instance, MutationType> config,
			AbstractOptimizationTask<Instance> task,
			FitInstance<Instance>[] bestThreadInstances, int id) {
		super(config, task, bestThreadInstances, id);
		this.restartPeriod = Integer.parseInt(config.getProperty("restart-period"));
	}
	
	@Override
	protected boolean doRestart() {
		if (id % 2 == 0 && colonyIterationNumber % restartPeriod == 0 && colonyIterationNumber > 0) {
			for (int i = 0; i < bestThreadInstances.length; i++) {
				if (i == id) {
					continue;
				}
				if (bestThreadInstances[i].getFitness() > bestThreadInstances[id].getFitness()) {
					return true;
				}
			}
		}
		
		return super.doRestart();
	}
	
	@Override
	protected FitInstance<Instance> getInitialSolutionForRestart() {
		FitInstance<Instance> bestFitInstance = bestThreadInstances[0];
		for (int i = 1; i < bestThreadInstances.length; i++) {
			if (i == id) {
				continue;
			}
			if (bestThreadInstances[i].getFitness() > bestFitInstance.getFitness()) {
				bestFitInstance = bestThreadInstances[i];
			}
		}
		return bestFitInstance;
	}
}