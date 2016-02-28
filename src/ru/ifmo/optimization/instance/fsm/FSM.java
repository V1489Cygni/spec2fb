package ru.ifmo.optimization.instance.fsm;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.instance.Checkable;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Digest;
import ru.ifmo.util.Dijkstra;

/**
 * 
 * @author Daniil Chivilikhin
 *
 */
public class FSM extends AbstractFSM implements Comparable<FSM>, Checkable<FSM, FsmMutation> {
	public FSM.Transition[][] transitions;
	private boolean transitionUsed[][];
	private boolean transitionIsInCounterexample[][];
	private int numberOfStates;
	private int initialState = 0;
	private boolean[] terminal;
	public static List<String> EVENTS;
	public static int NUMBER_OF_EVENTS;
	
	public static void setEvents(List<String> taskEvents) {
		EVENTS = taskEvents;
		NUMBER_OF_EVENTS=EVENTS.size();
	}
	 
	public FSM(int numberOfStates) {
		this.numberOfStates = numberOfStates;
		this.transitions = new Transition[numberOfStates][NUMBER_OF_EVENTS];
		this.transitionUsed = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		this.transitionIsInCounterexample = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		for (int i = 0; i < numberOfStates; i++) {
			Arrays.fill(this.transitionUsed[i], false);
			Arrays.fill(this.transitionIsInCounterexample[i], false);
		}
		
		for (int i = 0; i < transitions.length; i++) {
			for (int j = 0; j < transitions[i].length; j++) {
				transitions[i][j] = new Transition(i, -1, EVENTS.get(j), null);
			}
		}
		terminal = new boolean[numberOfStates];
	}
	
	public FSM(int numberOfStates, FSM.Transition[][] tr) {
		this.numberOfStates = numberOfStates;
		this.transitions = tr;
		this.transitionUsed = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		this.transitionIsInCounterexample = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		for (int i = 0; i < numberOfStates; i++) {
			Arrays.fill(this.transitionUsed[i], false);
			Arrays.fill(this.transitionIsInCounterexample[i], false);
		}
		terminal = new boolean[numberOfStates];
	}
	
	public FSM(int numberOfStates, FSM.Transition[][] tr, boolean[] terminal) {
		this(numberOfStates, tr);
		this.terminal = Arrays.copyOf(terminal, terminal.length);
		for (int i = 0; i < numberOfStates; i++) {
			Arrays.fill(this.transitionUsed[i], false);
		}
	}

