package ru.ifmo.optimization.algorithm.muaco.colony.stupidparallel;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.ant.factory.AntFactory;
import ru.ifmo.optimization.algorithm.muaco.ant.stupidparallel.StupidParallelAntFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.AntColony;
import ru.ifmo.optimization.algorithm.muaco.colony.factory.AntColonyFactory;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class StupidParallelAntColonyFactory <Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>>
	extends AntColonyFactory<Instance, MutationType> {

	private int numberOfThreads;
	
	public StupidParallelAntColonyFactory(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}
	
	@Override
	public AntColony<Instance, MutationType> createAntColony(
			SearchGraph<Instance, MutationType> graph,
			List<Node<Instance>> startNodes,
			AntFactory<Instance, MutationType> antFactory, AntStats stats,
			AbstractOptimizationTask<Instance> task, int antStagnationParameter) {
		// TODO Auto-generated method stub
		return new StupidParallelAntColony<Instance, MutationType>(graph, startNodes, (StupidParallelAntFactory<Instance, MutationType>) antFactory, stats, task, antStagnationParameter, numberOfThreads);
	}

}
