package ru.ifmo.optimization.algorithm.muaco;

import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class SmallGraphMuACO <Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends MuACO<Instance, MutationType> {

	private int graphRenewPeriod;
	
	public SmallGraphMuACO(MuACOConfig<Instance, MutationType> config,
			AbstractOptimizationTask<Instance> task) {
		super(config, task);
		graphRenewPeriod = Integer.parseInt(config.getProperty("graph-renew-period"));
	}
	
	public SmallGraphMuACO(MuACOConfig<Instance, MutationType> config,
			AbstractOptimizationTask<Instance> task, Instance initialSolution) {
		super(config, task, initialSolution);
		graphRenewPeriod = Integer.parseInt(config.getProperty("graph-renew-period"));
	}
	
	public SmallGraphMuACO(MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(config, taskFactory);
		graphRenewPeriod = Integer.parseInt(config.getProperty("graph-renew-period"));
	}
	
	@Override
	public boolean runIteration() {
		if (colonyIterationNumber > 0 && colonyIterationNumber % graphRenewPeriod == 0) {
			//dispose of current graph
			System.out.println("Disposing of current graph...");
			bestPath.clear();
			graph.clear();
			task.reset();
			
			FitInstance<Instance> md =  task.getFitInstance(stats.getBestInstance());
			graph = config.getSearchGraph(pheromoneUpdater, config.getHeuristicDistance(), md);
			stats.setBest(graph.getNode(md.getInstance()), md.getInstance(), System.currentTimeMillis());
		}
		return super.runIteration();
	}

}
