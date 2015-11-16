package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.util.List;
import java.util.concurrent.Callable;

import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;

public interface InteractingAlgorithm<Instance extends Constructable<Instance>> extends Callable<InstanceMetaData<Instance>>{
	void offerSolution();
	void acceptSolution(FitInstance<Instance> solution);
	void setOtherAlgorithms(List<InteractingAlgorithm<Instance>> otherAlgorithms);
}
