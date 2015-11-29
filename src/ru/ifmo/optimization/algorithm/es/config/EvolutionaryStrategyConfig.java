package ru.ifmo.optimization.algorithm.es.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeFinalStateMutator;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeOutputActionMutator;
import ru.ifmo.optimization.instance.fsm.mutator.LucasReynoldsMutator;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class EvolutionaryStrategyConfig<Instance extends Constructable<Instance>> {
	public static enum MutatorType {
		CHANGE_DEST,
		CHANGE_ACTIONS,
		LUCAS_REYNOLDS,
	}
	
	public static enum AdaptiveCriteria {
		INCREASE_PROBABILITY,
		FITNESS_GAIN,
		DEFAULT,
		CANONICAL_ES
	}
	
	private Properties properties = new Properties();
	
	public EvolutionaryStrategyConfig(String propertiesFileName) {
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
	
	
	public List<Mutator> getMutators(AbstractOptimizationTask<Instance> t) {
		List<Mutator> mutators = new ArrayList<Mutator>();
		for (MutatorType type : getMutatorTypes()) {
			switch (type) {
 			case CHANGE_ACTIONS: {
 				AbstractAutomatonTask task = (AbstractAutomatonTask)t;
				mutators.add(new ChangeOutputActionMutator(task.getActions(), task.getConstraints()));
				break;
 			}
			case CHANGE_DEST: 
				mutators.add(new ChangeFinalStateMutator());
				break;
			case LUCAS_REYNOLDS: {
				AbstractAutomatonTask task = (AbstractAutomatonTask)t;
				mutators.add(new LucasReynoldsMutator(task.getActions(), task.getConstraints(), task));
				break;
			}
			}
		}
		return mutators;
	}
	
	public AdaptiveCriteria getAdaptiveCriteria() {
		return AdaptiveCriteria.valueOf(properties.getProperty("adaptive-criteria"));
	}

	public int lambda() {
		return Integer.parseInt(properties.getProperty("lambda"));
	}
	
	public int getInitialSampleSize() {
		return Integer.parseInt(properties.getProperty("initial-sample-size"));
	}
	
	public int getDefaultLambda() {
		return Integer.parseInt(properties.getProperty("default-lambda"));
	}
	
	public int getStatisticalThreshold() {
		return Integer.parseInt(properties.getProperty("statistical-threshold"));
	}
	
	public boolean doUseLazyFitnessCalculation() {
		return Boolean.parseBoolean(properties.getProperty("do-use-lazy-fitness-calculation"));
	}
	
	public boolean onePlusLambda() {
		return Boolean.parseBoolean(properties.getProperty("one-plus-lambda"));
	}
	
	public int getStagnationParameter() {
		return Integer.parseInt(properties.getProperty("stagnation-parameter"));
	}
	
	public int getMaxCanonicalCacheSize() {
		return Integer.parseInt(properties.getProperty("max-canonical-cache-size"));
	}
}


