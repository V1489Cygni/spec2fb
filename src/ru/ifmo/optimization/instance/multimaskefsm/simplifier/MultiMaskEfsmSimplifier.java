package ru.ifmo.optimization.instance.multimaskefsm.simplifier;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.multimaskefsm.*;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.DestinationStateMutation;
import ru.ifmo.optimization.instance.multimaskefsm.task.MultiMaskTask;
import ru.ifmo.optimization.instance.multimaskefsm.task.MultiMaskTaskFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.random.RandomProvider;

public class MultiMaskEfsmSimplifier implements Runnable {
    private MultiMaskEfsmSkeleton efsm;
    private MultiMaskTask task;
    private OutputAction[] actions;

    public MultiMaskEfsmSimplifier(String filename, AbstractTaskConfig config) {
        MultiMaskTaskFactory factory = new MultiMaskTaskFactory(config);
        RandomProvider.initialize(1, 4317);
        RandomProvider.register();
        this.task = (MultiMaskTask) factory.createTask();
//		this.efsm = new MultiMaskEfsm(filename).getSkeleton();
    }

    public MultiMaskEfsmSimplifier(MultiMaskEfsmSkeleton skeleton, MultiMaskTask task) {
//		RandomProvider.initialize(1, 4317);
//		RandomProvider.register();
        this.task = task;
        this.efsm = skeleton;
    }

    public MultiMaskEfsmSimplifier(MultiMaskEfsm efsm, MultiMaskTask task) {
        this.task = task;
        this.efsm = efsm.getSkeleton();
        this.actions = efsm.getActions();
    }

    public static void main(String[] args) {
        new MultiMaskEfsmSimplifier("constructed.gv", new AbstractTaskConfig("new-fbdk.properties")).run();
    }

    private double getFitness(MultiMaskEfsmSkeleton skeleton) {
        return getFitInstance(skeleton).getFitness();
    }

    private FitInstance<MultiMaskEfsmSkeleton> getFitInstance(MultiMaskEfsmSkeleton skeleton) {
        if (actions == null) {
            return task.getFitInstance(skeleton);
        }
        return task.getFitInstance(new MultiMaskEfsm(skeleton, actions));
    }

    public MultiMaskEfsmSkeleton doAll() {
        MultiMaskEfsmSkeleton bestSolution = new MultiMaskEfsmSkeleton(efsm);
        double maxFitness = getFitness(bestSolution);//
        System.out.println("used transitions = " + getFitInstance(bestSolution).getInstance().getUsedTransitionsCount());


        while (true) {
            boolean somethingChanged = false;
            for (int stateId = 0; stateId < MultiMaskEfsmSkeleton.STATE_COUNT; stateId++) {
                for (int eventId = 0; eventId < MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT; eventId++) {
                    for (int tgId = 0; tgId < MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT; tgId++) {
                        TransitionGroup bestTg = bestSolution.getState(stateId).getTransitionGroup(eventId, tgId);
                        if (!bestTg.hasTransitions()) {
                            continue;
                        }
                        for (Integer i : bestTg.getDefinedTransitionIds()) {
                            MultiMaskEfsmSkeleton mutant = new MultiMaskEfsmSkeleton(bestSolution);

                            DestinationStateMutation mutation = new DestinationStateMutation(stateId, eventId, tgId, i, -1);
                            mutation.apply(mutant);
                            double fitness = getFitness(mutant);

                            if (!(fitness < maxFitness) && mutant.getDefinedTransitionsCount() < bestSolution.getDefinedTransitionsCount()) {
                                maxFitness = fitness;
                                bestSolution = getFitInstance(mutant).getInstance();
                                somethingChanged = true;
                            }

                            if (somethingChanged) {
                                System.out.println(bestSolution.getUsedTransitionsCount());
                                System.exit(1);
                            }
                        }
                    }
                }
            }
            if (!somethingChanged) {
                break;
            }
        }

        while (true) {
            boolean somethingChanged = false;
            outer:
            for (int stateId = 0; stateId < MultiMaskEfsmSkeleton.STATE_COUNT; stateId++) {
                for (int eventId = 0; eventId < MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT; eventId++) {
                    for (int tgId = 0; tgId < MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT; tgId++) {
                        TransitionGroup bestTg = bestSolution.getState(stateId).getTransitionGroup(eventId, tgId);
                        if (!bestTg.hasTransitions()) {
                            continue;
                        }
                        if (bestTg.getMeaningfulPredicatesCount() == 1) {
                            continue;
                        }
                        for (Integer predicateId : bestTg.getMeaningfulPredicateIds()) {
                            MultiMaskEfsmSkeleton mutant = new MultiMaskEfsmSkeleton(bestSolution);
                            TransitionGroup tg = mutant.getState(stateId).getTransitionGroup(eventId, tgId);
                            tg.removePredicateForSimplifying(predicateId, true);

                            double fitness = getFitness(mutant);

                            if (!(fitness < maxFitness)) {
                                maxFitness = fitness;
                                bestSolution = getFitInstance(mutant).getInstance();
                                somethingChanged = true;
                                System.out.println("New number of used transitions = " + bestSolution.getUsedTransitionsCount());
                                break outer;
                            }

                            mutant = new MultiMaskEfsmSkeleton(bestSolution);
                            tg = mutant.getState(stateId).getTransitionGroup(eventId, tgId);
                            tg.removePredicateForSimplifying(predicateId, false);

                            fitness = getFitness(mutant);

                            if (fitness >= maxFitness) {
                                maxFitness = fitness;
                                bestSolution = getFitInstance(mutant).getInstance();
                                somethingChanged = true;
                                System.out.println("New number of used transitions = " + bestSolution.getUsedTransitionsCount());
                                break outer;
                            }
                        }
                    }
                }
            }
            if (!somethingChanged) {
                break;
            }
        }
        System.out.println("Number of used transitions after simplification = " + bestSolution.getUsedTransitionsCount());
        return bestSolution;
    }

    public void run() {
        MultiMaskEfsmSkeleton simplified = doAll();
        MultiMaskMetaData result;
        if (actions == null) {
            result = (MultiMaskMetaData) task.getInstanceMetaData(simplified);
        } else {
            result = (MultiMaskMetaData) task.getInstanceMetaData(new MultiMaskEfsm(simplified, actions));
        }
        System.out.println(TLFitness.getFitness(result.getEfsm(), new MultiMaskTask(new AbstractTaskConfig("new-fbdk.properties"))).getKey());
        result.print("fbdk");
    }


}
