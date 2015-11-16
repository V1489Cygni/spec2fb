package ru.ifmo.optimization.instance.fsm.landscape;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.landscape.sampler.InstanceSampler;
import ru.ifmo.optimization.instance.fsm.landscape.sampler.MetropolisHastingsInstaceSampler;
import ru.ifmo.optimization.instance.fsm.landscape.sampler.RandomInstanceSampler;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.factory.FsmTaskFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class RandomNeighborhoodSampler implements Runnable {
	
	public enum Sampler {
		RANDOM,
		METROPOLIS_HASTINGS
	};
	
	private AbstractAutomatonTask task;
	private InstanceSampler sampler;
	private int sampleSize;

	public RandomNeighborhoodSampler(AbstractAutomatonTask task, InstanceSampler sampler, int sampleSize) {
		this.task = task;
		this.sampler = sampler;
		this.sampleSize = sampleSize;
	}

	private FitInstance<FSM> applyFitness(FSM fsm) {
		return task.getFitInstance(fsm);
	}

	public List<FSM> getAllMutations(FSM fsm) {
		List<FSM> result = new ArrayList<FSM>();
		for (int state = 0; state < fsm.getNumberOfStates(); state++) {
			for (int event = 0; event < fsm.getNumberOfEvents(); event++) {
				FSM.Transition t = fsm.transitions[state][event];
				int endState = t.getEndState();
				String actions = t.getAction();

				for (int i = 0; i < fsm.getNumberOfStates(); i++) {
					if (i == endState) {
						continue;
					}

					FSM mutated = new FSM(fsm);
					mutated.transitions[state][event].setEndState(i);
					result.add(mutated);
				}

				for (int i = 0; i < task.getActions().length; i++) {
					if (actions.equals(task.getActions()[i])) {
						continue;
					}
					FSM mutated = new FSM(fsm);
					mutated.transitions[state][event].setAction(task.getActions()[i]);
					result.add(mutated);
				}
			}
		}
		return result;
	}
	
	
	public FitnessDistributionCache getFitnessDistributionCache() {
		FitnessDistributionCache cache = new FitnessDistributionCache();
		List<FitInstance<FSM>> sample = sampler.sample(sampleSize);
		
		for (int i = 0; i < sample.size(); i++) {
			FitInstance<FSM> instance = sample.get(i);
			List<FSM> neighbors = getAllMutations(instance.getInstance());
			for (int j = 0; j < neighbors.size(); j++) {
				FitInstance<FSM> fitInstance = applyFitness(neighbors.get(j));
				cache.add(instance.getFitness(), fitInstance.getFitness());
				cache.add(fitInstance.getFitness(), instance.getFitness());
			}
		}
		return cache;
	}
	
	@Override
	public void run() {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File("sample"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		List<FitInstance<FSM>> sample = sampler.sample(sampleSize);
		
		for (int i = 0; i < sample.size(); i++) {
			FitInstance<FSM> instance = sample.get(i);
			List<FSM> neighbors = getAllMutations(instance.getInstance());
			out.print(instance.getFitness() + " ");
			for (int j = 0; j < neighbors.size(); j++) {
				out.print(applyFitness(neighbors.get(j)).getFitness());
				if (j < neighbors.size() - 1) {
					out.print(" ");
				}
			}
			out.println();
		}
		
		out.close();
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: java -jar neighborhood-sampler.jar [task config] [Sampler] [sample size]");
			System.exit(1);
		}
		String taskConfigFilename = args[0];
		Sampler samplerType = Sampler.valueOf(args[1]);
		int sampleSize = Integer.parseInt(args[2]);

		AbstractTaskConfig taskConfig = new AbstractTaskConfig(taskConfigFilename);
		FsmTaskFactory taskFactory = new FsmTaskFactory(taskConfig);
		AbstractAutomatonTask task = (AbstractAutomatonTask) taskFactory.createTask();
		
		InstanceSampler sampler = null;
		switch (samplerType) {
		case RANDOM:
		    sampler = new RandomInstanceSampler(task);
		    break;
		case METROPOLIS_HASTINGS:
			sampler = new MetropolisHastingsInstaceSampler(task);
			break;
		}
		new Thread(new RandomNeighborhoodSampler(task, sampler, sampleSize)).start();
	}
}
