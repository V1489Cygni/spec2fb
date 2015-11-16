package ru.ifmo.optimization.instance.fsm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;


/**
 * 
 * @author Daniil Chivilikhin
 * Class for storing an FSM with metadata corresponding to 
 * the calculation of the fitness function of the FSM.
 */
public class FsmMetaData extends InstanceMetaData<FSM> {
	private Collection<Transition> visitedTransitions;
//	private Collection<Transition> transitionsUsedInCounterexample;;
	private String[] stateLabels;

	public FsmMetaData(FSM fsm, Collection<Transition> visitedTransitions, double fitness) {
		super(fsm, fitness);
		this.visitedTransitions = visitedTransitions;
		stateLabels = new String[fsm.getNumberOfStates()];
		for (int i = 0; i < stateLabels.length; i++) {
			stateLabels[i] = i + "";
		}
	}
	
//	public FsmMetaData(FSM fsm, Collection<Transition> visitedTransitions, 
//			Collection<Transition> transitionsUsedInCounterexample, double fitness) {
//		this(fsm, visitedTransitions, fitness);
////		this.transitionsUsedInCounterexample = transitionsUsedInCounterexample;
//	}
//	
	public Collection<Transition> getVisitedTransitions() {
		return visitedTransitions;
	}
	
//	public Collection<Transition> getTransitionsUsedInCounterexample() {
//		return transitionsUsedInCounterexample;
//	}
//	
	public void setStateLabels(String[] labels) {
		this.stateLabels = labels;
	}

	public int getNumberOfVisitedStates() {
		int result = 0;
		boolean visited[] = new boolean[instance.getNumberOfStates()];
		Arrays.fill(visited, false);
		
		for (Transition tr : visitedTransitions) {
			visited[tr.getStartState()] = true;
			visited[tr.getEndState()] = true;
		}
		
		for (int i = 0; i < visited.length; i++) {
			if (visited[i]) {
				result += 1;
			}
		}
		return result;
	}
	
	public void printTransitionDiagram(String dirname) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(dirname + "/" + fitness + "_transitions"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		out.print(instance);
		out.close();
	}
	
	public void printToGraphViz(String dirname) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(dirname + "/" + fitness + ".gv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		out.println("digraph dfa{");
		out.println("rankdir = LR");

		if (instance.getTerminal() != null) {
			for (int i = 0; i < instance.getNumberOfStates(); i++) {
				if (instance.isStateTerminal(i)) {
					out.println("\"" + i + "\"" + "[label = \"" + stateLabels[i] + "\" style = bold];");
				} else {
					out.println("\"" + i + "\"" + "[label = \"" + stateLabels[i] + "\"];");
				}
			}
		} else {
			for (int i = 0; i < instance.getNumberOfStates(); i++) {
				out.println("\"" + i + "\"" + "[label = \"" + stateLabels[i] + "\"];");
			}
		}

		
		for (Transition tr : visitedTransitions) {
				out.println("\"" + tr.getStartState() + "\" -> \""
						+ tr.getEndState()
						+ "\" [label = \"" + tr.getEvent() + "/" + tr.getAction()
						+ "\"];");
		}
		
		out.print("}");
		out.close();
	}
	
//	public void printMetaData(String dirname) {
//		PrintWriter out = null;
//		try {
//			out = new PrintWriter(new File(dirname + "/" + fitness + "_metadata"));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		
//		out.println("fitness = " + fitness);
//		out.println("step-count = " + numberOfSteps);
//		out.println("time = " + time);
//		if (bestFitnessHistory != null) {
//			for (int i = 0; i < bestFitnessHistory.size(); i++) {
//				out.println(stepsHistory.get(i) + " "
//						+ bestFitnessHistory.get(i));
//			}
//		}
//		out.close();
//	}
	
	public void printNodeVisitStats(String dirname) {
		if (nodeVisitStats == null) {
			return;
		}
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(dirname + "/graph_stats"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int min = 0;
		int max = 0;
		double mean = 0;
		double sum = 0;
		
		for (Entry<Integer,Integer> e : nodeVisitStats.entrySet()) {
			if (e.getKey() < min) {
				min = e.getKey();
			} 
			if (e.getKey() > max) {
				max = e.getKey();
			}
			
			mean += e.getKey() * e.getValue();
			sum += e.getValue();
		}
		
		mean /= sum;
		
		out.println("min = " + min);
		out.println("mean = " + mean);
		out.println("max = " + max);
		
		for (Entry<Integer,Integer> e : nodeVisitStats.entrySet()) {
			out.println(e.getKey() + " " + e.getValue());
		}
		out.close();
	}
	
	@Override 
	public String toString() {
		return getFitness() + "";
	}

	@Override
	public void printProblemSpecificData(String dirname) {
		printToGraphViz(dirname);
		printTransitionDiagram(dirname);
	}
}