	public FSM(FSM other) {
		numberOfStates = other.numberOfStates;
		transitions = new FSM.Transition[numberOfStates][NUMBER_OF_EVENTS];
		transitionUsed = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		transitionIsInCounterexample = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < other.transitions[i].length; j++) {
				if (other.transitions[i][j] != null) {
					transitions[i][j] = new FSM.Transition(other.transitions[i][j]);
				}
				transitionUsed[i][j] = other.transitionUsed[i][j];
				transitionIsInCounterexample[i][j] = other.transitionIsInCounterexample[i][j];
 			}
		}
		terminal = Arrays.copyOf(other.terminal, other.terminal.length);
	}
	
	public FSM(int numberOfStates, Double[] realVector) {
		this.numberOfStates = numberOfStates;
		transitions = new FSM.Transition[numberOfStates][NUMBER_OF_EVENTS];
		transitionUsed = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		transitionIsInCounterexample = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		for (int i = 0; i < numberOfStates; i++) {
			Arrays.fill(this.transitionUsed[i], false);
			Arrays.fill(this.transitionIsInCounterexample[i], false);
		}
		terminal = new boolean[numberOfStates];
		int position = 0;
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
				Integer endState = FsmRealEncoder.stateEncoder().decode(realVector[position]);
				String action = FsmRealEncoder.actionEncoder().decode(realVector[position + 1]);
				transitions[i][j] = new FSM.Transition(i, endState, EVENTS.get(j), action);
				position += 2;
			}
		}
	}

	public FSM(String filename) {
		Scanner in = null;

		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		numberOfStates = in.nextInt();
		in.nextInt(); //number of events
		terminal = new boolean[numberOfStates];
		
		transitions = new FSM.Transition[numberOfStates][NUMBER_OF_EVENTS];
		transitionUsed = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		transitionIsInCounterexample = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		for (int i = 0; i < numberOfStates; i++) {
			Arrays.fill(this.transitionUsed[i], false);
			Arrays.fill(this.transitionIsInCounterexample[i], false);
		}
		String stringTerminal = in.next();
		for (int i = 0; i < numberOfStates; i++) {
			terminal[i] = (stringTerminal.charAt(i) == '0');
		}
		in.nextLine();
		
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
				transitions[i][j] = new FSM.Transition(i, -1, EVENTS.get(j), "");
			}
		}
		
		while (in.hasNext()) {
			String s = in.nextLine();
			String[] list = s.split(" - > ");
			for (int k = 0; k < list.length; k++) {
				list[k] = list[k].replace("(", "");
				list[k] = list[k].replace(")", "");
				list[k] = list[k].replace(",", "");
			}

			String[] startAndEvent = list[0].split(" ");
			String[] endAndAction = list[1].split(" ");

			int startState = Integer.parseInt(startAndEvent[0]);
			int endState = Integer.parseInt(endAndAction[0]);
			int event = Integer.parseInt(startAndEvent[1]);
			String action = "";
			if (endAndAction.length > 1) {
				action = endAndAction[1];
			}
			transitions[startState][event] = new FSM.Transition(startState, endState, event == -1 ? "-1" : EVENTS.get(event), action);
		}
	}
	
	private static String readFileAsString(String filePath) throws IOException {
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
        f.read(buffer);
        return new String(buffer);
    }

	
	public static FSM loadFromGV(String fp, int numberOfStates) throws IOException {
		String expr = "(\\d+) ?-> ?(\\d+) ?\\[label ?= ?\" ?(\\w+) ?\\[(.+)\\] \\((.*)\\) ?\"\\];";
        Pattern strPattern = Pattern.compile(expr);

        String target = readFileAsString(fp);
        Matcher matcher = strPattern.matcher(target);

        List<Integer> srcList = new ArrayList<Integer>();
        List<Integer> dstList = new ArrayList<Integer>();
        List<String> eventsList = new ArrayList<String>();
        List<String> actionsList= new ArrayList<String>();

        while (matcher.find()) {
            int srcNum = Integer.parseInt(matcher.group(1));
            srcList.add(srcNum);
            int dstNum = Integer.parseInt(matcher.group(2));
            dstList.add(dstNum);

            String event = matcher.group(3);
            eventsList.add(event + "[" + matcher.group(4).replace("~", "!") + "]");
            String actions = matcher.group(5);
            actionsList.add(actions);
        }
        
        FSM result = new FSM(numberOfStates);
        for (int i = 0; i < srcList.size(); i++) {
        	result.setTransition(srcList.get(i), eventsList.get(i), dstList.get(i), actionsList.get(i));
        }
        
        return result;
	}
	
	public FSM(AbstractOptimizationTask<FSM> t, Double[] position) {
		this(((AbstractAutomatonTask)t).getDesiredNumberOfStates(), position);
	}

    public boolean isFullyDefined() {
        for (int state = 0; state < getNumberOfStates(); state++) {
            for (int event = 0; event < getNumberOfEvents(); event++) {
                if (getTransition(state, event) == null) {
                    return false;
                }
            }
        }
        return true;
    }
	
	public void clearUsedTransitions() {
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
				transitionUsed[i][j] = false;
				transitionIsInCounterexample[i][j] = false;
			}
		}
	}
	
	public int getUsedTransitionsCount() {
		int result = 0;
		
		for (int i = 0; i < transitionUsed.length; i++) {
			for (int j = 0; j < transitionUsed[i].length; j++) {
				if (transitionUsed[i][j]) {
					result++;
				}
			}
		}
		return result;
	}
	
	public boolean isTransitionUsed(int state, int event) {
		return transitionUsed[state][event];
	}
	
	public boolean isTransitionUsed(Transition transition) {
		return transitionUsed[transition.startState][EVENTS.indexOf(transition.event)];
	}
	
	public boolean isTransitionInCounterexample(int state, int event) {
		return transitionIsInCounterexample[state][event];
	}
	
	public boolean isTransitionInCounterexample(Transition transition) {
		return transitionIsInCounterexample[transition.startState][EVENTS.indexOf(transition.event)];
	}
	 
	@Override
	public int getMaxNumberOfMutations() {
		int result = 0;
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
				if (transitionUsed[i][j]) {
					result++;
				}
			}
		}
		return result;
	};
	
	public List<Integer> getStartStatesOfUsedTransitions() {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
				if (transitionUsed[i][j]) {
					result.add(i);
					break;
				}
			}
		}
		return result;
	}
	
	public List<Integer> getEventsOfUsedTransitions(int startState) {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < NUMBER_OF_EVENTS; i++) {
			if (transitionUsed[startState][i]) {
				result.add(i);
			}
		}
		return result;
	}
	
	@Override
	public boolean needToComputeFitness(MutationCollection<FsmMutation> mutations) {
		List<Integer> usedStartStates = getStartStatesOfUsedTransitions();
		for (FsmMutation mutation : mutations.getMutations()) {
			if (mutation.isAddDelete()) {
				return true;
			}
			if (usedStartStates.contains(mutation.getStartState())) {
				if (getEventsOfUsedTransitions(mutation.getStartState()).contains(mutation.getEventId())) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void setFitnessDependentData(Object fitnessDependentData) {
		if (fitnessDependentData != null) {
			boolean fdd[][][] = (boolean[][][]) fitnessDependentData;
			setUsedTransitions(fdd[0]);
			setTransitionsInCounterexample(fdd[1]);
		} else {
			throw new RuntimeException("FDdata is null");
		}
	}
	
	@Override
	public Object getFitnessDependentData() {
		return new boolean[][][]{transitionUsed, transitionIsInCounterexample};
	}
	
	public void setUsedTransitions(boolean[][] otherTransitionUsed) {
		for (int i = 0; i < numberOfStates; i++) {
			this.transitionUsed[i] = Arrays.copyOf(otherTransitionUsed[i], otherTransitionUsed[i].length);
		}
	} 
	
	public void setTransitionsInCounterexample(boolean[][] otherTransitionsInCounterexample) {
		for (int i = 0; i < numberOfStates; i++) {
			this.transitionIsInCounterexample[i] = Arrays.copyOf(otherTransitionsInCounterexample[i], otherTransitionsInCounterexample[i].length);
		}
	} 
	
	public void markUsedTransitions(Collection<Transition> usedTransitions) {
		for (int i = 0; i < transitionUsed.length; i++) {
			Arrays.fill(transitionUsed[i], false);
		}
		for (Transition t : usedTransitions) {
			transitionUsed[t.getStartState()][EVENTS.indexOf(t.getEvent())] = true;
		}
	}
	
	public void markTransitionUsed(int state, int event) {
		transitionUsed[state][event] = true;
	}
	
	public boolean[][] getUsedTransitions() {
		return transitionUsed;
	}

	public List<String> getEvents() {
		return EVENTS;
	}
	
	@Override
	public void applyMutations(List<InstanceMutation<FSM>> mutations) {
		for (InstanceMutation<FSM> m : mutations) {
			m.apply(this);
		}
		//setting transitionsUsed to default just in case
		for (int i = 0; i < numberOfStates; i++) {
			Arrays.fill(this.transitionUsed[i], false);
			Arrays.fill(this.transitionIsInCounterexample[i], false);
		}
	}
	
	public int getNumberOfTransitions() {
		int result = 0;
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
				if (transitions[i][j].getEndState() != -1) {
					result++;
				}
			}
		}
		return result;
	}
	
	public int getNumberOfReachableStates() {
		Dijkstra dijsktra = new Dijkstra(this, 0);
		int[] weights = dijsktra.run();
		int numberOfReachableStates = 0;
		for (int i : weights) {
			if (i != Integer.MAX_VALUE) {
				numberOfReachableStates++;
			}
		}
		return numberOfReachableStates;
	}
	
	public int getDepth() {
		Dijkstra dijsktra = new Dijkstra(this, 0);
		int[] weights = dijsktra.run();
		int depth = 0;
		for (int i : weights) {
			if (i == Integer.MAX_VALUE) {
				continue;
			}
			depth = Math.max(depth, i);
		}
		return depth;
	}
	
	public boolean hasTransitionFromUtoV(int u, int v) {
		for (int i = 0; i < NUMBER_OF_EVENTS; i++) {
			if (transitions[u][i].getEndState() == v) {
				return true;
			}
		}
		return false;
	}

	public boolean isStateTerminal(int stateIndex) {
		return terminal[stateIndex]; 
	}
	
	public void setStateTerminal(int state, boolean terminal) {
		this.terminal[state] = terminal;
	}
	
	public boolean[] getTerminal() {
		return terminal;
	}
	
	@Override
	public int getInitialState() {
		return initialState;
	}

	@Override
	public int getNumberOfStates() {
		return numberOfStates;
	}

	public FSM.Transition getTransition(int state, int event) {
		return transitions[state][event];
	}
	
	public void setTransition(int startState, String event, int endState, String action) {
		transitions[startState][EVENTS.indexOf(event)] = new FSM.Transition(startState, endState, event, action);
	}

	@Override
	public void setInitialState(int state) {
		this.initialState = state;
	}

	@Override
	public String toString() {
		String a = numberOfStates + " " + NUMBER_OF_EVENTS + "\n";
		for (boolean b : terminal) {
			a += b ? 0 : 1;
		}
		a += "\n";
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < transitions[i].length; j++) {
				a += "(" + i + ", " + j + ") - > " + transitions[i][j] + "\n";
			}
		}
		return a;
	}
	
	public Double[] toRealVector() {
		Double[] result = new Double[2 *  NUMBER_OF_EVENTS * numberOfStates];
		int position = 0;
		for (int state = 0; state < numberOfStates; state++) {
			for (int event = 0; event < NUMBER_OF_EVENTS; event++) {
				result[position++] = FsmRealEncoder.stateEncoder().encode(transitions[state][event].getEndState());
				result[position++] = FsmRealEncoder.actionEncoder().encode(transitions[state][event].getAction());
			}
		}
		return result;
	}
	
	public void printTransitionDiagram(String filename) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		out.print(toString());
		out.close();
	}
	
	private String stringForHashing() {
		StringBuilder sb = new StringBuilder();
		for (boolean b : terminal) {
			sb.append(b ? 0 : 1);
		}
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < transitions[i].length; j++) {
				Transition t = transitions[i][j];
				sb.append(t.getEvent());
				sb.append(t.getEndState());
				String a = t.getAction();
				if (a == null) {
					sb.append("n");
					continue;
				}
				if (a.length() == 0) {
					sb.append("n");
				}  else {
					sb.append(a);
				}
			}
		}
		return sb.toString();
	}
	
	public Long computeStringHash() {
		return Digest.RSHash(stringForHashing());
	}
	
	private static boolean transitionsContainTransition(Collection<Transition> transitions, int state, String event) {
		for (Transition t : transitions) {
			if (t.getStartState() == state && t.getEvent() == event) {
				return true;
			}
		}
		return false;
	}
	
	public int dist(FSM other, 
			Collection<Transition> transitions1, 
			Collection<Transition> transitions2) {
		
		if (numberOfStates != other.getNumberOfStates()) {
			return Integer.MAX_VALUE;
		}
		int result = 0;
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < transitions[i].length; j++) {
				Transition thisTransition = transitions[i][j];
				Transition otherTransition = other.getTransition(i, j);
				if (!transitionsContainTransition(transitions1, i, EVENTS.get(j)) || !transitionsContainTransition(transitions2, i, EVENTS.get(j))) {
					continue;
				}
				if (thisTransition.getEndState() != otherTransition.getEndState()) {
					result++;
				}
				if (!thisTransition.getAction().equals(otherTransition.getAction())) {
					result++;
					continue;
				}
			}
		}
		return result;
	}
	
	public boolean hasTransition(int state, int event) {
		if (transitions[state].length < event + 1) {
			return false;
		}
		return transitions[state][event] != null;
	}
	
	public int dist(FSM other) { 
		if (numberOfStates != other.getNumberOfStates()) {
			return Integer.MAX_VALUE;
		}
		int result = 0;
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < transitions[i].length; j++) {
				if (!hasTransition(i, j) && other.hasTransition(i, j)) {
					result++;
					continue;
				}
				if (hasTransition(i, j) && !other.hasTransition(i, j)) {
					result++;
					continue;
				}
				
				if (!hasTransition(i, j) && !other.hasTransition(i, j)) {
					continue;
				}
				
				FSM.Transition thisTransition = transitions[i][j];
				FSM.Transition otherTransition = other.getTransition(i, j);
				
				if (thisTransition == null && otherTransition != null || 
						thisTransition != null && otherTransition == null) {
					result++;
					continue;
				}
				
				if (thisTransition == null && otherTransition == null) {
					continue;
				}
				
				if (thisTransition.getEndState() != otherTransition.getEndState()) {
					result++;
					continue;
				}
				
				if (thisTransition.getAction() == null && otherTransition.getAction() != null ||
						thisTransition.getAction() != null && otherTransition.getAction() == null) {
					result++;
					continue;
				}
				
				if (thisTransition.getAction() == null && otherTransition.getAction() == null) {
					continue;
				}
				
				if (!thisTransition.getAction().equals(otherTransition.getAction())) {
					result++;
					continue;
				}
			}
		}
		return result;
	}

	
	@Override
	public int compareTo(FSM other) {
		return computeStringHash().compareTo(other.computeStringHash());
	}
	
	@Override 
	public boolean equals(Object o) {
		FSM other = (FSM)o;
		return computeStringHash().longValue() == other.computeStringHash().longValue();
	}
	
	public int getNumberOfEvents() {
		return NUMBER_OF_EVENTS;
	}
	
	public FSM getCanonicalFSM() {
		int newId[] = new int[getNumberOfStates()];
		Arrays.fill(newId, -1);
		return getCanonicalFSM(newId);
	}
	
	public int[] getBfsStateMapping() {
		int[] newId = new int[numberOfStates];
		Arrays.fill(newId, -1);
		Queue<Integer> queue = new LinkedList<Integer>();
		int id = 0;
		queue.add(initialState);
		newId[initialState] = id++;
		while (!queue.isEmpty()) {
			int currentState = queue.poll();
			for (int eventId = 0; eventId < EVENTS.size(); eventId++) {
				if (transitions[currentState][eventId] == null) {
					continue;
				}
				int childId = transitions[currentState][eventId].getEndState();
				if (childId == -1) {
					continue;
				}
				if (newId[childId] == -1) {
					newId[childId] = id++;
					queue.add(childId);
				}
			}
		}
		return newId;
	}
	
	public FSM getCanonicalFSM(int newId[]) {
		newId = getBfsStateMapping();
		return transformTransitions(newId);
	}
	
	public FSM transformTransitions(int newId[]) {
		FSM result = new FSM(getNumberOfStates());
		for (int i = 0; i < getNumberOfStates(); i++) {
			if (newId[i] != -1) {
				result.transitions[newId[i]] = new Transition[transitions[i].length];
				for (int j = 0; j < transitions[i].length; j++) {
					Transition t = transitions[i][j];
					if (t.getStartState() == -1 || t.getEndState() == -1) {
						result.transitions[newId[i]][j] = new Transition(-1, -1, t.getEvent(), "1");
					} else {
						result.transitions[newId[i]][j] = new Transition(newId[t.getStartState()], newId[t.getEndState()], 
								EVENTS.get(j), newId[t.getEndState()] != -1 ? t.getAction() : "1");
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param newId1 - mapping of states from first FSM (originalFSM) to the canonical FSM 
	 * @param newId2 - mapping of states from second fSM to the canonical FSM
	 * @param originalFSM - the original FSM retrieved from canonical cache
	 */
	public void transformUsedTransitions(int[] newId1, int[] newId2, FSM originalFSM) {
		int oldId2[] = new int[getNumberOfStates()];
		Arrays.fill(oldId2, -1);
		for (int j = 0; j < newId2.length; j++) {
			if (newId2[j] != -1) {
				oldId2[newId2[j]] = j;
			}
		}
		
		int f12[] = new int[getNumberOfStates()];
		Arrays.fill(f12, -1);
		for (int j = 0; j < f12.length; j++) {
			if (newId1[j] == -1) {
			} else {
				f12[j] = oldId2[newId1[j]];
			}
		}	
		
		for (int i = 0; i < getNumberOfStates(); i++) {
			Arrays.fill(transitionUsed[i], false);
		}
		
		for (int i = 0; i < getNumberOfStates(); i++) {
			if (f12[i] != -1) {
				for (int j = 0; j < transitionUsed[i].length; j++) {
					transitionUsed[f12[i]][j] = originalFSM.transitionUsed[i][j];
				}
			}
		}
	}
	
	public Collection<Transition> getTransitions() {
		Collection<Transition> result = new ArrayList<Transition>();
		for (Transition[] tr : transitions) {
			for (Transition t : tr) {
				result.add(t);
			}
		}
		return result;
	}
	
	public MutationCollection<FsmMutation> getMutations(FSM other) {
		MutationCollection<FsmMutation> mutations = new MutationCollection<FsmMutation>();
		
		for (int state = 0; state < numberOfStates; state++) {
			for (int event = 0; event < transitions[state].length; event++) {
				if (!hasTransition(state, event) && !other.hasTransition(state, event)) {
					continue;
				}
				//delete transition mutation
				if (hasTransition(state, event) && !other.hasTransition(state, event)) {
					mutations.add(new FsmTransitionMutation(state, event, -1, null));
					continue;
				}
				
				//add transition mutation
				if (!hasTransition(state, event) && other.hasTransition(state, event)) {
					mutations.add(new FsmTransitionMutation(state, event, 
							other.transitions[state][event].getEndState(), 
							other.transitions[state][event].getAction()));
					continue;
				}
				
				//both transitions are the same
				if (transitions[state][event].equals(other.transitions[state][event])) {
					continue;
				}
				
				//change transition mutation
				mutations.add(new FsmTransitionMutation(state, event, 
						other.transitions[state][event].getEndState(),
						other.transitions[state][event].getAction()));
			}
		}
		return mutations;
	}
	
	public void dropLabels() {
		for (int state = 0; state < numberOfStates; state++) {
			for (Transition t : transitions[state]) {
				t.setAction("1");
			}
		}
	}

	public static class Transition implements AbstractFSM.Transition {
		private int startState;
		private int endState;
		private String event;
		private String action;

		public Transition(int startState, int endState, String event, String action) {
			this.startState = startState;
			this.endState = endState;
			this.event = event;
			this.action = action;
		}

		public Transition(Transition other) {
			startState = other.startState;
			endState = other.endState;
			event = other.event;
			action = other.action;
		}

		@Override
		public boolean equals(Object o) {
			FSM.Transition other = (FSM.Transition)o;
			if (startState != other.startState) {
				return false;
			}
			if (endState != other.endState) { 
				return false;
			}
			
			if (action == null && other.action != null ||
					action != null && other.action == null) {
				return false;
			}
			
			if (action == null && other.action == null) {
				return true;
			}
			
			return action.equals(other.action);
		}
		
		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		@Override
		public int getEndState() {
			return endState;
		}

		@Override
		public void setEndState(int state) {
			this.endState = state;
		}

		@Override
		public String toString() {
			return "(" + endState + ", " + action + ")";
		}

		@Override
		public int getStartState() {
			return startState;
		}
		
		public void setStartState(int startState) {
			this.startState = startState;
		}
		
		@Override
		public int hashCode() {
			return Digest.hash("" + startState + ";" + event + ";" + endState + ";" + action);
		}

		@Override
		public String getActions() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public void setEvent(String event) {
			this.event = event;
		}
		
		public String getEvent() {
			return event;
		}
	}

	@Override
	public FSM copyInstance(FSM other) {
		return new FSM(other);
	}
	
	public static void main(String[] args) {
//		AbstractAutomatonTask task = new TsarevSmartAntTask(new AbstractTaskConfig("santa-fe.properties"));
//		
//		InitialFSMGenerator initialFSMGenerator = new InitialFSMGenerator();
//
//		for (int i = 0; i < 10000; i++) {
//			FSM fsm = initialFSMGenerator.createInstance(task);
//			FSM canonicalFSM = fsm.getCanonicalFSM();
//			
//			FitInstance<FSM> plain = task.getFitInstance(fsm, 0);
//			FitInstance<FSM> canonical = task.getFitInstance(canonicalFSM, 0);
//			
//			if (Math.abs(plain.getFitness() - canonical.getFitness()) > 0.01) {
//				System.err.println("BAD: " + plain.getFitness() + " != " + canonical.getFitness());
//				System.exit(1);
//			}
//		}
//		
//		System.out.println("OK!");
		
		////
//		
//		AbstractAutomatonTask task = new TsarevSmartAntTask(new AbstractTaskConfig("santa-fe.properties"));
//		InitialFSMGenerator initialFSMGenerator = new InitialFSMGenerator();
//		FSM.setEvents(task.getEvents());
//		
//		int hits = 0;
//		for (int i = 0; i < 10000; i++) {
//			FSM fsm = initialFSMGenerator.createInstance(8, task.getEvents(), task.getActions(), new AutomatonTaskConstraints());
//			int newId1[] = new int[fsm.getNumberOfStates()];
//			FSM canonicalFSM = fsm.getCanonicalFSM(newId1);
//
//			ChangeFinalStateMutator m1 = new ChangeFinalStateMutator();
//			FSM otherFSM = m1.applySimple(m1.applySimple(fsm));
//			int newId2[] = new int[fsm.getNumberOfStates()];
//			FSM otherCanonicalFSM = otherFSM.getCanonicalFSM(newId2);
//			
//			if (!canonicalFSM.computeStringHash().equals(otherCanonicalFSM.computeStringHash())) {
//				continue;
//			}
//			
//			if (Math.abs(task.getFitInstance(canonicalFSM, 0).getFitness() - task.getFitInstance(otherCanonicalFSM, 0).getFitness()) > 1e-6) {
//				System.err.println("Canonization error!");
//				System.exit(1);
//			}
//			
//			int oldId2[] = new int[fsm.getNumberOfStates()];
//			Arrays.fill(oldId2, -1);
//			for (int j = 0; j < newId2.length; j++) {
//				if (newId2[j] != -1) {
//					oldId2[newId2[j]] = j;
//				}
//			}
//			
//			if (Math.abs(task.getFitInstance(otherCanonicalFSM.transformTransitions(oldId2), 0).getFitness() - task.getFitInstance(otherFSM, 0).getFitness()) > 1e-6) {
//				System.err.println("Error!");
//				System.exit(1);
//			}
//			
//			
//			
//			int f12[] = new int[fsm.getNumberOfStates()];
//			Arrays.fill(f12, -1);
//			for (int j = 0; j < f12.length; j++) {
//				if (newId1[j] == -1) {
//				} else {
//					f12[j] = oldId2[newId1[j]];
//				}
//			}
//			
//			FSM transformedFSM = fsm.transformTransitions(f12);
////			FSM transformedFSM = fsm.transformTransitions(newId1).transformTransitions(oldId2);
//			
//			double fitness1 = task.getFitInstance(fsm, 0).getFitness();
//			double fitness2 = task.getFitInstance(transformedFSM, 0).getFitness();
//			if (Math.abs(fitness1 - fitness2) > 1e-6) {
//				System.err.println(fitness1 + " != " + fitness2);
//				System.exit(1);
//			}
//			hits++;
//		}
//		
//		System.out.println("hits = " + hits);
		
		
		////
//		
//		ChangeFinalStateMutator mutator = new ChangeFinalStateMutator();
//		MutatedInstanceMetaData<FSM> mutatedInstance = mutator.apply(canonicalFSM);
//		FSM mutatedFSM = mutatedInstance.getInstance();
//		FSM canonicalMutatedFSM = mutatedFSM.getCanonicalFSM();
//		System.out.println(canonicalFSM.dist(canonicalMutatedFSM));
//		
//		MutationCollection<FSM> mutations = canonicalFSM.getMutations(canonicalMutatedFSM);
//		
//		System.out.println("initial FSM: " + canonicalFSM);
//		
//		for (InstanceMutation<FSM> m : mutations.getMutations()) {
//			System.out.println(m);
//		}
//		
//		System.out.println("canonical mutated FSM: " + canonicalMutatedFSM);
//		
//		
//		canonicalFSM.applyMutations(mutations.getMutations());
//		
//		System.out.println("rebuilt FSM: " + canonicalFSM);
//		
//		
//		System.out.println(canonicalFSM.equals(canonicalMutatedFSM));
	}
}
