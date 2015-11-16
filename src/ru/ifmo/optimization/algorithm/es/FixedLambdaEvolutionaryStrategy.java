package ru.ifmo.optimization.algorithm.es;

import java.util.Collection;
import java.util.List;

import ru.ifmo.optimization.instance.Checkable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;

public class FixedLambdaEvolutionaryStrategy<Instance extends Checkable<Instance, MutationType>, 
MutationType extends InstanceMutation<Instance>> extends EvolutionaryStrategy<Instance, MutationType> {
	private int lambda;

	public FixedLambdaEvolutionaryStrategy(AbstractTaskFactory<Instance> taskFactory,
			List<Mutator<Instance, MutationType>> mutators, Instance start, int lambda, boolean doUseLazyFitneessCalculation,
			boolean onePlusLambda, int stagnationParameter) {
		super(taskFactory, mutators, start, doUseLazyFitneessCalculation, onePlusLambda, stagnationParameter);
		this.lambda = lambda;
	}

	@Override
	public int lambda(double fitness) {
		return lambda;
	}

	@Override
	public void adaptiveHook(FitInstance<Instance> currentInstance,
			Collection<FitInstance<Instance>> mutants) {
	}
}
