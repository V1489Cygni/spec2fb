package ru.ifmo.optimization.instance.multimaskefsm.mutator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.TransitionGroup;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.DestinationStateMutation;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.MultiMaskEfsmMutation;
import ru.ifmo.optimization.instance.multimaskefsm.task.ScenarioElement;
import ru.ifmo.optimization.instance.multimaskefsm.task.VarsActionsScenario;

public class CounterExampleMutator implements Mutator<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> {
    private final int lambda;
    private double probability;

    public CounterExampleMutator(int lambda, double probability) {
        this.lambda = lambda;
        this.probability = probability;
    }

    @Override
    public MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> apply(MultiMaskEfsmSkeleton individual) {
        Set<Step> transitions = new HashSet<>();
        transitions.addAll(getTransitions(individual));
        if (transitions.isEmpty()) {
            return new MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation>(
            		new MultiMaskEfsmSkeleton(individual), new MutationCollection<MultiMaskEfsmMutation>());
        }
        
        for (VarsActionsScenario s : individual.getCounterExamples()) {
        	getTrace(individual, s, transitions);
        }
        List<Step> uniqueTransitions = new ArrayList<>();
        uniqueTransitions.addAll(transitions);
        Collections.sort(uniqueTransitions, new Comparator<Step>() {
			@Override
			public int compare(Step o1, Step o2) {
				return Double.compare(o1.weight, o2.weight);
			}
        });
        
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int size = uniqueTransitions.size();
        double weight[] = new double[size];
        weight[0] = uniqueTransitions.get(0).weight;
        for (int i = 1; i < size; i++) {
        	weight[i] = weight[i - 1] + uniqueTransitions.get(i).weight;
        }
        
        double p = weight[size - 1] * random.nextDouble();
        int j = 0;
        while (p > weight[j]) {
        	j++;
        }
        
        Step s = uniqueTransitions.get(j);
//        
        MultiMaskEfsmSkeleton ind = new MultiMaskEfsmSkeleton(individual);
        int x = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
        ind.getState(s.state).getTransitionGroup(s.event, s.group).setNewState(s.index, x);
        return new MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation>(
        		ind, new MutationCollection<MultiMaskEfsmMutation>(
        				new DestinationStateMutation(s.state, s.event, s.group, s.index, x)));
    }

    @Override
    public MultiMaskEfsmSkeleton applySimple(MultiMaskEfsmSkeleton individual) {
        return apply(individual).getInstance();
    }

    @Override
    public double probability() {
        return probability;
    }

    @Override
    public void setProbability(double probability) {
    	this.probability = probability;
    }

    private List<Step> getTransitions(MultiMaskEfsmSkeleton individual) {
        List<Step> transitions = new ArrayList<>();
        for (int state = 0; state < MultiMaskEfsmSkeleton.STATE_COUNT; state++) {
            for (int event = 0; event < MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT; event++) {
                for (int group = 0; group < individual.getState(state).getTransitionGroupCount(event); group++) {
                    for (int index = 0; index < individual.getState(state).getTransitionGroup(event, group).getTransitionsCount(); index++) {
                        if (individual.getState(state).getTransitionGroup(event, group).getNewState(index) != -1) {
                            transitions.add(new Step(state, event, group, index, 1));
                        }
                    }
                }
            }
        }
        return transitions;
    }

    private void getTrace(MultiMaskEfsmSkeleton individual, VarsActionsScenario scenario, Set<Step> transitions) {
        int state = individual.getInitialState();
        for (int i = 0; i < scenario.size(); i++) {
            ScenarioElement element = scenario.get(i);
            int eid = MultiMaskEfsmSkeleton.INPUT_EVENTS.get(element.getInputEvent());
            String variableValues = element.getVariableValues();
            int res = state, group = -1, index = 0;
            for (int j = 0; j < individual.getState(state).getTransitionGroupCount(eid); j++) {
                TransitionGroup g = individual.getState(state).getTransitionGroup(eid, j);
                if (g == null) {
                    continue;
                }
                if (!g.hasTransitions()) {
                    continue;
                }
                List<Integer> meaningfulPredicateIds = g.getMeaningfulPredicateIds();
                StringBuilder meaningfulPredicateValues = new StringBuilder();
                for (Integer k : meaningfulPredicateIds) {
                    meaningfulPredicateValues.append(variableValues.charAt(k));
                }
                int transitionIndex = Integer.parseInt(meaningfulPredicateValues.toString(), 2);
                int newState = g.getNewState(transitionIndex);
                if (newState == -1) {
                    continue;
                }
                g.setTransitionUsed(transitionIndex);
                res = newState;
                group = j;
                index = transitionIndex;
                break;
            }
            if (group != -1) {
                Step s = new Step(state, eid, group, index, 1 + lambda);
                if (transitions.contains(s)) {
                	transitions.remove(s);
                	transitions.add(s);
                }
                state = res;
            }
        }
    }

    private static class Step {
        public int state, event, group, index;
        public double weight;

        public Step(int state, int event, int group, int index, double weight) {
            this.state = state;
            this.event = event;
            this.group = group;
            this.index = index;
            this.weight = weight;
        }
        
        public Step(Step other) {
        	state = other.state;
        	event = other.event;
        	group = other.group;
        	index = other.index;
        	weight = other.weight;
        }
        
        @Override
        public int hashCode() {
            return state << 26 + event << 20 + group << 14 + index;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Step && state == ((Step) obj).state && event == ((Step) obj).event
                    && group == ((Step) obj).group && index == ((Step) obj).index;
        }
    }

    private static class MyMap {
        private static final int size = 997;
        private Node[] nodes = new Node[size];

        public void put(Step s, int value) {
            int h = (0x7F_FF_FF_FF & s.hashCode()) % size;
            if (nodes[h] == null) {
                nodes[h] = new Node(s, value);
            } else {
                Node n = nodes[h];
                while (n.next != null) {
                    n = n.next;
                }
                n.next = new Node(s, value);
            }
        }

        public int get(Step s) {
            Node n = nodes[(0x7F_FF_FF_FF & s.hashCode()) % size];
            while (!s.equals(n.key)) {
                n = n.next;
            }
            return n.value;
        }

        public void add(Step s, int value) {
            Node n = nodes[(0x7F_FF_FF_FF & s.hashCode()) % size];
            while (!s.equals(n.key)) {
                n = n.next;
            }
            n.value += value;
        }

        public boolean containsKey(Step s) {
            Node n = nodes[(0x7F_FF_FF_FF & s.hashCode()) % size];
            while (n != null) {
                if (n.key.equals(s)) {
                    return true;
                }
                n = n.next;
            }
            return false;
        }

        public void clear() {
            for (int i = 0; i < size; i++) {
                nodes[i] = null;
            }
        }

        private static class Node {
            private Step key;
            private int value;
            private Node next;

            public Node(Step key, int value) {
                this.key = key;
                this.value = value;
            }
        }
    }
}
