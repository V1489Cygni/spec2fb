package ru.ifmo.optimization.instance.fsm.task.languagelearning;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.SimpleFsmMetaData;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class DfaLanguageLearningEmulator extends LanguageLearningTask implements Runnable {
	private String fsmFileName;
	public DfaLanguageLearningEmulator(String fsmFilename) {
		super(new AbstractTaskConfig("noisy-dfa.properties"));
		this.fsmFileName = fsmFilename;
		
		this.config = new AbstractTaskConfig("noisy-dfa.properties");
		
		Scanner in = null;
		try {
			in = new Scanner(new File(config.getProperty("test-set")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int numberOfExamples = in.nextInt();
		in.next();
		examples = new ArrayList<Example>(numberOfExamples);
		while (in.hasNext()) {
			boolean label = (in.nextInt()) == 1;
			int length = in.nextInt();
			StringBuffer sb = new StringBuffer(length);
			for (int i = 0; i < length; i++) {
				sb.append(in.next()); 
			}
			examples.add(new Example(sb.toString(), label));
			if (label) {
				numberOfAccepts++;
			} else {
				numberOfRejects++;
			}
		}
		in.close();
	}
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM dfa) {
		numberOfFitnessEvaluations++;
		Set<Transition> visitedTransitions = new HashSet<Transition>();
	
		double numberOfSuccesses = 0;
		for (Example example : examples) {
			RunResult runResult = runDFAOnExampleWithTransitions(dfa, example);
			int finalState = runResult.getFinalState();
			if (example.accept() == dfa.isStateTerminal(finalState)) {
				numberOfSuccesses++;
			}
			visitedTransitions.addAll(runResult.getVisitedTransitions());
		}
		
		double fitness = 100.0 * numberOfSuccesses / (double)examples.size();
		return new FsmMetaData(dfa, visitedTransitions, fitness);
	}
	
	@Override
	public FitInstance<FSM> getFitInstance(FSM dfa) {
		FsmMetaData metaData = (FsmMetaData) getInstanceMetaData(dfa);
		dfa.markUsedTransitions(metaData.getVisitedTransitions());
		return new SimpleFsmMetaData(dfa, metaData.getFitness(), dfa.getUsedTransitions());
	}
	
	public static void main(String[] args) {
		new Thread(new DfaLanguageLearningEmulator(args[0])).start();
	}
	
	@Override
	public void run() {
		System.err.println("Number of examples = " + examples.size());
		FSM.setEvents(getEvents());
		FSM fsm = new FSM(fsmFileName);
		System.out.println("fitness = " + getFitInstance(fsm).getFitness());
	}

}
