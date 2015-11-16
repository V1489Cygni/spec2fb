package ru.ifmo.optimization.algorithm.es;

import java.util.Collection;
import java.util.List;

import ru.ifmo.optimization.instance.Checkable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.landscape.FitnessDistributionCache;
import ru.ifmo.optimization.instance.fsm.landscape.RandomNeighborhoodSampler;
import ru.ifmo.optimization.instance.fsm.landscape.sampler.MetropolisHastingsInstaceSampler;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;


public abstract class AdaptiveEvolutionaryStrategy<Instance extends Checkable<Instance, MutationType>, 
MutationType extends InstanceMutation<Instance>> extends EvolutionaryStrategy<Instance, MutationType> {
	protected FitnessDistributionCache fitnessDistributionCache;
	protected int defaultLambda;
	protected int statisticalThreshold;
	
	public AdaptiveEvolutionaryStrategy(AbstractTaskFactory<Instance> taskFactory,
			List<Mutator<Instance, MutationType>> mutators, Instance start, int initialSampleSize, int defaultLambda, 
			int statisticalThreshold, boolean doUseLazyFitnessCalculation, 
			boolean onePlusLambda, int stagnationParameter) {
		super(taskFactory, mutators, start, doUseLazyFitnessCalculation, onePlusLambda, stagnationParameter);
		this.defaultLambda = defaultLambda;
		this.statisticalThreshold = statisticalThreshold;
		System.out.println("Creating initial fitness sample...");
		fitnessDistributionCache = new RandomNeighborhoodSampler(
				(AbstractAutomatonTask) this.task, new MetropolisHastingsInstaceSampler((AbstractAutomatonTask) this.task), initialSampleSize).getFitnessDistributionCache();
		System.out.println("Created initial fitness sample!");
	}

	@Override
	public void adaptiveHook(FitInstance<Instance> currentInstance,
			Collection<FitInstance<Instance>> mutants) {
		for (FitInstance<Instance> mutant : mutants) {
			fitnessDistributionCache.add(currentInstance.getFitness(), mutant.getFitness());
			fitnessDistributionCache.add(mutant.getFitness(), currentInstance.getFitness());
		}
	}
}
