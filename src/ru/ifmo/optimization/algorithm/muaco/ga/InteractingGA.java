package ru.ifmo.optimization.algorithm.muaco.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import ru.ifmo.ctddev.genetic.transducer.algorithm.FST;
import ru.ifmo.ctddev.genetic.transducer.algorithm.Parameters;
import ru.ifmo.ctddev.genetic.transducer.algorithm.Transition;
import ru.ifmo.ctddev.genetic.transducer.algorithm.ga.SimpleGeneticAlgorithm;
import ru.ifmo.ctddev.genetic.transducer.scenario.TestGroup;
import ru.ifmo.optimization.algorithm.muaco.parallel.InteractingAlgorithm;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.FstFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.random.RandomProvider;

public class InteractingGA<Instance extends Constructable<Instance>> extends SimpleGeneticAlgorithm 
	implements InteractingAlgorithm<Instance>, Callable<InstanceMetaData<Instance>> {

	private List<InteractingAlgorithm<Instance>> otherAlgorithms;
	private AbstractOptimizationTask<Instance> task;
	private List<FitInstance<Instance>> offeredSolutions = new ArrayList<FitInstance<Instance>>();
	private int migrationPeriod;
	
	public InteractingGA(Parameters parameters,  String[] setOfInputs, String[] setOfOutputs, List<TestGroup> groups,
            int maxFitnessEvals, double maxRunTime, boolean useBfs, boolean useLazy, int bfsCacheSize, AbstractTaskFactory<Instance> taskFactory, int migrationPeriod) {
		super(parameters, setOfInputs, setOfOutputs, groups, Integer.MAX_VALUE, Double.MAX_VALUE, useBfs, useLazy, bfsCacheSize);
		task = taskFactory.createTask();
		this.migrationPeriod = migrationPeriod;
	}
	
	@Override
	public void setOtherAlgorithms(List<InteractingAlgorithm<Instance>> otherAlgorithms) {
		this.otherAlgorithms = otherAlgorithms;
	}

	@Override
	public void offerSolution() {
		if (genCount % migrationPeriod == 0 && otherAlgorithms != null) {
			if (bestAutomaton != null && !otherAlgorithms.isEmpty()) {
				FSM bestFSM = new FSM(bestAutomaton);
				otherAlgorithms.get(RandomProvider.getInstance().nextInt(otherAlgorithms.size())).acceptSolution(task.getFitInstance((Instance) bestFSM));
				System.out.println("GA: offered solution with f = " + bestAutomaton.fitness());
			}
		}
	}
	
	@Override
	public synchronized void acceptSolution(FitInstance<Instance> solution) {
		offeredSolutions.add(solution);
	}
	
	private InstanceMetaData<Instance> runAlgorithm() {
		FST fst = go();
		System.out.println("Thread " + Thread.currentThread().getId() + " finished");
		if (Thread.currentThread().isInterrupted()) {
			System.out.println("Thread " + Thread.currentThread().getId() + " interrupted");
			return null;
		}

		System.out.println("Converting FST with f = " + fst.fitness() + " to FSM; best fitness = " + bestAutomaton.fitness());
		FSM fsm = new FSM(fst.getNumberOfStates());
		for (int state = 0; state < fst.getNumberOfStates(); state++) {
			for (Transition t : fst.getStates()[state]) {
				int eventId = FSM.EVENTS.indexOf(t.getInput().replace(" ", ""));
				fsm.transitions[state][eventId] = new FSM.Transition(state, t.getNewState(), t.getInput().replace(" ", ""), "1");
			}
		}
		
		System.out.println("Converted");
		System.out.println(fsm);
		InstanceMetaData<Instance> result = (InstanceMetaData<Instance>) new FsmMetaData(fsm, fsm.getTransitions(), bestAutomaton.fitness());
		System.out.println("Returning result with f = " + result.getFitness());
		System.out.println("Thread " + Thread.currentThread().getId() + " really finished");
		return result;
	}
	
	@Override
	protected FST runIteration() {
		Arrays.sort(curGeneration, new Comparator<FST>() {
			public int compare(FST a1, FST a2) {
				return Double.compare(fitnessEvaluator.getFitness(a2), fitnessEvaluator.getFitness(a1));
			}}
		);
		
		offerSolution();
		
		synchronized (this) {
			if (!offeredSolutions.isEmpty()) { 
				System.out.println("GA: Have " + offeredSolutions.size() + " offered solutions: ");
				List<FitInstance<Instance>> solutions = new ArrayList<FitInstance<Instance>>();
				solutions.addAll(offeredSolutions);
				offeredSolutions.clear();
				Collections.sort(solutions);

				for (FitInstance<Instance> solution : solutions) {
					System.out.print(solution.getFitness() + " ");
				}
				System.out.println();

				for (FitInstance<Instance> offerredSolution : solutions) {
					for (int i = 0; i < curGeneration.length; i++) {
						if (offerredSolution.getFitness() > curGeneration[i].fitness()) {
							System.out.println("Offered solution with f=" + offerredSolution.getFitness() + " is better than solution #" + i + " with f=" + curGeneration[i].fitness());
							curGeneration[i] = FstFactory.createFST((FSM)offerredSolution.getInstance(), setOfOutputs);
							break;
						}
					}
				}
			}
		}
		
		return super.runIteration();
	}
	
	@Override
	public InstanceMetaData<Instance> call() throws Exception {
		InstanceMetaData<Instance> result = runAlgorithm();
		System.out.println("Thread " + Thread.currentThread().getId() + " call finished");
		return result;
	}
}
