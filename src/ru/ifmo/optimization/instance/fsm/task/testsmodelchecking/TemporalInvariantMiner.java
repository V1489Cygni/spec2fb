package ru.ifmo.optimization.instance.fsm.task.testsmodelchecking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;

public class TemporalInvariantMiner {
	private AutomatonTest[] tests;
	private List<String> events;
	private List<String> actions;
	private int[] eventOccurences;
	private int[][] eventFollowsEvent;
	private int[][] eventPrecedesEvent;
	
	public TemporalInvariantMiner(AutomatonTest[] tests, List<String> events, List<String> actions) {
		this.tests = tests;
		this.events = new ArrayList<String>();
		for (String in: events) {
			String event = in.split("\\[")[0];
			if (this.events.contains(event)) {
				continue;
			}
			this.events.add(event);
		}
		this.actions = actions;
		eventOccurences = new int[events.size()];
		eventFollowsEvent = new int[events.size()][events.size()];
		eventPrecedesEvent = new int[events.size()][events.size()];
		Arrays.fill(eventOccurences, 0);
		for (int i = 0; i < eventFollowsEvent.length; i++) {
			eventFollowsEvent[i] = new int[events.size()];
			eventPrecedesEvent[i] = new int[events.size()];
			Arrays.fill(eventFollowsEvent[i], 0);
			Arrays.fill(eventPrecedesEvent[i], 0);
		}
	}
	
	public void mine() {
		for (AutomatonTest test : tests) {
			Set<String> occurredEvents = new HashSet<String>();
			for (String in : test.getInput()) {		
				String event = in.split("\\[")[0];
				eventOccurences[events.indexOf(event)]++;
				
				for (String occurredEvent : occurredEvents) {
					if (occurredEvent.equals(event)) {
						continue;
					}
					eventFollowsEvent[events.indexOf(occurredEvent)][events.indexOf(event)]++;
				}
				occurredEvents.add(event);
			}
			
			occurredEvents.clear();
			for (int i = test.getInput().length - 1; i >=0; i--) {
				String event = test.getInput()[i].split("\\[")[0];
				
				for (String occurredEvent : occurredEvents) {
					if (occurredEvent.equals(event)) {
						continue;
					}
					eventPrecedesEvent[events.indexOf(occurredEvent)][events.indexOf(event)]++;
				}
				
				occurredEvents.add(event);
			}
		}
		
		//determine likely invariants
		for (int i = 0; i < events.size(); i++) {
			for (int j = 0; j < events.size(); j++) {
				if (i == j) {
					continue;
				}
				
				if (eventFollowsEvent[i][j] == eventOccurences[i]) {
					System.out.println("invariant: G(" + events.get(i) + " -> F(" + events.get(j) + "))");
				}
				
				if (eventFollowsEvent[i][j] == 0) {
					System.out.println("invariant: G(!(" + events.get(i) + " -> F(" + events.get(j) + ")))");
				}
				
				if (eventPrecedesEvent[i][j] == eventOccurences[j]) {
					System.out.println("invariant: " + events.get(i) + " <- " + events.get(j));
				}
			}
		}
		
	}
	
}
