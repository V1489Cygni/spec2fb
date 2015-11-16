package ru.ifmo.optimization.instance.fsm.task.languagelearning;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.comparator.MaxSingleObjectiveComparator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class LanguageLearningTask extends AbstractAutomatonTask {
	public class RunResult {
		private boolean rightAnswer;
		private int finalState;
		private Collection<Transition> visitedTransitions;
		
		public RunResult(int finalState, boolean rightAnswer, Collection<Transition> visitedTransitions) {
			this.finalState = finalState;
			this.rightAnswer = rightAnswer;
			this.visitedTransitions = visitedTransitions;
		}
		
		public boolean accept() {
			return rightAnswer;
		}
		
		public int getFinalState() {
			return finalState;
		}
		
		public Collection<Transition> getVisitedTransitions() {
			return visitedTransitions;
		}
	}
		
	protected AbstractTaskConfig config;
	private String[] actions = new String[]{"n"};
	protected List<Example> examples;
	protected List<String> events = Arrays.asList(new String[]{"0", "1"}); 
	protected double numberOfAccepts = 0;
	protected double numberOfRejects = 0;
	
	public LanguageLearningTask(AbstractTaskConfig config) {
		this.config = config;
		this.desiredFitness = config.getDesiredFitness();
		this.desiredNumberOfStates = Integer.parseInt(config.getProperty("desired-number-of-states"));
		
		Scanner in = null;
		try {
			in = new Scanner(new File(config.getProperty("examples")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int numberOfExamples = in.nextInt();
		in.next();
		examples = new ArrayList<Example>(numberOfExamples);
		for (int j = 0; j < numberOfExamples; j++) {
			boolean accept = (in.nextInt() == 1);
			int length = in.nextInt();
			StringBuffer sb = new StringBuffer(length);
			for (int i = 0; i < length; i++) {
				sb.append(in.next()); 
			}
			examples.add(new Example(sb.toString(), accept));
			if (accept) {
				numberOfAccepts++;
			} else {
				numberOfRejects++;
			}
		}
		in.close();
		
		comparator = new MaxSingleObjectiveComparator();
	}

	protected int runDFAOnExample(FSM dfa, Example example) {
		int currentState = dfa.getInitialState();
		for (int event : example.getString()) {
			currentState = dfa.transitions[currentState][event].getEndState();
		}
		return currentState;
	}
	
	protected RunResult runDFAOnExampleWithTransitions(FSM dfa, Example example) {
		Set<Transition> visitedTransitions = 
			new HashSet<Transition>();
		
		int currentState = dfa.getInitialState();
		for (int event : example.getString()) {
			int nextState = dfa.transitions[currentState][event].getEndState();
			Transition tr = new Transition(currentState, nextState, dfa.getEvents().get(event), "n");
			visitedTransitions.add(tr);
			currentState = nextState;
		}
		return new RunResult(currentState, example.accept(), visitedTransitions);
	}
	
	@Override
	public FitInstance<FSM> getFitInstance(FSM dfa) {
		
		FsmMetaData metaData = (FsmMetaData) getInstanceMetaData(dfa);
		dfa.markUsedTransitions(metaData.getVisitedTransitions());
		return new FitInstance<FSM>(dfa, metaData.getFitness());
	}
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM dfa) {
		numberOfFitnessEvaluations++;
		Set<Transition> visitedTransitions = new HashSet<Transition>();
		
		int[][] mark = new int[dfa.getNumberOfStates()][2];
		for (int i = 0; i < dfa.getNumberOfStates(); i++) {
			for (int j = 0; j < mark[i].length; j++) {
				mark[i][j] = 0;
			}
		}
		
		for (Example example : examples) {
			RunResult runResult = runDFAOnExampleWithTransitions(dfa, example);
			int accept = example.accept() ? 1 : 0;
			int finalState = runResult.getFinalState();
			mark[finalState][accept]++;
			visitedTransitions.addAll(runResult.getVisitedTransitions());
		}
		
		FSM labelled = new FSM(dfa);
		double numberOfSuccesses = 0;
		
		for (int i = 0; i < dfa.getNumberOfStates(); i++) {
			if (mark[i][1] > mark[i][0]) {
				numberOfSuccesses += mark[i][1];
				labelled.setStateTerminal(i, true);
			} else {
				numberOfSuccesses += mark[i][0];
				labelled.setStateTerminal(i, false);
			}
		}
		
		double fitness = 100.0 * numberOfSuccesses / (double)examples.size();
		return new FsmMetaData(labelled, visitedTransitions, fitness);
	}

	@Override
	public List<String> getEvents() {
		return events;
	}

	@Override
	public String[] getActions() {
		return actions;
	}

	@Override
	public AutomatonTaskConstraints getConstraints() {
		return new AutomatonTaskConstraints();
	}

	@Override
	public void reset() {
	}

	@Override
	public Comparator<Double> getComparator() {
		return comparator;
	}
	
	@Override
	public double correctFitness(double fitness, FSM cachedInstance, FSM trueInstance) {
		return 0;
	}
}
