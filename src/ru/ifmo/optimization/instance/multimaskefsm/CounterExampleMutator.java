package ru.ifmo.optimization.instance.multimaskefsm;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.DestinationStateMutation;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.MultiMaskEfsmMutation;
import ru.ifmo.optimization.instance.multimaskefsm.task.ScenarioElement;
import ru.ifmo.optimization.instance.multimaskefsm.task.VarsActionsScenario;
import ru.ifmo.random.RandomProvider;

import java.util.*;

public class CounterExampleMutator implements Mutator<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> {
    private static final int LAMBDA = 1;
    private double probability;

    public CounterExampleMutator(double probability) {
        this.probability = probability;
    }

    @Override
    public MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> apply(MultiMaskEfsmSkeleton individual) {
        List<Step> transitions = getTransitions(individual);
        if (transitions.isEmpty()) {
            return new MutatedInstanceMetaData<>(new MultiMaskEfsmSkeleton(individual), new MutationCollection<>());
        }
        Map<Step, Integer> num = new HashMap<>();
        for (Step s : transitions) {
            num.put(s, 1);
        }
        for (VarsActionsScenario s : individual.getCounterExamples()) {
            getTrace(individual, s, num);
        }
        double sum = 0;
        for (Step s : transitions) {
            sum += num.get(s);
        }
        int i = 0;
        double cur = num.get(transitions.get(0)) / sum;
        Random random = RandomProvider.getInstance();
        while (random.nextDouble() > cur) {
            i++;
            cur += num.get(transitions.get(i)) / sum;
        }
        Step s = transitions.get(i);
        MultiMaskEfsmSkeleton ind = new MultiMaskEfsmSkeleton(individual);
        int x = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
        ind.getState(s.state).getTransitionGroup(s.event, s.group).setNewState(s.index, x);
        return new MutatedInstanceMetaData<>(ind, new MutationCollection<>(new DestinationStateMutation(s.state, s.event, s.group, s.index, x)));
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
                            transitions.add(new Step(state, event, group, index));
                        }
                    }
                }
            }
        }
        return transitions;
    }

    private void getTrace(MultiMaskEfsmSkeleton individual, VarsActionsScenario scenario, Map<Step, Integer> num) {
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
                Step s = new Step(state, eid, group, index);
                if (num.containsKey(s)) {
                    num.put(s, num.get(s) + LAMBDA);
                } else {
                    System.err.println("Warning: Unexpected step in trace.");
                }
                state = res;
            }
        }
    }

    private static class Step {
        private int state, event, group, index;

        public Step(int state, int event, int group, int index) {
            this.state = state;
            this.event = event;
            this.group = group;
            this.index = index;
        }

        @Override
        public int hashCode() {
            return state << 24 + event << 16 + group << 8 + index;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Step && state == ((Step) obj).state && event == ((Step) obj).event
                    && group == ((Step) obj).group && index == ((Step) obj).index;
        }
    }
}
