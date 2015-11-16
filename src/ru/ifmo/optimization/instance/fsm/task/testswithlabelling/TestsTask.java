package ru.ifmo.optimization.instance.fsm.task.testswithlabelling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import ru.ifmo.optimization.instance.comparator.MaxSingleObjectiveComparator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

/**
 * Class implementing calculation of a fitness function of an automaton
 * based on tests.
 * @author Daniil Chivilikhin
 */
public abstract class TestsTask extends AbstractAutomatonTask {
	protected AutomatonTest[] tests;
	protected ArrayList<String> events;
	protected String[] outputs;
	protected String[] actionsForEvolving;
	protected String[] actions;
	protected boolean useTests;
	protected boolean useScenarios;
	protected Properties testsProperties;
	
	public TestsTask(AbstractTaskConfig config) {
		this.config = config;
		desiredFitness = config.getDesiredFitness();
		desiredNumberOfStates = Integer.parseInt(config.getProperty("desired-number-of-states"));
		comparator = new MaxSingleObjectiveComparator();
		
		testsProperties = new Properties();
		try {
			testsProperties.load(new FileInputStream(new File(config.getProperty("tests"))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		useTests = Boolean.parseBoolean(testsProperties.getProperty("use-tests"));
		useScenarios = Boolean.parseBoolean(testsProperties.getProperty("use-scenarios"));
		if (useTests && useScenarios) {
			throw new RuntimeException("Both useTests and useScenarios parameters are true, select one");
		}		
	}
	
	protected void readTests(String filename) {
		Scanner in = null;
		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int numberOfTests = in.nextInt();
		tests = new AutomatonTest[numberOfTests];
		
		for (int i = 0; i < numberOfTests; i++) {
			String[] inputAndOutput = in.next().split("->");
			String[] inputSequence = inputAndOutput[0].split("_");
			String[] outputSequence = new String[] {""};
			if (inputAndOutput.length > 1) {
				outputSequence = inputAndOutput[1].split("_");
			}
			tests[i] = new AutomatonTest(inputSequence, outputSequence);
		}
	}
	
	protected void loadScenarios(String filename) {
		Scanner in = null;
		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int numberOfTests = in.nextInt();
		in.nextLine();
		
		tests = new AutomatonTest[numberOfTests];
		for (int i = 0; i < numberOfTests; i++) {
			List<String> inputSequence = new ArrayList<String>();
			List<String> outputSequence = new ArrayList<String>();
			
			String scenario = in.nextLine();
			String[] elements = scenario.split(";");
			
			for (String element : elements) {
				String[] eventActions = element.trim().split("/");
				String event = eventActions[0];
				String actions = eventActions.length > 1 ? eventActions[1] : "";
				actions = actions.replaceAll(",", "");
				inputSequence.add(event);
				outputSequence.add(actions);
			}
			tests[i] = new AutomatonTest(inputSequence.toArray(new String[0]), outputSequence.toArray(new String[0]));
		}
		
		in.close();
	}
	
	protected FsmMetaData runFsmOnTest(FSM fsm, AutomatonTest test, boolean scenario) {
		int currentState = fsm.getInitialState();

		Set<Transition> visitedTransitions = new HashSet<Transition>();
		List<String> answers = new ArrayList<String>();
		for (int i = 0; i < test.getInput().length; i++) {
			int eventIndex = events.indexOf(test.getInput()[i]);
			String answer = fsm.transitions[currentState][eventIndex].getAction();
			int nextState = fsm.transitions[currentState][eventIndex].getEndState();
			if (nextState == -1) {
				break;
			}
			
			Transition tr = new Transition(currentState, nextState, test.getInput()[i], answer);
			visitedTransitions.add(tr);
			currentState = nextState;
			answers.add(answer);
		}
		String answerArray[] = answers.toArray(new String[0]);

		double f = Math.max(test.getOutput().length, answerArray.length) == 0 
					? 1.0
					: test.getLevenshteinDistance(answerArray) / Math.max(test.getOutput().length, answerArray.length);
		return new FsmMetaData(fsm, visitedTransitions, f);
	}


	public String[] getAutomatonOutputs() {
		return outputs;
	}

	public void printAutomataOutputs(String filename) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < outputs.length; i++) {
			out.println(outputs[i]);
		}
		out.close();
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
}
