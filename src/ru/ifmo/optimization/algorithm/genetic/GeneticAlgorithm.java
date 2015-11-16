package ru.ifmo.optimization.algorithm.genetic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.algorithm.genetic.config.GeneticAlgorithmConfig;
import ru.ifmo.optimization.algorithm.genetic.operator.Selection;
import ru.ifmo.optimization.algorithm.genetic.operator.crossover.Crossover;
import ru.ifmo.optimization.algorithm.genetic.operator.crossover.TsarevSmartAntCrossover1;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.Checkable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.random.RandomProvider;

public class GeneticAlgorithm<Instance extends Checkable<Instance, MutationType>, 
				MutationType extends InstanceMutation<Instance>> extends AbstractOptimizationAlgorithm<Instance> {
	
	private List<FitInstance<Instance>> population;
    private final int elitePart;
    private double bestFitness = Double.MIN_VALUE;
    private final int stepsUntilBigMutation;
    private final int stepsUntilSmallMutation;
    private int lastBestFitnessOccurence = 0;
    private Selection<Instance> selection;
    private List<Crossover> crossovers = new ArrayList<Crossover>();
    private List<Mutator<Instance, MutationType>> mutators = new ArrayList<Mutator<Instance, MutationType>>();
    private Instance bestIndividual;
    private String resultDirName;
    protected final boolean doUseLazyFitnessCalculation;
    
    public GeneticAlgorithm(GeneticAlgorithmConfig config,  
    			AbstractTaskFactory<Instance> taskFactory) {
    	super(taskFactory);
        int populationSize = config.getPopulationSize();
        this.elitePart = (int) (0.01 * config.getElitePart() * populationSize);
        this.stepsUntilBigMutation = config.getStepsUntilBigMutation();
        this.stepsUntilSmallMutation = config.getStepsUntilSmallMutation();
        this.doUseLazyFitnessCalculation = config.doUseLazyFitnessCalculation();
        
        population = new ArrayList<FitInstance<Instance>>(populationSize);

        for (int i = 0; i < populationSize; i++) {
        	Instance individual = getRandomFSM();
            population.add(task.getFitInstance(individual));
        }

        Collections.sort(population);

        bestIndividual = population.get(populationSize - 1).getInstance();
        bestFitness = population.get(populationSize - 1).getFitness();

        selection = new Selection<Instance>();
        mutators = config.getMutators(task);
        crossovers.add(new TsarevSmartAntCrossover1());
//        crossovers.add(new OnePointFsmCrossover());
        resultDirName = "genetic-results";  
        File resultDir = new File(resultDirName);
        if (!resultDir.exists()) {
            resultDir.mkdir();
        }
    }
    
    @Override
    public InstanceMetaData<Instance> runAlgorithm() {
    	int generationCount = 0;
    	
    	double lastBestFitness = -1;
    	int cntBestEqual = 0;
    	
    	while (bestFitness < task.getDesiredFitness()) {
    		if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
    			InstanceMetaData<Instance> result = task.getInstanceMetaData(bestIndividual);
    			result.setNumberOfFitnessEvaluations(task.getNumberOfFitnessEvaluations());
    			return result;
    		}
    		int populationSize = population.size();
    		List<FitInstance<Instance>> nextGeneration = new ArrayList<FitInstance<Instance>>(populationSize);
    		for (int i = 0; i < elitePart; i++) {
    			nextGeneration.add(population.get(populationSize - i - 1));
    		}

    		while (nextGeneration.size() < population.size()) {
    			if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
    				break;
    			}
    			List<Instance> offspring = new ArrayList<Instance>(2);
    			List<FitInstance<Instance>> fitOffspring = new ArrayList<FitInstance<Instance>>(2);
    			List<Instance> parents = new ArrayList<Instance>(2);
    			int firstParentId = RandomProvider.getInstance().nextInt(populationSize);
    			int secondParentId = firstParentId;
    			while (secondParentId == firstParentId) {
    				secondParentId = RandomProvider.getInstance().nextInt(populationSize);
    			}
    			parents.add(population.get(firstParentId).getInstance());
    			parents.add(population.get(secondParentId).getInstance());

    			if (RandomProvider.getInstance().nextBoolean()) {
    				Crossover<Instance> crossover = randomCrossover();
    				offspring = crossover.apply(parents);

    				if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
    					break;
    				}

    				fitOffspring.add(applyFitness(parents.get(0), 0, offspring.get(0), null));
    				fitOffspring.add(applyFitness(parents.get(1), 0, offspring.get(1), null));
    				nextGeneration.addAll(fitOffspring);
    			} else {
    				MutatedInstanceMetaData<Instance, MutationType> mutated0 = mutateInstance(parents.get(0));
    				MutatedInstanceMetaData<Instance, MutationType> mutated1 = mutateInstance(parents.get(1));
    				if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
    					break;
    				}
    				nextGeneration.add(applyFitness(parents.get(0), population.get(firstParentId).getFitness(), 
    						mutated0.getInstance(), mutated0));
    				nextGeneration.add(applyFitness(parents.get(1), population.get(secondParentId).getFitness(), mutated1.getInstance(), mutated1));
    			}
    		}

    		population.clear();
    		population.addAll(nextGeneration);
    		Collections.sort(population);
    		
    		double maxFitness = max();
    		if (Math.abs(maxFitness - lastBestFitness) < 1e-15) {
    			cntBestEqual++;
    		} else {
    			lastBestFitness = maxFitness;
    			cntBestEqual = 0;
    		}

    		if (cntBestEqual >= stepsUntilSmallMutation) {
    			System.out.println("Small mutation!");
    			smallMutation();
    		}
    		
        	if (cntBestEqual >= stepsUntilBigMutation) {
        		System.out.println("Big mutation!");
        		bigMutation();
        	}

    		if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
    			break;
    		}    		

    		if (population.get(populationSize - 1).getFitness() > bestFitness) {
    			lastBestFitnessOccurence = generationCount;
    			bestFitness = population.get(populationSize - 1).getFitness();
    			bestIndividual = population.get(populationSize - 1).getInstance();
    			if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
    				break;
    			}
    			InstanceMetaData<Instance> result = task.getInstanceMetaData(bestIndividual);
    			result.printProblemSpecificData(resultDirName);
    			System.out.println(generationCount + "; min = " + min() + "; mean = " + mean() + "; max = " + max() + "; overallBest = " + bestFitness);
    		}

    		if (generationCount % 10 == 0) {
    			System.out.println(generationCount + "; min = " + min() + "; mean = " + mean() + "; max = " + max() + "; overallBest = " + bestFitness);
    		}
        	
            generationCount++;
        }
        InstanceMetaData<Instance> result = task.getInstanceMetaData(bestIndividual);
        result.setNumberOfFitnessEvaluations(task.getNumberOfFitnessEvaluations());
        return result;
    }
    
    private FitInstance<Instance> bestIndividual(
    		FitInstance<Instance> ind1, FitInstance<Instance> ind2) {
        return ind1.getFitness() > ind2.getFitness() ? ind1 : ind2;
    }
    
    private Instance getRandomFSM() {
    	Instance instance = (Instance) new InitialFSMGenerator().createInstance(task);  
    	return instance;
    }
    
    private FitInstance<Instance> randomIndividual() {
        return population.get(RandomProvider.getInstance().nextInt(population.size()));
    }
    
    protected FitInstance<Instance> applyFitness(Instance individual, double sourceFitness, Instance mutatedInstance,
    		MutatedInstanceMetaData<Instance, MutationType> mutatedInstanceMetaData) {
    	if (!doUseLazyFitnessCalculation || mutatedInstanceMetaData == null) {
    		FitInstance<Instance> fitInstance = task.getFitInstance(mutatedInstance);
    		return new FitInstance<Instance>(fitInstance.getInstance(), fitInstance.getFitness());
    	}
    	if (individual.needToComputeFitness(mutatedInstanceMetaData.getMutations())) {
			FitInstance<Instance> result = task.getFitInstance(mutatedInstanceMetaData.getInstance());
			return result;
		}
    	RunStats.N_SAVED_EVALS_LAZY++;
		Instance originalInstanceCopy = individual.copyInstance(individual);
		mutatedInstance.setFitnessDependentData(originalInstanceCopy.getFitnessDependentData());
		return new FitInstance<Instance>(mutatedInstanceMetaData.getInstance(), sourceFitness);
    }
    
    private Crossover<Instance> randomCrossover() {
        return crossovers.get(RandomProvider.getInstance().nextInt(crossovers.size()));
    }
    
    private Mutator<Instance, MutationType> randomMutator() {
        return mutators.get(RandomProvider.getInstance().nextInt(mutators.size()));
    }
    
    private MutatedInstanceMetaData<Instance, MutationType> mutateInstance(Instance instance) {
    	return randomMutator().apply(instance);
    }
    
    private List<FitInstance<Instance>> selectParents(List<FitInstance<Instance>> population) {
        final int n = population.size();
        final double[] weight = new double[n];
        weight[0] = population.get(0).getFitness();
        
        for (int i = 1; i < n; i++) {
            weight[i] = weight[i - 1] + population.get(i).getFitness();
        }
        
        List<FitInstance<Instance>> selected = new ArrayList<FitInstance<Instance>>();
        
        while (selected.size() < 2) {
            double p = weight[n - 1] * RandomProvider.getInstance().nextDouble();
            int i = 0;
            while (p > weight[i]) {
                i++;
            }
            selected.add(population.get(i));
        }
        
        return selected;
    }

    private void nextGeneration(int generationCount) {
        int populationSize = population.size();
        List<FitInstance<Instance>> nextGeneration = new ArrayList<FitInstance<Instance>>(populationSize);
        for (int i = 0; i < elitePart; i++) {
        	nextGeneration.add(population.get(populationSize - i - 1));
        }

		while (nextGeneration.size() < population.size()) {
			 if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
				return;
			}
			List<Instance> offspring = new ArrayList<Instance>(2);
			List<FitInstance<Instance>> fitOffspring = new ArrayList<FitInstance<Instance>>(2);
			List<Instance> parents = new ArrayList<Instance>(2);
			int firstParentId = RandomProvider.getInstance().nextInt(populationSize);
			int secondParentId = firstParentId;
			while (secondParentId == firstParentId) {
				secondParentId = RandomProvider.getInstance().nextInt(populationSize);
			}
			parents.add(population.get(firstParentId).getInstance());
			parents.add(population.get(secondParentId).getInstance());

			if (RandomProvider.getInstance().nextBoolean()) {
				Crossover<Instance> crossover = randomCrossover();
				offspring = crossover.apply(parents);
				
				if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
					return;
				}
				
				fitOffspring.add(applyFitness(parents.get(0), 0, offspring.get(0), null));
				fitOffspring.add(applyFitness(parents.get(1), 0, offspring.get(1), null));
				nextGeneration.addAll(fitOffspring);
			} else {
				MutatedInstanceMetaData<Instance, MutationType> mutated0 = mutateInstance(parents.get(0));
				MutatedInstanceMetaData<Instance, MutationType> mutated1 = mutateInstance(parents.get(1));
				if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
					return;
				}
				nextGeneration.add(applyFitness(parents.get(0), population.get(firstParentId).getFitness(), 
								mutated0.getInstance(), mutated0));
				nextGeneration.add(applyFitness(parents.get(1), population.get(secondParentId).getFitness(), mutated1.getInstance(), mutated1));
			}
		}

        population.clear();
        population.addAll(nextGeneration);
        
        if (generationCount - lastBestFitnessOccurence > stepsUntilBigMutation) {
            lastBestFitnessOccurence = generationCount;
            System.out.println("Big mutation!");
            bigMutation();
        }
        
        if (generationCount - lastBestFitnessOccurence > stepsUntilSmallMutation) {
            lastBestFitnessOccurence = generationCount;
            System.out.println("Small mutation!");
            smallMutation();
        }
        
        if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
			return;
		}
        
        Collections.sort(population);
        
        if (population.get(populationSize - 1).getFitness() > bestFitness) {
            lastBestFitnessOccurence = generationCount;
            bestFitness = population.get(populationSize - 1).getFitness();
            bestIndividual = population.get(populationSize - 1).getInstance();
            if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
				return;
			}
            InstanceMetaData<Instance> result = task.getInstanceMetaData(bestIndividual);
            result.printProblemSpecificData(resultDirName);
            System.out.println(generationCount + "; min = " + min() + "; mean = " + mean() + "; max = " + max() + "; overallBest = " + bestFitness);
        }

        if (generationCount % 10 == 0) {
            System.out.println(generationCount + "; min = " + min() + "; mean = " + mean() + "; max = " + max() + "; overallBest = " + bestFitness);
        }
    }
    
    private double min() {
        return population.get(0).getFitness();
    }
    
    private double mean() {
        double result = 0;
        for (FitInstance<Instance> ind : population) {
            result += ind.getFitness();
        }
        return result / (double)population.size();
    }
    
    private double max() {
        return population.get(population.size() - 1).getFitness();
    }

    private void bigMutation() {
        for (int i = 0; i < population.size(); i++) {
        	if (RandomProvider.getInstance().nextBoolean()) {
        		MutatedInstanceMetaData<Instance, MutationType> mutated = mutateInstance(population.get(i).getInstance());
        		if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
					return;
				}
        		population.set(i, applyFitness(population.get(i).getInstance(), population.get(i).getFitness(), mutated.getInstance(), mutated));
        	} else {
        		Instance randomFSM = getRandomFSM();
        		if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
					return;
				}
        		population.set(i, applyFitness(randomFSM, 0, randomFSM, null));
        	}
        }
    }
    
    private void smallMutation() {
    	for (int i = 0; i < population.size() - elitePart; i++) {
    		MutatedInstanceMetaData<Instance, MutationType> mutated = mutateInstance(population.get(i).getInstance());
    		if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
				return;
			}
       		population.set(i, applyFitness(population.get(i).getInstance(), population.get(i).getFitness(), mutated.getInstance(), mutated));
        }
    }
}