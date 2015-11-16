package ru.ifmo.optimization.instance;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public interface Checkable<Instance, MutationType extends InstanceMutation<Instance>> extends Constructable<Instance>{
	boolean needToComputeFitness(MutationCollection<MutationType> mutationCollection);
}
