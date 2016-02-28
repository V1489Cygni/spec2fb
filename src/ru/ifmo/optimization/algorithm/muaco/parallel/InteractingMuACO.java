package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.crossover.AbstractCrossover;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;

public class InteractingMuACO <Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends MuACO<Instance, MutationType> implements
		InteractingAlgorithm<Instance> {

	private List<InteractingAlgorithm<Instance>> otherAlgorithms = new ArrayList<InteractingAlgorithm<Instance>>();
	private List<FitInstance<Instance>> offeredSolutions = new ArrayList<FitInstance<Instance>>();
	private AbstractCrossover crossover;
	
	public InteractingMuACO(MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(config, taskFactory);
		crossover = config.getCrossover();
	}
	
	@Override
	public void setOtherAlgorithms(List<InteractingAlgorithm<Instance>> otherAlgorithms) {
		this.otherAlgorithms = otherAlgorithms;
	}

	@Override
	protected boolean runIteration() {
		boolean result = super.runIteration();
		if (!otherAlgorithms.isEmpty()) {
			offerSolution();
		}
		return result;
	}

	@Override
	public void acceptSolution(FitInstance<Instance> solution) {
		offeredSolutions.add(solution);	
	}

	@Override
	public void offerSolution() {
		FitInstance<Instance> solution = task.getFitInstance(stats.getBestInstance());
		System.out.println("Offering solution with f = " + solution.getFitness());
		otherAlgorithms.get(ThreadLocalRandom.current().nextInt(otherAlgorithms.size())).acceptSolution(solution);
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
		
		if (!offeredSolutions.isEmpty()) {
			startNodes.remove(0);
			FitInstance<Instance> bestOfferedSolution = Collections.max(offeredSolutions);
			offeredSolutions.clear();

			Node<Instance> node = addChildToBest(bestOfferedSolution.getInstance()).getDest();
			System.out.println("Crossover (" + stats.getBestFitness() + ", " + bestOfferedSolution.getFitness() + ") best child fitness = " + node.getFitness());
			startNodes.add(node);
		}
		return startNodes;
	}
}
