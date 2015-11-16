package ru.ifmo.optimization.algorithm.muaco.ant.stupidparallel;

import ru.ifmo.optimization.algorithm.muaco.ant.AbstractAnt;
import ru.ifmo.optimization.algorithm.muaco.ant.config.AntConfig;
import ru.ifmo.optimization.algorithm.muaco.ant.factory.AntFactory;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.pathselector.AbstractPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pathselector.stupidparallel.StupidParallelPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class StupidParallelAntFactory <Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> 
	extends AntFactory<Instance, MutationType> {

	public StupidParallelAntFactory(AntConfig antConfig,
			AbstractOptimizationTask<Instance> task,
			PheromoneUpdater<Instance, MutationType> pheromoneUpdater) {
		super(antConfig, task, pheromoneUpdater);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractAnt<Instance, MutationType> createAnt(int antId,
			int numberOfAnts, Node<Instance> start, Instance startInstance) {
		return new StupidParallelAnt<Instance, MutationType>(antId, numberOfAnts, start, startInstance, (StupidParallelPathSelector<Instance, MutationType>) antConfig.getPathSelector(), task, pheromoneUpdater);
	}
	
	public AbstractAnt<Instance, MutationType> createAnt(int antId,
			int numberOfAnts, Node<Instance> start, Instance startInstance, SearchGraph<Instance, MutationType> graph) {
		return new StupidParallelAnt<Instance, MutationType>(antId, numberOfAnts, start, startInstance, (StupidParallelPathSelector<Instance, MutationType>) antConfig.getPathSelector(), task, pheromoneUpdater, graph);
	}

	@Override
	public AbstractAnt<Instance, MutationType> createAnt(int antId,
			int numberOfAnts, Node<Instance> start,
			AbstractPathSelector<Instance, MutationType> pathSelector,
			Instance startInstance) {
		return new StupidParallelAnt<Instance, MutationType>(antId, numberOfAnts, start, startInstance, (StupidParallelPathSelector<Instance, MutationType>) pathSelector, task, pheromoneUpdater);
	}

}
