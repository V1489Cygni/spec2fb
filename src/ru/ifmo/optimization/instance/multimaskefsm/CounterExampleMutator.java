package ru.ifmo.optimization.instance.multimaskefsm;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.DestinationStateMutation;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.MultiMaskEfsmMutation;
import ru.ifmo.optimization.instance.multimaskefsm.task.ScenarioElement;
import ru.ifmo.optimization.instance.multimaskefsm.task.VarsActionsScenario;
import ru.ifmo.random.RandomProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CounterExampleMutator implements Mutator<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> {
    private double probability;

    public CounterExampleMutator(double probability) {
        this.probability = probability;
    }

    @Override
    public MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> apply(MultiMaskEfsmSkeleton individual) {
        if (individual.getCounterExamples().size() == 0) {
            return new MutatedInstanceMetaData<>(new MultiMaskEfsmSkeleton(individual), new MutationCollection<>());
        }
        List<VarsActionsScenario> counterExamples = individual.getCounterExamples();
        VarsActionsScenario scenario = counterExamples.get(0);
        for (VarsActionsScenario s : counterExamples) {
            if (s.size() > scenario.size()) {
                scenario = s;
            }
        }
        List<Step> trace = getTrace(individual, scenario);
        Random random = RandomProvider.getInstance();
        MultiMaskEfsmSkeleton result = new MultiMaskEfsmSkeleton(individual);
        MutationCollection<MultiMaskEfsmMutation> mutations = new MutationCollection<>();
        trace.stream().filter(step -> step.group != -1 && random.nextDouble() < probability).forEach(step -> {
            int sid = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
            mutations.add(new DestinationStateMutation(step.state, step.event, step.group, step.index, sid));
            result.getState(step.state).getTransitionGroup(step.event, step.group).setNewState(step.index, sid);
        });
        return new MutatedInstanceMetaData<>(result, mutations);
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

    private List<Step> getTrace(MultiMaskEfsmSkeleton individual, VarsActionsScenario scenario) {
        List<Step> trace = new ArrayList<>();
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
            trace.add(new Step(state, eid, group, index));
            state = res;
        }
        return trace;
    }

    private static class Step {
        private int state, event, group, index;

        public Step(int state, int event, int group, int index) {
            this.state = state;
            this.event = event;
            this.group = group;
            this.index = index;
        }
    }
}
