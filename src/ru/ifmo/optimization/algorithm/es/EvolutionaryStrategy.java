package ru.ifmo.optimization.algorithm.es;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.Checkable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.Hashable;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.util.Pair;

public abstract class EvolutionaryStrategy<Instance extends Checkable<Instance, MutationType>, 
						 MutationType extends InstanceMutation<Instance>> extends AbstractOptimizationAlgorithm<Instance> {
	protected List<Mutator<Instance, MutationType>> mutators;
	protected Instance start;
	protected boolean doUseLazyFitnessCalculation;
	protected final boolean onePlusLambda;
	protected final int stagnationParameter;
	private List<Pair<Integer, Integer>> lambdaHistory = new ArrayList<Pair<Integer,Integer>>();
	
	class EsInstanceMetaData<Instance extends Hashable> extends InstanceMetaData<Instance> {
		private InstanceMetaData<Instance> instance;
		private List<Pair<Integer, Integer>> lambdaHistory;
		
		
		public EsInstanceMetaData(Instance instance, double fitness, List<Pair<Integer, Integer>> lambdaHistory) {
			super(instance, fitness);
			this.lambdaHistory = lambdaHistory;
		}
		
		public EsInstanceMetaData(InstanceMetaData<Instance> instanceMd, List<Pair<Integer, Integer>> lambdaHistory) {
			super(instanceMd.getInstance(), instanceMd.getFitness());
			this.instance = instanceMd;
			this.lambdaHistory = lambdaHistory;
		}
		
		@Override
		public void printProblemSpecificData(String dirname) {
		}

		@Override
		public void print(String dirname) {
			super.print(dirname);
			instance.printProblemSpecificData(dirname);
			PrintWriter out = null;
			try {
				out = new PrintWriter(new File(dirname + "/lambda-history"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			for (Pair<Integer, Integer> p : lambdaHistory) {
				out.println(p.first + " " + p.second);
			}
			out.close();
		}
	}
	
	protected InitialFSMGenerator initialInstanceGenerator = new InitialFSMGenerator();
	
	public EvolutionaryStrategy(AbstractTaskFactory<Instance> taskFactory, 
			List<Mutator<Instance, MutationType>> mutators, Instance start, boolean doUseLazyFitnessCalculation,
			boolean onePlusLambda, int stagnationParameter) {
		super(taskFactory);
		this.mutators = mutators;
		this.start = start;
		this.doUseLazyFitnessCalculation = doUseLazyFitnessCalculation;
		this.onePlusLambda = onePlusLambda;
		this.stagnationParameter = stagnationParameter;
	}
	
	private MutatedInstanceMetaData<Instance, MutationType> mutateInstance(Instance instance) {
        return mutators.get(ThreadLocalRandom.current().nextInt(mutators.size())).apply(instance);
    }
	
	@Override
	public InstanceMetaData<Instance> runAlgorithm() {
		return run(Integer.MAX_VALUE);
	}
	
	protected FitInstance<Instance> applyFitness(Instance instance, double sourceFitness, Instance mutatedInstance,
			MutatedInstanceMetaData<Instance, MutationType> mutatedInstanceMetaData) {
		task.increaseNumberOfAttemptedFitnessEvaluations(1);
		
		if (!doUseLazyFitnessCalculation || mutatedInstanceMetaData == null) {
			FitInstance<Instance> fitInstance = task.getFitInstance(mutatedInstance);
			return new FitInstance<Instance>(fitInstance.getInstance(), fitInstance.getFitness()); 
		}
		if (instance.needToComputeFitness(mutatedInstanceMetaData.getMutations())) {
			FitInstance<Instance> result = task.getFitInstance(mutatedInstanceMetaData.getInstance());
			return result;
		}
		RunStats.N_SAVED_EVALS_LAZY++;
		Instance originalInstanceCopy = instance.copyInstance(instance);
		((FSM)mutatedInstanceMetaData.getInstance()).setFitnessDependentData(originalInstanceCopy.getFitnessDependentData());
		return new FitInstance<Instance>(mutatedInstanceMetaData.getInstance(), sourceFitness);
	}
	
	protected Instance randomInstance() {
		return (Instance) initialInstanceGenerator.createInstance(task);
	}
	
	public abstract int lambda(double fitness);
	
	public abstract void adaptiveHook(FitInstance<Instance> currentInstance, Collection<FitInstance<Instance>> mutants);
	
	public InstanceMetaData<Instance> run(int maxNumberOfSteps) {
		Instance initialInstance = randomInstance();
		FitInstance<Instance> instance = applyFitness(initialInstance, 0, initialInstance, null);
		FitInstance<Instance> bestSolution = applyFitness(initialInstance, 0, initialInstance, null);
		FitInstance<Instance> currentBestSolution = applyFitness(initialInstance, 0, initialInstance, null);
		double bestFitness = Double.MIN_VALUE;
		
		System.out.println("steps = " + task.getNumberOfFitnessEvaluations() + "; bestFitness = " + bestSolution.getFitness());
		
		int lastBestFitnessOccurence = task.getNumberOfAttemptedFitnessEvaluations();
		
		while (task.getNumberOfFitnessEvaluations() < maxNumberOfSteps) {
			if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
				InstanceMetaData<Instance> result = new EsInstanceMetaData<Instance>(
						task.getInstanceMetaData(bestSolution.getInstance()), lambdaHistory);
				result.setNumberOfFitnessEvaluations(task.getNumberOfFitnessEvaluations());
				System.out.println("Resources depleted, best solution has f = " + bestSolution.getFitness());
				System.out.println("Completed " + task.getNumberOfAttemptedFitnessEvaluations() + 
						" attempts: " + task.getNumberOfFitnessEvaluations() + " true fitness evals and " + RunStats.N_CANONICAL_CACHE_HITS + " canonical cache hits.");
				
				if (!(result.getFitness() >= bestFitness)) {
					throw new RuntimeException("Whoops! result = " + result.getFitness() +  "; best = " + bestFitness + ".//In cutoff");
				}

				return result;	
			}
			List<FitInstance<Instance>> mutated = new ArrayList<FitInstance<Instance>>();
			
			int lambd = lambda(instance.getFitness());
			lambdaHistory.add(new Pair<Integer, Integer>(task.getNumberOfFitnessEvaluations(), lambd));
			
			for (int i = 0; i < lambd; i++) {
				MutatedInstanceMetaData<Instance, MutationType> mutatedInstanceMetaData = mutateInstance(instance.getInstance());
				mutated.add(applyFitness(instance.getInstance(), instance.getFitness(), 
						mutatedInstanceMetaData.getInstance(), mutatedInstanceMetaData));
				
				if (task.getNumberOfAttemptedFitnessEvaluations() % 500 == 0) {
					System.out.println("steps = " + task.getNumberOfFitnessEvaluations() + "; " +
							"lambda = " + lambd + "; " +
							"currentSolution = " + instance.getFitness() + "; " +
							"bestFitness = " + bestSolution.getFitness());
				}
			}
			
			adaptiveHook(instance, mutated);
			FitInstance<Instance> bestMutant = Collections.max(mutated);			
			
			if (onePlusLambda) {
				if (bestMutant.getFitness() >= instance.getFitness()) {
					instance = new FitInstance<Instance>(bestMutant);	
				}
			} else {
				instance = new FitInstance<Instance>(bestMutant);
			}

			if (instance.getFitness() >= currentBestSolution.getFitness()) {
				if (instance.getFitness() > currentBestSolution.getFitness()) {
					lastBestFitnessOccurence = task.getNumberOfAttemptedFitnessEvaluations();
				}
				currentBestSolution = new FitInstance<Instance>(instance);
			}
			
			if (instance.getFitness() > bestSolution.getFitness()) {
				bestSolution = new FitInstance<Instance>(instance);
				bestFitness = instance.getFitness();
			}

			if (instance.getFitness() >= task.getDesiredFitness()) {
				InstanceMetaData<Instance> result = new EsInstanceMetaData<Instance>(
						task.getInstanceMetaData(instance.getInstance()), lambdaHistory);
				result.setNumberOfFitnessEvaluations(task.getNumberOfFitnessEvaluations());
				System.out.println("Found acceptable solution with f = " + result.getFitness());
				System.out.println("Completed " + task.getNumberOfAttemptedFitnessEvaluations() + 
						" attempts: " + task.getNumberOfFitnessEvaluations() + " true fitness evals and " + 
						RunStats.N_CANONICAL_CACHE_HITS + " canonical cache hits.");
				if (!(result.getFitness() >= bestFitness)) {
					throw new RuntimeException("Whoops! result = " + result.getFitness() +  "; best = " + bestFitness + ".//In f >= desired f");
				}

				return result;	
			}
			
			if (task.getNumberOfAttemptedFitnessEvaluations() - lastBestFitnessOccurence >= stagnationParameter) {
				//restarting
				System.out.println("Restarting...");
				lastBestFitnessOccurence = task.getNumberOfAttemptedFitnessEvaluations();
				instance = task.getFitInstance(randomInstance());
				task.increaseNumberOfAttemptedFitnessEvaluations(1);
				currentBestSolution = new FitInstance<Instance>(instance);
			}
			
		}
		InstanceMetaData<Instance> result = new EsInstanceMetaData<Instance>(
				task.getInstanceMetaData(bestSolution.getInstance()), lambdaHistory);
		result.setNumberOfFitnessEvaluations(task.getNumberOfFitnessEvaluations());
		System.out.println("Completed " + task.getNumberOfAttemptedFitnessEvaluations() + 
				" attempts: " + task.getNumberOfFitnessEvaluations() + " true fitness evals and " + 
				RunStats.N_CANONICAL_CACHE_HITS + " canonical cache hits.");
		if (!(result.getFitness() > bestFitness)) {
			throw new RuntimeException("Whoops! result = " + result.getFitness() +  "; best = " + bestFitness + ".// In the end");
		}
		return result;
	}
}
