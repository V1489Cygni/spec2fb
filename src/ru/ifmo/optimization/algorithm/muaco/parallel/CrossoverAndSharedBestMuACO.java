package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.crossover.AbstractCrossover;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class CrossoverAndSharedBestMuACO<Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends SharedBestMuACO<Instance, MutationType> {

	private AbstractCrossover crossover;
	private int crossoverApplicationPeriod;
	
	public CrossoverAndSharedBestMuACO(
			MuACOConfig<Instance, MutationType> config,
			AbstractOptimizationTask<Instance> task,
			FitInstance<Instance>[] bestThreadInstances, int id) {
		super(config, task, bestThreadInstances, id);
		crossover = config.getCrossover();
		crossoverApplicationPeriod = Integer.parseInt(config.getProperty("crossover-application-period", "1"));
	}
	
	public CrossoverAndSharedBestMuACO(
			MuACOConfig<Instance, MutationType> config,
			AbstractOptimizationTask<Instance> task,
			FitInstance<Instance>[] bestThreadInstances, int id, Instance startInstance) {
		super(config, task, startInstance, bestThreadInstances, id);
		crossover = config.getCrossover();
		crossoverApplicationPeriod = Integer.parseInt(config.getProperty("crossover-application-period", "1"));
	}
	
	protected Edge addChildToBest(Instance instance) {
		List<MutatedInstanceMetaData<Instance, MutationType>> offspring = crossover.apply(stats.getBestInstance(), instance);
		FitInstance<Instance> md0 = task.getFitInstance(offspring.get(0).getInstance());
		FitInstance<Instance> md1 = task.getFitInstance(offspring.get(1).getInstance());
		if (md0.getFitness() > md1.getFitness()) {			
			return graph.addNode(stats.getBestNode(), offspring.get(0).getMutations(), md0, task.getNumberOfFitnessEvaluations());
		}
		
		return graph.addNode(stats.getBestNode(), offspring.get(1).getMutations(), md1, task.getNumberOfFitnessEvaluations());
	}
	
	@Override
	protected List<Node<Instance>> getStartNodes() {
		List<Node<Instance>> startNodes = super.getStartNodes();
		
		if (colonyIterationNumber > 0 && colonyIterationNumber % crossoverApplicationPeriod == 0) {
			int i = ThreadLocalRandom.current().nextInt(bestThreadInstances.length);
			while (i == id || bestThreadInstances[i] == null) {
				i = ThreadLocalRandom.current().nextInt(bestThreadInstances.length);
			}

			startNodes.remove(0);
			Node<Instance> node = addChildToBest(bestThreadInstances[i].getInstance()).getDest();
			System.out.println("Crossover (" + stats.getBestFitness() + ", " + bestThreadInstances[i].getFitness() + ") best child fitness = " + node.getFitness());
			startNodes.add(node);
		}
		
		return startNodes;
		
	}
}