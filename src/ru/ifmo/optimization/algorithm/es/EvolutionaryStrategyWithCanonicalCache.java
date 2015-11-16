package ru.ifmo.optimization.algorithm.es;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.CanonicalInstancesCache;
import ru.ifmo.optimization.instance.Checkable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.CanonicalInstanceData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;

public class EvolutionaryStrategyWithCanonicalCache<Instance extends Checkable<Instance, MutationType>, 
MutationType extends InstanceMutation<Instance>> extends FixedLambdaEvolutionaryStrategy<Instance, MutationType> {

	private CanonicalInstancesCache<Instance, CanonicalInstanceData<FitInstance<Instance>>> canonicalInstancesCache;
	
	public EvolutionaryStrategyWithCanonicalCache(
			AbstractTaskFactory<Instance> taskFactory, List<Mutator<Instance, MutationType>> mutators,
			Instance start, int lambda, boolean doUseLazyFitneessCalculation,
			boolean onePlusLambda, int stagnationParameter,
			int maxCanonicalCacheSize) {
		super(taskFactory, mutators, start, lambda, doUseLazyFitneessCalculation,
				onePlusLambda, stagnationParameter);
		canonicalInstancesCache = new CanonicalInstancesCache<Instance, CanonicalInstanceData<FitInstance<Instance>>>(maxCanonicalCacheSize);
	}
	
	@Override
	protected FitInstance<Instance> applyFitness(Instance instance, double sourceFitness, Instance mutatedInstance, 
			MutatedInstanceMetaData<Instance, MutationType> mutatedInstanceMetaData) {
		task.increaseNumberOfAttemptedFitnessEvaluations(1);
		
		if (mutatedInstanceMetaData == null) {
			FitInstance<Instance> fitInstance = task.getFitInstance(mutatedInstance);
			return new FitInstance<Instance>(fitInstance.getInstance(), fitInstance.getFitness());
		}
		
		if (doUseLazyFitnessCalculation) {
			if (!instance.needToComputeFitness(mutatedInstanceMetaData.getMutations())) {
				RunStats.N_SAVED_EVALS_LAZY++;
				Instance originalInstanceCopy = instance.copyInstance(instance);
				mutatedInstance.setFitnessDependentData(originalInstanceCopy.getFitnessDependentData());
				return new FitInstance<Instance>(mutatedInstance, sourceFitness);
			}
		}
		FSM fsmInstance = (FSM)instance;
		
		//try with canonical cache
		int newId[] = new int[fsmInstance.getNumberOfStates()];		
		Instance canonicalInstance = (Instance) ((FSM)mutatedInstance).getCanonicalFSM(newId);
		CanonicalInstanceData<FitInstance<Instance>> newResult = canonicalInstancesCache.getFirstNonCanonicalInstance(canonicalInstance);
		
		//if the canonical instances cache does not contain a FSM isomorphic to mutatedInstance
		if (newResult == null) {
			FitInstance<Instance> result = task.getFitInstance(mutatedInstance);
			canonicalInstancesCache.add(new CanonicalInstanceData<FitInstance<Instance>>(result, newId), canonicalInstance);
			return result;
		} else {
			RunStats.N_CANONICAL_CACHE_HITS++;
			((FSM)mutatedInstance).transformUsedTransitions(newResult.getNewId(), newId, (FSM)newResult.getData().getInstance());
			
//			FitInstance<FSM> otherInstance = task.getFitInstance(mutatedInstance.copyInstance(mutatedInstance), 0);
//			for (int i = 0; i < mutatedInstance.getNumberOfStates(); i++) {
//				for (int j = 0; j < mutatedInstance.getNumberOfEvents(); j++) {
//					if (mutatedInstance.isTransitionUsed(i, j) != otherInstance.getInstance().isTransitionUsed(i, j)) {
//						System.err.println("Wrong");
//						System.exit(1);
//					}
//				}
//			}
			
			return new FitInstance<Instance>(mutatedInstance, newResult.getData().getFitness());
			
			
			
//			FitInstance<FSM> trueResult = task.getFitInstance(mutatedInstance, 0);
//			if (Math.abs(result.getFitness() - trueResult.getFitness()) > 1e-6) {
//				System.err.println("Wrong canonization result: canonical fitness=" + result.getFitness() + "; true fitness=" + trueResult.getFitness());
//				System.exit(1);
//			}
//			if (!newResult.getData().getInstance().getCanonicalFSM().computeStringHash().equals(canonicalInstance.computeStringHash())) {
//				System.err.println("Canonical FSMs are not equal!");
//				System.exit(1);
//			}
//			return result;
		}
	}

}
