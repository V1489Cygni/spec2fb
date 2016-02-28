package ru.ifmo.optimization.instance.fsm.task.languagelearning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;

public class RandomDfaGenerator implements Runnable {
	private int numberOfStates;
	private double trainingSetDensity;
	private double noiseLevel;

	private List<String> events;
	private final String[] actions = new String[]{"n"};
	
	public RandomDfaGenerator(int numberOfStates, double trainingSetDensity, double noiseLevel) {
		this.numberOfStates = numberOfStates;
		this.trainingSetDensity = trainingSetDensity;
		this.noiseLevel = noiseLevel;
		
		events = new ArrayList<String>();
		events.add("0");
		events.add("1");
	}
	
	private String createZerosString(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append("0");
		}
		return sb.toString();
	}
	
	public List<String> createAllBinaryStrings(int length) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < Math.pow(2, length); i++) {
			String binaryString = Integer.toBinaryString(i);
			if (binaryString.length() < length) {
				binaryString = createZerosString(length - binaryString.length()) + binaryString;
			}
			result.add(binaryString);
		}
		return result;
	}
 	
	@Override
	public void run() {
		List<String> strings = new ArrayList<String>();
		FSM.setEvents(events);
		FSM dfa = null;
		//generate DFA with fixed nominal number of states and fixed depth
		int expectedDepth = (int) (2 * Math.log(numberOfStates) / Math.log(2) - 2);
		while (true) {
			dfa = new InitialFSMGenerator().createInstance(numberOfStates * 5 / 4, events, actions, new AutomatonTaskConstraints());
			int depth = dfa.getDepth();
			if (depth == expectedDepth) {
				System.out.println("Depth = " + depth);
				break;
			}
		}

		dfa.setInitialState(0);

		//randomly assign class labels to states 
		for (int i = 0; i < dfa.getNumberOfStates(); i++) {
			dfa.setStateTerminal(i, ThreadLocalRandom.current().nextBoolean());
		}
		
		dfa.printTransitionDiagram("target-dfa.transitions");

		//generate all binary strings of length from 1..(2*log_2(n) + 3)
		for (int i = 1; i <= 2 * Math.log(numberOfStates) / Math.log(2) + 3; i++) {
			strings.addAll(createAllBinaryStrings(i));
		}

		createDataSets(dfa, strings, trainingSetDensity, noiseLevel, ".");
	}
	
	private void createDataSets(FSM dfa, List<String> strings, double density, double noiseLevel, String path) {
		PrintWriter trainingSet = null;
		PrintWriter testSet = null;
		try {
			trainingSet = new PrintWriter(new File(path + "/train.a"));
			testSet = new PrintWriter(new File(path + "/test.a"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		boolean[] training = new boolean[strings.size()];
		Arrays.fill(training, false);
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		int trainingSetSize = (int)(strings.size() * density);
		int testSetSize = strings.size() - trainingSetSize;
		trainingSet.println(trainingSetSize + " " + 2);
		for (int i = 0; i < trainingSetSize; i++) {			
			int id = random.nextInt(strings.size());
			while (training[id]) {
				id = random.nextInt(strings.size());
			}
			training[id] = true;
			boolean flipLabel = random.nextDouble() < noiseLevel;
			printString(dfa, strings.get(id), trainingSet, flipLabel);
		}
		
		testSet.println(testSetSize + " " + 2);
		for (int i = 0; i < strings.size(); i++) {
			if (!training[i]) {
				printString(dfa, strings.get(i), testSet, false);
			}
		}
		
		trainingSet.close();
		testSet.close();
	}
	
	private void printString(FSM dfa, String s, PrintWriter out, boolean flipLabel) {
		char[] string = s.toCharArray();
		boolean label = accept(dfa, string);
		if (flipLabel) {
			System.err.println("Flipping label");
			label = !label;
		}
		out.print(label ? 1 : 0);
		out.print(" " + string.length);
		for (char c : string) {
			out.print(" " + c);
		}
		out.println();
	}
	
	private boolean accept(FSM dfa, char[] string) {
		int currentState = dfa.getInitialState();
		for (char c : string) {
			int event = (c == '0') ? 0 : 1;
			currentState = dfa.getTransition(currentState, event).getEndState();
		}
		return dfa.isStateTerminal(currentState);
	}
	
	public static void main(String[] args) {
		if (args.length < 4) {
			System.err.println("Usage: java -jar random-dfa-generator.jar [number of DFA states] [training set density] [noise level] [number of instances]");
			System.exit(1);
		}
 		int numberOfStates = Integer.parseInt(args[0]);
		double trainingSetDensity = Double.parseDouble(args[1]);
		double noiseLevel = Double.parseDouble(args[2]);
		new Thread(new RandomDfaGenerator(numberOfStates, trainingSetDensity, noiseLevel)).start();
	}
}
