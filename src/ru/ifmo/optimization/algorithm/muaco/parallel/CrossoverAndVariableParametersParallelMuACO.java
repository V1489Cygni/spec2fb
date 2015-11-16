package ru.ifmo.optimization.algorithm.muaco.parallel;

import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.ant.config.AntConfig;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.random.RandomProvider;

public class CrossoverAndVariableParametersParallelMuACO<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> 
	extends CrossoverAndSharedBestParallelMuACO<Instance, MutationType> {

	private double maxVariation;
	
	public CrossoverAndVariableParametersParallelMuACO(
			MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(config, taskFactory);
		
		maxVariation = Double.parseDouble(config.getProperty("max-parameter-variation"));
		
		System.out.print("Original parameters: ");
		System.out.print("Nmut = " + algorithms.get(0).getAntFactory().getAntConfig().getPathSelector().getNumberOfMutationsPerStep() + "; ");
		System.out.println("n_stag = " + algorithms.get(0).getAntStagnationParameter());

		for (MuACO<Instance, MutationType> algorithm : algorithms) {
			
			//change numberOfMutationsPerStep
			AntConfig antConfig = algorithm.getAntFactory().getAntConfig();
			antConfig.getPathSelector().setNumberOfMutationsPerStep(changeValue(antConfig.getPathSelector().getNumberOfMutationsPerStep()));
			
			//change antStagnationParameter
			algorithm.setAntStagnationParameter(changeValue(algorithm.getAntStagnationParameter()));
		}
		
		for (MuACO<Instance, MutationType> algorithm : algorithms) {
			System.out.print("Nmut = " + algorithm.getAntFactory().getAntConfig().getPathSelector().getNumberOfMutationsPerStep() + "; ");
			System.out.println("n_stag = " + algorithm.getAntStagnationParameter());
		}
	}

	private int changeValue(int value) {
		return (int) (value + Math.pow(-1, RandomProvider.getInstance().nextInt(2)) 
				* value * RandomProvider.getInstance().nextDouble() * maxVariation);
	}

}
