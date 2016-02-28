package ru.ifmo.optimization.algorithm.rmhc;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class RandomMutationHillClimber<Instance extends Constructable<Instance>, 
			MutationType extends InstanceMutation<Instance>> extends AbstractOptimizationAlgorithm<Instance> {
	protected List<Mutator<Instance, MutationType>> mutators;
	protected Instance start;
	
	public RandomMutationHillClimber(AbstractTaskFactory<Instance> taskFactory, 
			List<Mutator<Instance, MutationType>> mutators, Instance start) {
		super(taskFactory);
		this.mutators = mutators;
		this.start = start;
	}
	
	public RandomMutationHillClimber(AbstractOptimizationTask<Instance> task,
			List<Mutator<Instance, MutationType>> mutators, Instance start) {
		super(task);
		this.mutators = mutators;
		this.start = start;
	}
	
	private MutatedInstanceMetaData<Instance, MutationType> mutateFSM(Instance instance) {
        return mutators.get(ThreadLocalRandom.current().nextInt(mutators.size())).apply(instance);
    }
	
	@Override
	public InstanceMetaData<Instance> runAlgorithm() {
		return run(Integer.MAX_VALUE);
	}
	
	public InstanceMetaData<Instance> run(int maxNumberOfSteps) {
		Instance instance = start;
		double bestFitness = task.getFitInstance(instance).getFitness();
		System.out.println("steps = " + task.getNumberOfFitnessEvaluations() + "; bestFitness = " + bestFitness);
		
		int step = 0;
		while (true) {
			if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
				break;
			}
			
			if (step >= maxNumberOfSteps) {
				break;
			}
			MutatedInstanceMetaData<Instance, MutationType> mutated = mutateFSM(instance);
			double fitness = task.getFitInstance(mutated.getInstance()).getFitness();
			if (fitness > bestFitness) {
				instance = mutated.getInstance();
				bestFitness = fitness;
			} else {				
				step++;
			}
			
			if (fitness > task.getDesiredFitness()) {
				InstanceMetaData<Instance> result = task.getInstanceMetaData(instance);
				result.setNumberOfFitnessEvaluations(task.getNumberOfFitnessEvaluations());
				return result;	
			}
			
			if (step % 100 == 0) {
				System.out.println("steps = " + task.getNumberOfFitnessEvaluations() + "; bestFitness = " + bestFitness);
			}
		}
		InstanceMetaData<Instance> result = task.getInstanceMetaData(instance);
		result.setNumberOfFitnessEvaluations(task.getNumberOfFitnessEvaluations());
		return result;
	}
}
