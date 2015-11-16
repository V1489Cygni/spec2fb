package ru.ifmo.optimization.instance.fsm.task.synchronous;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;

public class SynchronousFsmGenerator implements Runnable {
	
	private class Element {
		private double clock;
		private int input;
		private int output;
		private String state;
		
		public Element(double clock, int input, int output, String state) {
			this.clock = clock;
			this.input = input;
			this.output = output;
			this.state = state;
		}
		
		@Override
		public String toString() {
			return clock + " " + input + " " + output + " " + state;
		}
	}
	
	private List<Element> data;
	private Map<String, Integer> stateCode = new HashMap<String, Integer>();
	private Set<String> inputs = new HashSet<String>();
	private Set<String> outputs = new HashSet<String>();

	public SynchronousFsmGenerator(String dataFile) {
		Scanner in = null;
		try {
			in = new Scanner(new File(dataFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		data = new ArrayList<Element>();
		
		List<Element> buf = new ArrayList<Element>();
		while (in.hasNext()) {
			in.next();
			double clock = Double.parseDouble(in.next());
			double input = Double.parseDouble(in.next());
			double output = Double.parseDouble(in.next());
			String state = Math.round(Double.parseDouble(in.next())) + "" + Math.round(Double.parseDouble(in.next())) + "" + Math.round(Double.parseDouble(in.next()));
			in.next();
			
			if (clock > 1.0 - 1e-10) {
				buf.add(new Element(clock, (int)Math.round(input), (int)Math.round(output), state));
				continue;
			} 
			
			if (!buf.isEmpty()) {
				Element e = buf.get(buf.size() / 2 - 1);
				data.add(e);
				if (!stateCode.containsKey(e.state)) {
					stateCode.put(e.state, stateCode.size());
				}
				inputs.add(e.input + "");
				outputs.add(e.output + "");
				buf.clear();
			}
		}
		in.close();
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File("sync-scenarios"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (Element e : data) {
			out.print(e.input + "/" + e.output + "; ");
		}
		
		out.close();
	}
	
	@Override
	public void run() {
		List<String> events = new ArrayList<String>();
		events.addAll(inputs);
		FSM.EVENTS = events;
		FSM.NUMBER_OF_EVENTS = inputs.size();
		FSM fsm = new FSM(stateCode.size());
		String currentState = "000";
		int num = 0;
		for (Element e : data) {
			int from = stateCode.get(currentState);
			int to = stateCode.get(e.state);
			String event = e.input + "";
			int eventId = events.indexOf(event);
			String output = e.output + "";
			if (fsm.transitions[from][eventId].getEndState() != -1) {
				Transition oldTransition = fsm.transitions[from][eventId]; 
				if (!oldTransition.getAction().equals(output)) {
					System.err.println(num + ": Inconsistent output: old=" + oldTransition.getAction() + ", new=" + output);
				}
				if (oldTransition.getEndState() != to) {
					System.err.println(num + ": Inconsistent destination state: old=" + oldTransition.getEndState() + ", new=" + to);
				}
			} else {
				fsm.transitions[from][eventId] = new Transition(from, to, event, output);
			}
			currentState = e.state;
			num++;
		}
		
		new FsmMetaData(fsm, fsm.getTransitions(), 0).printToGraphViz(".");
		
		for (Entry<String, Integer> e : stateCode.entrySet()) {
			System.out.println(e.getValue() + " -> " + e.getKey());
		}
	}

	
	public static void main(String args[]) {
		String dataFile = "fsm_5_state.txt";
		new SynchronousFsmGenerator(dataFile).run();
	}
}
