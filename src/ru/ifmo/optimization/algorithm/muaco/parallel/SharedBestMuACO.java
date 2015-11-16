package ru.ifmo.optimization.algorithm.muaco.parallel;

import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.random.RandomProvider;

public class SharedBestMuACO<Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends MuACO<Instance, MutationType> {

	protected int id;
	protected FitInstance<Instance> bestThreadInstances[];
	protected boolean restartWithBest;
	
	public SharedBestMuACO(MuACOConfig<Instance, MutationType> config, AbstractOptimizationTask<Instance> task, 
			 FitInstance<Instance> bestThreadInstances[], int id) {
		super(config, task);
		this.bestThreadInstances = bestThreadInstances;
		this.id = id;
		System.out.println("This is SharedBestParallelMuACO, id=" + id);
		bestThreadInstances[id] = new FitInstance<Instance>(stats.getBestInstance(), stats.getBestFitness());
		this.restartWithBest = Boolean.parseBoolean(config.getProperty("restart-with-best"));
	}
	
	public SharedBestMuACO(MuACOConfig<Instance, MutationType> config, AbstractOptimizationTask<Instance> task, 
			Instance startInstance, FitInstance<Instance> bestThreadInstances[], int id) {
		super(config, task, startInstance);
		this.bestThreadInstances = bestThreadInstances;
		this.id = id;
		System.out.println("This is SharedBestParallelMuACO, id=" + id);
		bestThreadInstances[id] = new FitInstance<Instance>(stats.getBestInstance(), stats.getBestFitness());
		this.restartWithBest = Boolean.parseBoolean(config.getProperty("restart-with-best"));
	}
	
	@Override
	protected void updateBestNode(Instance instance, double fitnessValue) {
		if (fitnessValue > bestThreadInstances[id].getFitness()) {
			bestThreadInstances[id] = new FitInstance<Instance>(instance, fitnessValue);
		}
	}
	
	@Override
	protected FitInstance<Instance> getInitialSolutionForRestart() {
		if (RandomProvider.getInstance().nextBoolean()) {
			int i = id;
			if (!restartWithBest) {
				i = RandomProvider.getInstance().nextInt(bestThreadInstances.length);
				while (i == id) {
					i = RandomProvider.getInstance().nextInt(bestThreadInstances.length);
				}
			}
			System.out.println("Algorithm " + id + " restarting with best solution of algorithm " + i);
			FitInstance<Instance> newStart = new FitInstance<Instance>(
					bestThreadInstances[i].getInstance(), 
					bestThreadInstances[i].getFitness() / 2.0);
			return newStart;
		}
		FitInstance<Instance> newStart = task.getFitInstance(randomInstance());
		bestThreadInstances[id] = newStart;
		return newStart;
	}
	
	@Override
	public InstanceMetaData<Instance> runAlgorithm() {
		InstanceMetaData<Instance> result =  super.runAlgorithm();
		System.out.println("Algorithm " + id + " found solution with f = " + result.getFitness());
		return result;
	}
}
