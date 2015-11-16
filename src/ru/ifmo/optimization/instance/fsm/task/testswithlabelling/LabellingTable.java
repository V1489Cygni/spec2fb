package ru.ifmo.optimization.instance.fsm.task.testswithlabelling;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LabellingTable {
//	Map<String, Integer> table[][];
	Map<String, Double> table[][];

	public LabellingTable(int numberOfStates, int numberOfEvents) {
		table = new Map[numberOfStates][numberOfEvents];
	}
	
	public void add(int state, int event, String sequence) {
		Map<String, Double> tableForTransition = table[state][event];
		if (tableForTransition == null) {
			tableForTransition = new HashMap<String, Double>();
			tableForTransition.put(sequence, 1.0);
			table[state][event] = tableForTransition;
			return;
		}

		Double count = tableForTransition.get(sequence);
		if (count == null) {
			tableForTransition.put(sequence, 1.0);
			return;
		} 
		
		tableForTransition.put(sequence, count + 1);
	}
	
	public void add(int state, int event, String sequence, double weight) {
		
		Map<String, Double> tableForTransition = table[state][event];
		if (tableForTransition == null) {
			tableForTransition = new HashMap<String, Double>();
			tableForTransition.put(sequence, weight);
			table[state][event] = tableForTransition;
			return;
		}

		Double count = tableForTransition.get(sequence);
		if (count == null) {
			tableForTransition.put(sequence, weight);
			return;
		} 
		
		tableForTransition.put(sequence, count + weight);
	}
	
	public String getActions(int state, int event) {
		Map<String, Double> tableForTransition = table[state][event];
		if (tableForTransition == null) {
			return "";
		}
		double max = Double.MIN_VALUE;
		String sequence = null;
		for (Entry<String, Double> e : tableForTransition.entrySet()) {
			if (e.getValue() > max) {
				max = e.getValue();
				sequence = e.getKey();
			}
		}
		return sequence;
	}
} 
