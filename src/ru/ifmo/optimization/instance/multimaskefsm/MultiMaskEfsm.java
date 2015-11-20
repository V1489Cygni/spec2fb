package ru.ifmo.optimization.instance.multimaskefsm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiMaskEfsm {
    private static Pattern transitionPattern = Pattern.compile("^([0-9]+)\\s->\\s([0-9]+)\\s\\[label\\s=\\s\"([A-Z]+)\\s\\[(.+)\\] \\(\\)\"\\];$");
    private static Pattern statePattern = Pattern.compile("^([0-9]+)\\s\\[label=\"s_([01x]+(_[0-9]+)?)\\(([A-Z]*)\\)\"\\];$");
    private MultiMaskEfsmSkeleton skeleton;
    private OutputAction[] actions;

    public MultiMaskEfsm(MultiMaskEfsmSkeleton skeleton) {
        this.skeleton = new MultiMaskEfsmSkeleton(skeleton);
        actions = new OutputAction[MultiMaskEfsmSkeleton.STATE_COUNT];
    }

    public MultiMaskEfsm(MultiMaskEfsmSkeleton skeleton, OutputAction[] actions) {
        this.skeleton = new MultiMaskEfsmSkeleton(skeleton);
        this.actions = Arrays.copyOf(actions, actions.length);
    }

    public MultiMaskEfsm(String filename) {
        State[] states = new State[MultiMaskEfsmSkeleton.STATE_COUNT];
        actions = new OutputAction[MultiMaskEfsmSkeleton.STATE_COUNT];
        Map<String, Integer>[] transitionGroupMap = new HashMap[MultiMaskEfsmSkeleton.STATE_COUNT];

        for (int stateId = 0; stateId < transitionGroupMap.length; stateId++) {
            states[stateId] = new State();
            transitionGroupMap[stateId] = new HashMap<String, Integer>();
        }

        Scanner in = null;
        try {
            in = new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (in.hasNext()) {
            String line = in.nextLine();
            if (!line.contains("->")) {
                Matcher m = statePattern.matcher(line);
                m.find();
                if (!m.matches()) {
                    continue;
                }
                int state = Integer.parseInt(m.group(1));
                String algorithm = m.group(2);
                String outputEvent = m.group(4);
                actions[state] = new OutputAction(algorithm, outputEvent);
                continue;
            }
            Matcher m = transitionPattern.matcher(line);
            m.find();
            if (!m.matches()) {
                continue;
            }
            int startState = Integer.parseInt(m.group(1));
            int endState = Integer.parseInt(m.group(2));
            String event = m.group(3);
            String formula = m.group(4);

            List<Integer> meaningfulPredicatesList = new ArrayList<Integer>();
            StringBuilder inputVariables = new StringBuilder();
            StringBuilder meaningfulPredicates = new StringBuilder();

            //convert formula to bit string
            for (String v : formula.split(" & ")) {
                int value = v.contains("!") ? 0 : 1;
                inputVariables.append(value);
                String predicateName = v.contains("!") ? v.replace("!", "") : v;
                int predicateId = MultiMaskEfsmSkeleton.PREDICATE_NAMES.indexOf(predicateName);
                meaningfulPredicates.append(predicateId + ",");
                meaningfulPredicatesList.add(predicateId);
            }

            int eventIndex = MultiMaskEfsmSkeleton.INPUT_EVENTS.get(event);

            //determine transition group
            int transitionGroupId = -1;
            if (transitionGroupMap[startState].containsKey(meaningfulPredicates.toString())) {
                transitionGroupId = transitionGroupMap[startState].get(meaningfulPredicates.toString());
            } else {
                transitionGroupId = transitionGroupMap[startState].size();
                if (transitionGroupId >= MultiMaskEfsmSkeleton.MEANINGFUL_PREDICATES_COUNT) {
                    throw new RuntimeException("Wrong number of transitionGroups in state " + startState);
                }
                transitionGroupMap[startState].put(meaningfulPredicates.toString(), transitionGroupId);
            }

            //if transition group is empty, initialize its mask
            if (states[startState].getTransitionGroup(eventIndex, transitionGroupId) == null) {
                states[startState].addTransitionGroup(event, new TransitionGroup(meaningfulPredicates.length()));
                for (Integer predicateId : meaningfulPredicatesList) {
                    states[startState].getTransitionGroup(eventIndex, transitionGroupId).setMaskElement(predicateId, true);
                }
            }

            int transitionIndex = Integer.valueOf(inputVariables.toString(), 2);
            states[startState].getTransitionGroup(eventIndex, transitionGroupId).setNewState(transitionIndex, endState);
            states[startState].getTransitionGroup(eventIndex, transitionGroupId).setTransitionUsed(transitionIndex);
        }

        in.close();

//		//check that all states have all needed data
//		for (int state = 0; state < states.length; state++) {
//			for (String inputEvent : MultiMaskEfsmSkeleton.INPUT_EVENTS.keySet()) {
//				int eventIndex = MultiMaskEfsmSkeleton.INPUT_EVENTS.get(inputEvent);
//				for (int tgId = 0; tgId < MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT; tgId++) {
//					if (states[state].getTransitionGroup(eventIndex, tgId) == null) {
//						states[state].addTransitionGroup(inputEvent, new TransitionGroup(1));
//						Set<Integer> meaningfulPredicates = new HashSet<Integer>();
//						meaningfulPredicates.add(RandomProvider.getInstance().nextInt(MultiMaskEfsmSkeleton.PREDICATE_COUNT));
//						for (Integer predicateId : meaningfulPredicates) {
//							states[state].getTransitionGroup(eventIndex, tgId).setMaskElement(predicateId, true);
//						}
//						continue;
//					}
//					if (states[state].getTransitionGroup(eventIndex, tgId).getMeaningfulPredicateIds().isEmpty()) {
//						Set<Integer> meaningfulPredicates = new HashSet<Integer>();
//						while (meaningfulPredicates.size() < MultiMaskEfsmSkeleton.MEANINGFUL_PREDICATES_COUNT) {
//							meaningfulPredicates.add(RandomProvider.getInstance().nextInt(MultiMaskEfsmSkeleton.PREDICATE_COUNT));
//						}
//						for (Integer predicateId : meaningfulPredicates) {
//							states[state].getTransitionGroup(eventIndex, tgId).setMaskElement(predicateId, true);
//						}
//					}
//				}
//			}
//		}


        skeleton = new MultiMaskEfsmSkeleton(states);
        skeleton.removeNullTransitionGroups();
    }

    public void markTransitionsUnused() {
        skeleton.markTransitionsUnused();
    }

    public MultiMaskEfsmSkeleton getSkeleton() {
        return skeleton;
    }

    public void setSkeleton(MultiMaskEfsmSkeleton skeleton) {
        this.skeleton = skeleton;
    }

    public int getInitialState() {
        return skeleton.getInitialState();
    }

    public int getNewState(int state, String inputEvent, String variableValues) {
        int newState = skeleton.getNewState(state, inputEvent, variableValues);
        return newState;
    }

    public void setActions(int state, OutputAction actions) {
        this.actions[state] = actions;
    }

    public OutputAction getActions(int state) {
        return actions[state];
    }

    public State getState(int stateId) {
        return skeleton.getState(stateId);
    }

    public String toGraphvizString() {
        StringBuilder sb = new StringBuilder();
        Map<Integer, Integer> stateIdMap = new HashMap<Integer, Integer>();
        Map<String, Integer> labelSet = new HashMap<String, Integer>();

        int stateCounter = 0;
        sb.append("digraph efsm{\n");
        for (int state = 0; state < actions.length; state++) {
            if (skeleton.stateUsedInTransitions(state)) {
                String label = actions[state].getAlgorithm();
                if (labelSet.containsKey(label)) {
                    labelSet.put(label, labelSet.get(label) + 1);
                    label = label + "_" + labelSet.get(label);
                } else {
                    labelSet.put(label, 1);
                }
                sb.append(stateCounter + " [label=\"s_" + label + "(" + actions[state].getOutputEvent() + ")" + "\"];\n");
                stateIdMap.put(state, stateCounter);
                stateCounter++;
            }
        }
        sb.append(skeleton.toGraphvizString(stateIdMap));
        sb.append("}");

        return sb.toString();
    }

    public OutputAction[] getActions() {
        return actions;
    }
}
