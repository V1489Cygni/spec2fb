package ru.ifmo.optimization.algorithm.muaco.pathselector.fsm.canonical;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.muaco.pathselector.config.PathSelectorConfig;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Pair;

public class ActiveCanonicalHeuristicAntPathSelector extends FsmHeuristicAntPathSelector {

	public ActiveCanonicalHeuristicAntPathSelector(
			AbstractOptimizationTask<FSM> task,
			List<Mutator<FSM, FsmMutation>> mutators, PathSelectorConfig config,
			AntStats antStats) {
		super(task, mutators, config, antStats);
	}

	
	@Override
	protected Edge dealWithNewNode(SearchGraph<FSM, FsmMutation> graph,
			MutatedInstanceMetaData<FSM, FsmMutation> mutated, FSM instance,
			Node<FSM> node, double currentBestFitness, double localBestFitness) {
		Pair<Node<FSM>, MutatedInstanceMetaData<FSM, FsmMutation>> pair = graph.getPathToIsomorphicNode(mutated.getInstance());
		if (pair != null) {
			MutatedInstanceMetaData<FSM, FsmMutation> newMutated = pair.second;
			Node<FSM> isomorphicNode = pair.first;
			RunStats.N_CACHE_HITS++;
			RunStats.N_CANONICAL_CACHE_HITS++;
			Edge edge = node.getChild(newMutated);
			if (edge == null) {
				edge = graph.addEdge(node, newMutated.getMutations(), isomorphicNode);
			}
			
			return edge;
		}
		return super.dealWithNewNode(graph, mutated, instance, node, currentBestFitness, localBestFitness);
	}

}
