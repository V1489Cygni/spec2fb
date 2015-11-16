package ru.ifmo.optimization.instance.multimaskefsm.task;

import ru.ifmo.optimization.instance.multimaskefsm.*;
import ru.ifmo.optimization.instance.multimaskefsm.simplifier.MultiMaskEfsmSimplifier;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

import java.util.*;

public class ExactEccGenerator {
    public ExactEccGenerator() {
    }

    public static void main(String[] args) {
        new ExactEccGenerator().run();
    }

    private OutputAction getAlgorithmConsistentWithFirstElement(VarsActionsScenario[] scenarios, List<OutputAction> possibleAlgorithms) {
        alg:
        for (OutputAction outputAction : possibleAlgorithms) {
            for (VarsActionsScenario s : scenarios) {
                if (!applyMask(s.get(0).getActions(), outputAction.getAlgorithm()).equals(s.get(1).getActions())) {
                    continue alg;
                }
            }
            return outputAction;
        }
        return null;
    }

    private OutputAction getBestPossibleAlgorithm(ScenarioElement first, ScenarioElement second, Collection<OutputAction> possibleAlgorithms) {
        for (OutputAction outputAction : possibleAlgorithms) {
            if (applyMask(first.getActions(), outputAction.getAlgorithm()).equals(second.getActions())) {
                return outputAction;
            }
        }
        throw new RuntimeException();
    }

    private OutputAction getPossibleAlgorithm(ScenarioElement first, ScenarioElement second) {
        String actions1 = first.getActions();
        String actions2 = second.getActions();
        StringBuilder possibleAlgorithm = new StringBuilder();
        for (int j = 0; j < actions1.length(); j++) {
            if (actions1.charAt(j) == '0' && actions2.charAt(j) == '1') {
                possibleAlgorithm.append('1');
            } else if (actions1.charAt(j) == '1' && actions2.charAt(j) == '0') {
                possibleAlgorithm.append('0');
            } else {
                possibleAlgorithm.append('x');
            }
        }
        return new OutputAction(possibleAlgorithm.toString(), second.getOutputEvent());
    }

    private List<OutputAction> getPossibleAlgorithms(VarsActionsScenario[] scenarios) {
        Set<OutputAction> algorithms = new HashSet<OutputAction>();
        for (VarsActionsScenario s : scenarios) {
            for (int i = 0; i < s.size() - 1; i++) {
                if (s.get(i + 1).getOutputEvent().isEmpty()) {
                    continue;
                }
                OutputAction possibleAlgorithm = getPossibleAlgorithm(s.get(i), s.get(i + 1));
                algorithms.add(possibleAlgorithm);
                System.out.println(possibleAlgorithm.getAlgorithm());
            }
        }

        List<OutputAction> mergeCandidates = new ArrayList<OutputAction>();
        mergeCandidates.addAll(algorithms);

        while (true) {
            boolean somethingChanged = false;
            loop:
            for (int i = 0; i < mergeCandidates.size(); i++) {
                for (int j = 0; j < mergeCandidates.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    OutputAction firstCandidate = mergeCandidates.get(i);
                    OutputAction secondCandidate = mergeCandidates.get(j);
                    OutputAction merged = getMergedAlgorithm(firstCandidate, secondCandidate);
                    if (canMergeAlgorithms(firstCandidate, secondCandidate, scenarios, mergeCandidates)) {
                        mergeCandidates.remove(firstCandidate);
                        mergeCandidates.remove(secondCandidate);
                        mergeCandidates.add(merged);
                        somethingChanged = true;
                        break loop;
                    }
                }
            }
            if (!somethingChanged) {
                break;
            }
        }
        return mergeCandidates;
    }

    private OutputAction getMergedAlgorithm(OutputAction first, OutputAction second) {
        if (!first.getOutputEvent().equals(second.getOutputEvent())) {
            return null;
        }
        String alg1 = first.getAlgorithm();
        String alg2 = second.getAlgorithm();
        StringBuilder mergedSb = new StringBuilder();
        for (int i = 0; i < alg1.length(); i++) {
            if (alg1.charAt(i) == alg2.charAt(i)) {
                mergedSb.append(alg1.charAt(i));
            } else if (alg1.charAt(i) == 'x') {
                mergedSb.append(alg2.charAt(i));
            } else if (alg2.charAt(i) == 'x') {
                mergedSb.append(alg1.charAt(i));
            }
        }

        return new OutputAction(mergedSb.toString(), first.getOutputEvent());
    }

