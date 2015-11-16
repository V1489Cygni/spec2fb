package ru.ifmo.optimization.algorithm.es;

import java.util.List;

import ru.ifmo.optimization.instance.Checkable;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.landscape.CachedFitnessData;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class AdaptiveIncreaseProbabilityEvolutionaryStrategy<Instance extends Checkable<Instance, MutationType>, 
MutationType extends InstanceMutation<Instance>> extends AdaptiveEvolutionaryStrategy<Instance, MutationType> {
	
	private final int maxLambda;
	private int numberOfCalls = 0;
	private int numberOfDefaultCalls = 0;
	
	public AdaptiveIncreaseProbabilityEvolutionaryStrategy(
			AbstractTaskFactory<Instance> taskFactory, List<Mutator<Instance, MutationType>> mutators,
			Instance start, int lambda, int initialSampleSize, int defaultLambda, int statisticalThreshold, 
			boolean doUseLazyFitnessCalculation, boolean onePlusLambda, int stagnationParameter) {
		super(taskFactory, mutators, start, initialSampleSize, defaultLambda, statisticalThreshold, 
				doUseLazyFitnessCalculation, onePlusLambda, stagnationParameter);
		maxLambda = (int)Math.sqrt(task.getNeighborhoodSize());
	}

	@Override
	public int lambda(double fitness) {
		numberOfCalls++;
		if (fitnessDistributionCache.contains(fitness)) {
			CachedFitnessData cfd = fitnessDistributionCache.get(fitness);
			if (cfd.getNeighbourCount() >= statisticalThreshold) {
				return Math.max(1, (int)(task.getNeighborhoodSize() * cfd.getIncreaseProbability()));
			} 
		}		
		numberOfDefaultCalls++;
		return defaultLambda;
	}
	
	@Override
	public String toString() {
		return "defaultCallsRatio=" + (double)numberOfDefaultCalls / (double)numberOfCalls;
	}
}
