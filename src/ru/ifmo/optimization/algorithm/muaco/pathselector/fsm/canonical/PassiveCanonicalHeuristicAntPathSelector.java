package ru.ifmo.optimization.algorithm.muaco.pathselector.fsm.canonical;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.muaco.pathselector.config.PathSelectorConfig;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.CanonicalInstanceData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class PassiveCanonicalHeuristicAntPathSelector extends FsmHeuristicAntPathSelector {

	public PassiveCanonicalHeuristicAntPathSelector(
			AbstractOptimizationTask<FSM> task,
			List<Mutator<FSM, FsmMutation>> mutators, PathSelectorConfig config,
			AntStats antStats) {
		super(task, mutators, config, antStats);
	}
	
	protected FitInstance<FSM> applyFitness(FSM originalInstance, double sourceFitness, 
			MutatedInstanceMetaData<FSM, FsmMutation> mutatedInstanceMetaData, double currentBestFitness, 
			CanonicalInstanceData<Node<FSM>> firstIsomorphicNode, 
			FSM firstIsomorphicInstance, int[] newId2) {
		task.increaseNumberOfAttemptedFitnessEvaluations(1);
		
		if (firstIsomorphicNode == null) {
			return task.getFitInstance(mutatedInstanceMetaData.getInstance());
		}
		
		RunStats.N_CANONICAL_CACHE_HITS++;
		(mutatedInstanceMetaData.getInstance()).transformUsedTransitions(firstIsomorphicNode.getNewId(), newId2, firstIsomorphicInstance);

		
		double resultFitness = firstIsomorphicNode.getData().getFitness();
		double correction = task.correctFitness(resultFitness, firstIsomorphicInstance, mutatedInstanceMetaData.getInstance());
		resultFitness += correction;
		return new FitInstance<FSM>(mutatedInstanceMetaData.getInstance(), resultFitness);
	}

	@Override
	protected Edge dealWithNewNode(SearchGraph<FSM, FsmMutation> graph, 
			MutatedInstanceMetaData<FSM, FsmMutation> mutated, FSM instance, 
			Node<FSM> node, double currentBestFitness, double localBestFitness) {
		int newId[] = new int[instance.getNumberOfStates()];
		CanonicalInstanceData<Node<FSM>> firstIsomorphicNode = graph.getFirstIsomorphicNode(mutated.getInstance(), newId);
		
		FitInstance<FSM> metaData = applyFitness(instance, node.getFitness(), 
				                                 mutated, currentBestFitness, 
				                                 firstIsomorphicNode, 
				                                 firstIsomorphicNode == null ? null : graph.getNodeInstance(firstIsomorphicNode.getData()), 
				                                 newId);
		Edge edge = graph.addNode(node, mutated.getMutations(), metaData, task.getNumberOfFitnessEvaluations());
		return edge;
	}
}
