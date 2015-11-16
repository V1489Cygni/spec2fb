package ru.ifmo.optimization.instance.fsm.crossover;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public interface AbstractCrossover<Instance, MutationType extends InstanceMutation<Instance>> {
	List<MutatedInstanceMetaData<Instance, MutationType>> apply(Instance first, Instance second);
}