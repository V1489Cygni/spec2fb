package ru.ifmo.optimization.algorithm.muaco.pathselector.fsm.canonical;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.muaco.pathselector.config.PathSelectorConfig;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.CanonicalInstanceData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class LazyPassiveCanonicalHeuristicAntPathSelector extends PassiveCanonicalHeuristicAntPathSelector {

	public LazyPassiveCanonicalHeuristicAntPathSelector(
			AbstractOptimizationTask<FSM> task,
			List<Mutator<FSM, FsmMutation>> mutators, PathSelectorConfig config,
			AntStats antStats) {
		super(task, mutators, config, antStats);
	}

	@Override
	protected FitInstance<FSM> applyFitness(FSM originalInstance, double sourceFitness,
			MutatedInstanceMetaData<FSM, FsmMutation> mutatedInstanceMetaData, double currentBestFitness, 
			CanonicalInstanceData<Node<FSM>> firstIsomorphicNode, 
			FSM firstInstance, int newId2[]) {
		task.increaseNumberOfAttemptedFitnessEvaluations(1);
		//if a used transition was changed
		if (originalInstance.needToComputeFitness(mutatedInstanceMetaData.getMutations())) {
			//try going with canonical FSM fitness value
			if (firstIsomorphicNode == null) {
				//if we don't have an FSM isomorphic to the current one, calculate fitness value
				return task.getFitInstance(mutatedInstanceMetaData.getInstance());
			}
			RunStats.N_CANONICAL_CACHE_HITS++;
			//else return the fitness of the first isomorphic FSM
			mutatedInstanceMetaData.getInstance().transformUsedTransitions(firstIsomorphicNode.getNewId(), newId2, firstInstance);
			return new FitInstance<FSM>(mutatedInstanceMetaData.getInstance(), firstIsomorphicNode.getData().getFitness());
		}
		//if no used transition was changed, return the fitness value of the original FSM
		RunStats.N_SAVED_EVALS_LAZY++;
		FSM originalInstanceCopy = originalInstance.copyInstance(originalInstance);
		mutatedInstanceMetaData.getInstance().setFitnessDependentData(originalInstanceCopy.getFitnessDependentData());
		return new FitInstance<FSM>(mutatedInstanceMetaData.getInstance(), sourceFitness);	
	}
}