    private boolean canMergeAlgorithms(OutputAction first, OutputAction second, VarsActionsScenario[] scenarios, List<OutputAction> mergeCandidates) {
        if (!first.getOutputEvent().equals(second.getOutputEvent())) {
            return false;
        }
        String alg1 = first.getAlgorithm();
        String alg2 = second.getAlgorithm();
        for (int i = 0; i < alg1.length(); i++) {
            if ((alg1.charAt(i) == '0' && alg2.charAt(i) == '1') || (alg1.charAt(i) == '1' && alg2.charAt(i) == '0')) {
                return false;
            }
        }

        OutputAction merged = getMergedAlgorithm(first, second);
        if (merged == null) {
            return false;
        }
        for (VarsActionsScenario s : scenarios) {
            for (int i = 0; i < s.size() - 1; i++) {
                OutputAction possibleAlgorithm = getBestPossibleAlgorithm(s.get(i), s.get(i + 1), mergeCandidates);
                String currentActions = s.get(i).getActions();
                String mergedResult = applyMask(currentActions, merged.getAlgorithm());

                if (possibleAlgorithm.getAlgorithm().equals(alg1)) {
                    String alg1Result = applyMask(currentActions, alg1);
                    if (!alg1Result.equals(mergedResult)) {
                        return false;
                    }
                } else if (possibleAlgorithm.getAlgorithm().equals(alg2)) {
                    String alg2Result = applyMask(currentActions, alg2);
                    if (!alg2Result.equals(mergedResult)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private String applyMask(String currentActions, String mask) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < currentActions.length(); i++) {
            result.append(mask.charAt(i) == 'x' ? currentActions.charAt(i) : mask.charAt(i));
        }
        return result.toString();
    }

    private VarsActionsScenario[] removeScenarioNondeterminism(VarsActionsScenario[] scenarios) {
        VarsActionsScenario[] result = new VarsActionsScenario[scenarios.length];

        for (int i = 0; i < scenarios.length; i++) {
            List<ScenarioElement> processed = new ArrayList<ScenarioElement>();
            int j = 0;
            ScenarioElement currentElement = scenarios[i].get(j++);
            int numberOfRepeats = 1;
            while (j < scenarios[i].size()) {
                if (scenarios[i].get(j).equals(currentElement)) {
                    j++;
                    numberOfRepeats++;
                } else {
                    if (scenarios[i].get(j).getVariableValues().equals(currentElement.getVariableValues()) &&
                            !scenarios[i].get(j).getActions().equals(currentElement.getActions())) {
                        //remove nondeterminism
                    } else {
                        //add all elements
                        for (int k = 0; k < numberOfRepeats; k++) {
                            processed.add(currentElement);
                        }
                    }
                    currentElement = scenarios[i].get(j);
                    numberOfRepeats = 1;
                    j++;
                }

                if (j == scenarios[i].size()) {
                    for (int k = 0; k < numberOfRepeats; k++) {
                        processed.add(currentElement);
                    }
                }
            }

            result[i] = new VarsActionsScenario(processed);
        }
        return result;
    }

    private void constructSolution(AbstractTaskConfig config) {
        VarsActionsScenario[] scenarios = EccUtils.readScenarios(config.getProperty("scenarios"));
        scenarios = removeScenarioNondeterminism(scenarios);
        EccUtils.readPredicateNames(config.getProperty("predicate-names"));
        MultiMaskEfsmSkeleton.STATE_COUNT = Integer.parseInt(config.getProperty("desired-number-of-states"));
        MultiMaskEfsmSkeleton.MEANINGFUL_PREDICATES_COUNT = Integer.parseInt(config.getProperty("meaningful-predicates-count"));
        MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT = Integer.parseInt(config.getProperty("transition-groups-count"));
        MultiMaskEfsmSkeleton.PREDICATE_COUNT = MultiMaskEfsmSkeleton.PREDICATE_NAMES.size();

        List<OutputAction> possibleAlgorithms = getPossibleAlgorithms(scenarios);
        System.out.println(possibleAlgorithms.size());
        for (OutputAction s : possibleAlgorithms) {
            System.out.println(s);
        }

        List<OutputAction> usedAlgorithms = new ArrayList<OutputAction>();
        Set<Tran>[] transitionsExist = new HashSet[possibleAlgorithms.size()];
        List<Tran>[] transitions = new ArrayList[possibleAlgorithms.size()];
        Map<String, Integer>[] newStateMap = new HashMap[possibleAlgorithms.size()];
        for (int i = 0; i < transitions.length; i++) {
            transitionsExist[i] = new HashSet<Tran>();
            transitions[i] = new ArrayList<Tran>();
            newStateMap[i] = new HashMap<String, Integer>();
        }

        usedAlgorithms.add(getAlgorithmConsistentWithFirstElement(scenarios, possibleAlgorithms));

        for (VarsActionsScenario s : scenarios) {
            int currentState = 0;
            for (int i = 0; i < s.size() - 1; i++) {
                if (s.get(i).getActions().equals(s.get(i + 1).getActions())) {
                    continue;
                }

                OutputAction algorithm = getBestPossibleAlgorithm(s.get(i), s.get(i + 1), possibleAlgorithms);
                int newState = usedAlgorithms.indexOf(algorithm);
                if (newState == -1) {
                    usedAlgorithms.add(algorithm);
                    newState = usedAlgorithms.size() - 1;
                }

                Tran newTran = new Tran(s.get(i + 1).getInputEvent(), s.get(i + 1).getVariableValues(), newState);
                if (!transitionsExist[currentState].contains(newTran)) {
                    if (newStateMap[currentState].containsKey(s.get(i + 1).getVariableValues())) {
                        if (newStateMap[currentState].get(s.get(i + 1).getVariableValues()) != newTran.destination) {
                            throw new RuntimeException("Nondeterministic behavior in scenarios");
                        }
                    } else {
                        newStateMap[currentState].put(newTran.label, newTran.destination);
                    }
                    transitionsExist[currentState].add(newTran);
                    transitions[currentState].add(newTran);
                }
                currentState = newState;
            }
        }

        for (List<Tran> s : transitions) {
            for (Tran t1 : s) {
                for (Tran t2 : s) {
                    if (t1.equals(t2)) {
                        continue;
                    }
                    //check for nondeterminism
                    if (t2.label.equals(t1.label) && t2.destination != t1.destination) {
                        throw new RuntimeException();
                    }
                }
            }
        }

        if (usedAlgorithms.size() != possibleAlgorithms.size()) {
            throw new RuntimeException();
        }

        for (OutputAction s : usedAlgorithms) {
            System.out.println("Used " + s + " algorithms");
        }

        MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT = 50;
        State[] states = new State[transitions.length];
        for (int i = 0; i < states.length; i++) {
            states[i] = new State();
            for (Tran t : transitions[i]) {
                TransitionGroup tg = new TransitionGroup(MultiMaskEfsmSkeleton.PREDICATE_COUNT);
                for (int j = 0; j < MultiMaskEfsmSkeleton.PREDICATE_COUNT; j++) {
                    tg.setMaskElement(j, true);
                }
                tg.setNewState(Integer.valueOf(t.label, 2), t.destination);
                states[i].addTransitionGroup(t.inputEvent, tg);
            }
        }

        MultiMaskEfsmSkeleton.STATE_COUNT = states.length;

        MultiMaskEfsmSkeleton skeleton = new MultiMaskEfsmSkeleton(states);
        MultiMaskEfsm result = new MultiMaskEfsm(skeleton);
        for (int i = 0; i < usedAlgorithms.size(); i++) {
            result.setActions(i, usedAlgorithms.get(i));
        }

        MultiMaskTask task = new MultiMaskTask(config);
        MultiMaskEfsmSkeleton.STATE_COUNT = states.length;

        double fitness = task.getFitness(result);
        System.out.println("Fitness of constructed solution before simplification = " + fitness);
        if (fitness < 1.1) {
            throw new RuntimeException("Fitness of constructed solution is less than 1.1 - an error occurred!");
        }
        System.out.println("Simplifying solution...");
        MultiMaskEfsmSimplifier simplifier = new MultiMaskEfsmSimplifier(result, task);
        simplifier.run();
    }

    public void run() {
        constructSolution(new AbstractTaskConfig("new-fbdk.properties"));
    }

    private class Tran {
        private String inputEvent;
        private String label;
        private int destination;

        public Tran(String inputEvent, String label, int destination) {
            this.inputEvent = inputEvent;
            this.label = label;
            this.destination = destination;
        }

        @Override
        public int hashCode() {
            return (inputEvent + label).hashCode() + destination;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Tran)) {
                return false;
            }
            Tran t = (Tran) obj;
            if (inputEvent.equals(t.inputEvent) && t.label.equals(label) && t.destination == destination) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return inputEvent + "[" + label + ", " + destination + "]";
        }
    }
}
