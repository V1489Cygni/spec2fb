package ru.ifmo.optimization.algorithm.genetic;

import ru.ifmo.optimization.algorithm.genetic.config.GeneticAlgorithmConfig;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.CanonicalInstancesCache;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;

public class FsmGeneticAlgorithm extends GeneticAlgorithm<FSM, FsmMutation> {

	private CanonicalInstancesCache<FSM, CanonicalCachedData> canonicalInstancesCache;
	
	private class CanonicalCachedData {
		private FitInstance<FSM> instance;
		private int[] newId;
		
		public CanonicalCachedData(FitInstance<FSM> instance, int[] newId) {
			this.instance = instance;
			this.newId = newId;
		}
		
		public FitInstance<FSM> getFitInstance() {
			return instance;
		}
		
		public int[] getNewId() {
			return newId;
		}
	}
	
	public FsmGeneticAlgorithm(GeneticAlgorithmConfig config, AbstractTaskFactory<FSM> taskFactory) {
		super(config, taskFactory);
		canonicalInstancesCache = new CanonicalInstancesCache<FSM, CanonicalCachedData>(config.getMaxCanonicalCacheSize());
		// TODO Auto-generated constructor stub
	}

	@Override
	protected FitInstance<FSM> applyFitness(FSM individual, double sourceFitness, FSM mutatedInstance,
			MutatedInstanceMetaData<FSM, FsmMutation> mutatedInstanceMetaData) {
		if (mutatedInstanceMetaData == null) {
    		FitInstance<FSM> fitInstance = task.getFitInstance(mutatedInstance);
    		return new FitInstance<FSM>(fitInstance.getInstance(), fitInstance.getFitness());
    	}
		
    	if (individual.needToComputeFitness(mutatedInstanceMetaData.getMutations()) || !doUseLazyFitnessCalculation) {
    		int newId[] = new int[mutatedInstance.getNumberOfStates()];
    		FSM canonicalInstance = mutatedInstance.getCanonicalFSM(newId);
    		if (canonicalInstancesCache.contains(canonicalInstance)) {
    			RunStats.N_CANONICAL_CACHE_HITS++;
    			CanonicalCachedData data = canonicalInstancesCache.getFirstNonCanonicalInstance(canonicalInstance);
    			mutatedInstanceMetaData.getInstance().transformUsedTransitions(data.getNewId(), newId, data.getFitInstance().getInstance());
    			return new FitInstance<FSM>(mutatedInstanceMetaData.getInstance(), data.getFitInstance().getFitness());
    		} else {
    			FitInstance<FSM> result = task.getFitInstance(mutatedInstanceMetaData.getInstance());
    			canonicalInstancesCache.add(new CanonicalCachedData(result, newId), canonicalInstance);
    			return result;
    		}
		}
    	RunStats.N_SAVED_EVALS_LAZY++;
		FSM originalInstanceCopy = individual.copyInstance(individual);
		mutatedInstance.setFitnessDependentData(originalInstanceCopy.getFitnessDependentData());
		return new FitInstance<FSM>(mutatedInstanceMetaData.getInstance(), sourceFitness);	
	}
	
}
