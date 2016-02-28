package ru.ifmo.optimization.instance.fsm.landscape;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeFinalStateMutator;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeOutputActionMutator;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.factory.FsmTaskFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class FitnessCloudGenerator implements Runnable {
	private AbstractOptimizationTask<FSM> task;
	private int sampleSize;
	private int neigborhoodSize = 10;
	private InitialFSMGenerator fsmGenerator = new InitialFSMGenerator();
	private List<Mutator<FSM, FsmMutation>> mutators = new ArrayList<Mutator<FSM, FsmMutation>>();
	
	public FitnessCloudGenerator(AbstractOptimizationTask<FSM> task, int sampleSize) {
		this.task = task;
		this.sampleSize = sampleSize;
		mutators.add(new ChangeFinalStateMutator());
		mutators.add(new ChangeOutputActionMutator(
				((AbstractAutomatonTask)task).getActions(), ((AbstractAutomatonTask)task).getConstraints()));
	}
	
	private FSM createRandomFSM() {
		return fsmGenerator.createInstance(task);
	}
	
	private FitInstance<FSM> applyFitness(FSM fsm) {
		return task.getFitInstance(fsm);
	}
	
	private double alpha(FitInstance<FSM> lastInstance, FitInstance<FSM> newInstance) {
		return Math.min(1.0, newInstance.getFitness() / lastInstance.getFitness());
	}
	
	private List<FitInstance<FSM>> sampleSearchSpace(int size) {
		List<FitInstance<FSM>> samples = new ArrayList<FitInstance<FSM>>(size);
		samples.add(applyFitness(createRandomFSM()));
	
		while (samples.size() < size) {
			FitInstance<FSM> sample = applyFitness(createRandomFSM());
			
			double u = ThreadLocalRandom.current().nextDouble();
			if (u <= alpha(samples.get(samples.size() - 1), sample)) {
				samples.add(sample);
			}
		}
		return samples;
	}
	
	private FitInstance<FSM> mutateInstance(FSM fsm) {
		return applyFitness(mutators.get(ThreadLocalRandom.current().nextInt(mutators.size())).applySimple(fsm));
	}
	
	public FitInstance<FSM> rhouletteWheelSelection(List<FitInstance<FSM>> fsms) {
		Collections.sort(fsms);
		int size = fsms.size();
		double weight[] = new double[size];
		weight[0] = fsms.get(0).getFitness();

		for (int i = 1; i < size; i++) {
			weight[i] = weight[i - 1] + fsms.get(i).getFitness();
		}
		double p = weight[size - 1] * ThreadLocalRandom.current().nextDouble();
		int j = 0;

		while (p > weight[j]) {
			j++;
		}
		return fsms.get(j);
	}
	
	public FitInstance<FSM> tournamentSelection(List<FitInstance<FSM>> fsms) {
		return Collections.max(fsms);
	}
	
	private List<FitInstance<FSM>> generateMutants(FSM fsm) {
		List<FitInstance<FSM>> mutants = new ArrayList<FitInstance<FSM>>();
		for (int i = 0; i < neigborhoodSize; i++) {
			mutants.add(mutateInstance(fsm));
		}
		return mutants;
	}

	@Override
	public void run() {
		PrintWriter outNegativeSlopeCoefficient = null;
		PrintWriter outFitnessProportionalNegativeSlopeCoefficient = null;
		try {
			outNegativeSlopeCoefficient = new PrintWriter(new File("nsc_cloud"));
			outFitnessProportionalNegativeSlopeCoefficient = new PrintWriter(new File("fpnsc_cloud"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		List<FitInstance<FSM>> searchSpaceSamples = sampleSearchSpace(sampleSize);
		Collections.sort(searchSpaceSamples);
		
		for (FitInstance<FSM> instance : searchSpaceSamples) {
			List<FitInstance<FSM>> mutants = generateMutants(instance.getInstance());
			outNegativeSlopeCoefficient.println(
					instance.getFitness() + " " + tournamentSelection(mutants).getFitness());
			outFitnessProportionalNegativeSlopeCoefficient.println(
					instance.getFitness() + " " + rhouletteWheelSelection(mutants).getFitness());
		}
		outNegativeSlopeCoefficient.close();
		outFitnessProportionalNegativeSlopeCoefficient.close();
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: java -jar landscape-cloud.jar [task config name] [sample size]");
			System.exit(1);
		}
		
		String taskConfigFilename = args[0];
		int sampleSize = Integer.parseInt(args[1]);
		
		AbstractTaskConfig taskConfig = new AbstractTaskConfig(taskConfigFilename);
		FsmTaskFactory taskFactory = new FsmTaskFactory(taskConfig);
		new Thread(new FitnessCloudGenerator(taskFactory.createTask(), sampleSize)).start();
	}
}
