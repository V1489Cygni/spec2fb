package ru.ifmo.optimization.algorithm.genetic.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig.MutatorType;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeFinalStateMutator;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeOutputActionMutator;
import ru.ifmo.optimization.instance.fsm.mutator.MultipleChangesMutator;
import ru.ifmo.optimization.instance.fsm.mutator.MultipleFinalStateMutator;
import ru.ifmo.optimization.instance.fsm.mutator.MultipleOutputActionsMutator;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class GeneticAlgorithmConfig<Instance extends Constructable<Instance>> {
	
	private Properties properties = new Properties();
	
    public GeneticAlgorithmConfig(String propertiesFileName) {
        try {
            properties.load(new FileInputStream(new File(propertiesFileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public List<MutatorType> getMutatorTypes() {
		String[] listOfMutators = properties.getProperty("mutators").split(",");
		List<MutatorType> mutators = new ArrayList<MutatorType>();
		for (String s : listOfMutators) {
			MutatorType type = MutatorType.valueOf(s);
			mutators.add(type);
		}
		return mutators;
	}
    
    public List<Mutator<FSM, FsmMutation>> getMutators(AbstractOptimizationTask<Instance> t) {
    	AbstractAutomatonTask task = (AbstractAutomatonTask)t;
		List<Mutator<FSM, FsmMutation>> mutators = new ArrayList<Mutator<FSM, FsmMutation>>();
		for (MutatorType type : getMutatorTypes()) {
			switch (type) {
			case CHANGE_ACTIONS:
				mutators.add(new ChangeOutputActionMutator(task.getActions(), task.getConstraints()));
				break;
			case CHANGE_DEST:
				mutators.add(new ChangeFinalStateMutator());
				break;
			case MULTIPLE:
				mutators.add(new MultipleChangesMutator(task.getActions(), task.getConstraints(), task));
				break;
			case MULTIPLE_DEST:
				mutators.add(new MultipleFinalStateMutator(task));
				break;
			case MULTIPLE_ACTIONS:
				mutators.add(new MultipleOutputActionsMutator(task.getActions(), task.getConstraints(), task));
				break;
			}
		}
		return mutators;
    }
    
	public int getPopulationSize() {
		return Integer.parseInt(properties.getProperty("population-size"));
	}

	public int getElitePart() {
		return Integer.parseInt(properties.getProperty("elite-part"));
	}

	public int getStepsUntilSmallMutation() {
		return Integer.parseInt(properties.getProperty("steps-until-small-mutation"));
	}
	
	public int getStepsUntilBigMutation() {
		return Integer.parseInt(properties.getProperty("steps-until-big-mutation"));
	}
	
	public int getMaxCanonicalCacheSize() {
		return Integer.parseInt(properties.getProperty("max-canonical-cache-size"));
	}

	public double getMutationProbability() {
		return Double.parseDouble(properties
				.getProperty("mutation-probability"));	
	}

	public boolean addBestFromCrossover() {
		return Boolean.parseBoolean(properties
				.getProperty("add-best-from-crossover"));
	}
	
	public boolean doUseLazyFitnessCalculation() {
		return Boolean.parseBoolean(properties.getProperty("do-use-lazy-fitness-calculation"));
	}

}
