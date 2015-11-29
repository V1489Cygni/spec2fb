package ru.ifmo.optimization.instance.multimaskefsm;

import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.multimaskefsm.task.VarsActionsScenario;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.util.Digest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MultiMaskEfsmSkeleton implements Constructable<MultiMaskEfsmSkeleton>, Serializable {
    public static int STATE_COUNT;
    public static int PREDICATE_COUNT;
    public static int INPUT_EVENT_COUNT;
    public static int MEANINGFUL_PREDICATES_COUNT;
    public static int TRANSITION_GROUPS_COUNT;
    public static Map<String, Integer> INPUT_EVENTS;
    public static List<String> PREDICATE_NAMES;
    private final List<VarsActionsScenario> counterExamples = new ArrayList<>();
    private int initialState;
    private State[] states;
    private double fitness;

    public MultiMaskEfsmSkeleton() {
        states = new State[STATE_COUNT];
    }

    public MultiMaskEfsmSkeleton(State[] states) {
        this.states = states;
    }

    public MultiMaskEfsmSkeleton(MultiMaskEfsmSkeleton other) {
        states = new State[STATE_COUNT];
        this.initialState = other.initialState;
        counterExamples.addAll(other.counterExamples);
        fitness = other.fitness;
        for (int i = 0; i < states.length; i++) {
            states[i] = new State(other.states[i]);
        }
    }

    public static String tranIdToLabel(int tranId, List<Integer> meaningfulPredicates) {
        String f = Integer.toBinaryString(tranId);
        while (f.length() < meaningfulPredicates.size()) {
            f = "0" + f;
        }
        StringBuilder formula = new StringBuilder();
        for (int i = 0; i < f.length(); i++) {
            if (f.charAt(i) == '0') {
                formula.append("!");
            }
            formula.append(MultiMaskEfsmSkeleton.PREDICATE_NAMES.get(meaningfulPredicates.get(i)));
            if (i < f.length() - 1) {
                formula.append(" & ");
            }
        }
        return formula.toString();
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public List<VarsActionsScenario> getCounterExamples() {
        return counterExamples;
    }

    public void clearCounterExamples() {
        counterExamples.clear();
    }

    public void addCounterExample(VarsActionsScenario scenario) {
        counterExamples.add(scenario);
    }

    public void markTransitionsUnused() {
        for (int i = 0; i < states.length; i++) {
            states[i].markTransitionsUnused();
        }
    }

    public int getInitialState() {
        return initialState;
    }

    public void setInitialState(int initialState) {
        this.initialState = initialState;
    }

    public int getNewState(int state, String inputEvent, String variableValues) {
        return states[state].getNewState(inputEvent, variableValues);
    }

    public TransitionGroup getNewStateTransitionGroup(int state, String inputEvent, String variableValues) {
        return states[state].getNewStateTransitionGroup(inputEvent, variableValues);
    }

    public State getState(int state) {
        return states[state];
    }

    public boolean hasTransitions(int state) {
        return states[state].getUsedTransitionsCount() > 0;
    }

    public boolean stateUsedInTransitions(int state) {
        if (states[state].getUsedTransitionsCount() > 0) {
            return true;
        }

        for (int otherState = 0; otherState < MultiMaskEfsmSkeleton.STATE_COUNT; otherState++) {
            if (otherState == state) {
                continue;
            }
            for (int eventId = 0; eventId < MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT; eventId++) {
                for (int tgId = 0; tgId < MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT; tgId++) {
                    TransitionGroup tg = states[otherState].getTransitionGroup(eventId, tgId);
                    for (int tranId = 0; tranId < tg.getTransitionsCount(); tranId++) {
                        if (tg.isTransitionUsed(tranId) && tg.getNewState(tranId) == state) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private String stringForHashing() {
        StringBuilder sb = new StringBuilder();
        for (State state : states) {
            sb.append(state);
        }
        return sb.toString();
    }

    @Override
    public Long computeStringHash() {
        return Digest.RSHash(stringForHashing());
    }

    @Override
    public void applyMutations(List<InstanceMutation<MultiMaskEfsmSkeleton>> mutations) {
        for (InstanceMutation<MultiMaskEfsmSkeleton> m : mutations) {
            m.apply(this);
        }
    }

    @Override
    public MultiMaskEfsmSkeleton copyInstance(MultiMaskEfsmSkeleton other) {
        return new MultiMaskEfsmSkeleton(other);
    }

    @Override
    public Object getFitnessDependentData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setFitnessDependentData(Object fitnessDependentData) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getMaxNumberOfMutations() {
        return Integer.MAX_VALUE;
    }

    public String toGraphvizString(Map<Integer, Integer> stateIdMap) {
        StringBuilder sb = new StringBuilder();

        for (int stateId = 0; stateId < states.length; stateId++) {
            State state = states[stateId];
            for (int eventId = 0; eventId < MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT; eventId++) {
                for (int tgId = 0; tgId < states[stateId].getTransitionGroupCount(eventId); tgId++) {
                    TransitionGroup tg = state.getTransitionGroup(eventId, tgId);
                    for (int tranId = 0; tranId < tg.getTransitionsCount(); tranId++) {
                        if (!tg.isTransitionDefined(tranId)) {
                            continue;
                        }
//                        if (!tg.isTransitionUsed(tranId)) {
//                            continue;
//                        }
                        sb.append(stateIdMap.get(stateId) + " -> " + stateIdMap.get(tg.getNewState(tranId))
                                + " [label = \"REQ [" + tranIdToLabel(tranId, tg.getMeaningfulPredicateIds()) + "] ()\"];\n");
                    }
                }
            }
        }

        return sb.toString();
    }

    public int getUsedTransitionsCount() {
        int result = 0;

        for (int i = 0; i < states.length; i++) {
            result += states[i].getUsedTransitionsCount();
        }

        return result;
    }

    public int getDefinedTransitionsCount() {
        int result = 0;

        for (int i = 0; i < states.length; i++) {
            result += states[i].getDefinedTransitionsCount();
        }

        return result;
    }

    public int getTransitionsCount() {
        int result = 0;

        for (int i = 0; i < states.length; i++) {
            result += states[i].getTransitionsCount();
        }

        return result;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Map<Integer, Integer> stateIdMap = new HashMap<Integer, Integer>();

        int stateCounter = 0;
        sb.append("digraph efsm{\n");
        for (int state = 0; state < MultiMaskEfsmSkeleton.STATE_COUNT; state++) {
            if (stateUsedInTransitions(state)) {
                String label = "" + state;
                sb.append(stateCounter + " [label=\"s_" + label + "\"];\n");
                stateIdMap.put(state, stateCounter);
                stateCounter++;
            }
        }
        sb.append(toGraphvizString(stateIdMap));
        sb.append("}");

        return sb.toString();
    }

    public void clearUsedTransitions() {
        for (State state : states) {
            state.clearUsedTransitions();
        }
    }

    public void removeNullTransitionGroups() {
        for (State state : states) {
            state.removeNullTransitionGroups();
        }
    }
}
