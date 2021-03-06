package ru.ifmo.optimization.instance.multimaskefsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class State implements Serializable {
	private int numberOfOutputActions;
    private TransitionGroup[][] transitionGroups;
    private int[] fixedActionId;

    public State() {
    	numberOfOutputActions = 1;//MultiMaskEfsmSkeleton.MAX_OUTPUT_ACTION_COUNT;
        transitionGroups = new TransitionGroup[MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT][MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT];
        fixedActionId = new int[numberOfOutputActions];
        Arrays.fill(fixedActionId, -1);
    }

    public State(State other) {
    	numberOfOutputActions = other.numberOfOutputActions;
        transitionGroups = new TransitionGroup[other.transitionGroups.length][other.transitionGroups[0].length];
        for (int i = 0; i < transitionGroups.length; i++) {
            transitionGroups[i] = new TransitionGroup[other.transitionGroups[i].length];
            for (int j = 0; j < transitionGroups[i].length; j++) {
                transitionGroups[i][j] = new TransitionGroup(other.transitionGroups[i][j]);
            }
        }
        fixedActionId = Arrays.copyOf(other.fixedActionId, other.fixedActionId.length); 
    }
    
    public void setFixedActionId(int actionNumber, int actionId) {
    	if (actionNumber < numberOfOutputActions) {
    		fixedActionId[actionNumber] = actionId;
    	}
    }
    
    public int getFixedActionId(int actionNumber) {
    	if (actionNumber >= numberOfOutputActions) {
    		return -1;
    	}
    	return fixedActionId[actionNumber];
    }
    
    public int getNumberOfOutputActions() {
    	return numberOfOutputActions;
    }
    
    public void setNumberOfOutputActions(int numberOfOutputActions) {
    	this.numberOfOutputActions = numberOfOutputActions;
    	int[] tmp = new int[numberOfOutputActions];
    	for (int i = 0; i < Math.min(tmp.length, fixedActionId.length); i++) {
    		tmp[i] = fixedActionId[i];
    	}
    	if (tmp.length > fixedActionId.length) {
    		for (int i = Math.min(tmp.length, fixedActionId.length); i < tmp.length; i++) {
    			tmp[i] = -1;
    		}
    	}
    	fixedActionId = Arrays.copyOf(tmp, tmp.length);
    }

    public void addTransitionGroup(String inputEvent, TransitionGroup tg) {
        int eventIndex = MultiMaskEfsmSkeleton.INPUT_EVENTS.get(inputEvent);
        if (transitionGroups[eventIndex] == null) {
            transitionGroups[eventIndex] = new TransitionGroup[MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT];
        }
        for (int i = 0; i < transitionGroups[eventIndex].length; i++) {
            if (transitionGroups[eventIndex][i] == null) {
                transitionGroups[eventIndex][i] = tg;
                return;
            }
        }
    }


    public void markTransitionsUnused() {
        for (int i = 0; i < transitionGroups.length; i++) {
            for (int j = 0; j < transitionGroups[i].length; j++) {
                transitionGroups[i][j].markTransitionsUnused();
            }
        }
    }

    public int getNewState(String inputEvent, String variableValues) {
        int eventIndex = MultiMaskEfsmSkeleton.INPUT_EVENTS.get(inputEvent);
        if (transitionGroups[eventIndex] == null) {
            return -1;
        }
        for (TransitionGroup g : transitionGroups[eventIndex]) {
            if (g == null) {
                continue;
            }
            if (!g.hasTransitions()) {
                continue;
            }
            List<Integer> meaningfulPredicateIds = g.getMeaningfulPredicateIds();
            StringBuilder meaningfulPredicateValues = new StringBuilder();
            for (Integer i : meaningfulPredicateIds) {
                meaningfulPredicateValues.append(variableValues.charAt(i));
            }
            int transitionIndex = Integer.parseInt(meaningfulPredicateValues.toString(), 2);
            int newState = g.getNewState(transitionIndex);
            if (newState == -1) {
                continue;
            }

            g.setTransitionUsed(transitionIndex);
            return newState;
        }
        return -1;
    }

    public TransitionGroup getNewStateTransitionGroup(String inputEvent, String variableValues) {
        int eventIndex = MultiMaskEfsmSkeleton.INPUT_EVENTS.get(inputEvent);
        if (transitionGroups[eventIndex] == null) {
            return null;
        }
        for (TransitionGroup g : transitionGroups[eventIndex]) {
            if (!g.hasTransitions()) {
                continue;
            }
            List<Integer> meaningfulPredicateIds = g.getMeaningfulPredicateIds();
            String meaningfulPredicateValues = "";
            for (Integer i : meaningfulPredicateIds) {
                meaningfulPredicateValues += variableValues.charAt(i);
            }
            int transitionIndex = Integer.parseInt(meaningfulPredicateValues, 2);
            int newState = g.getNewState(transitionIndex);
            if (newState != -1) {
                return g;
            }
        }
        return null;
    }

    public TransitionGroup getTransitionGroup(int eventIndex, int i) {
        return transitionGroups[eventIndex][i];
    }

    public int getTransitionGroupCount(int eventIndex) {
        return transitionGroups[eventIndex].length;
    }

    public Collection<Integer> getMeaningfulPredicateIds(String inputEvent) {
        Set<Integer> result = new HashSet<Integer>();
        int eventIndex = MultiMaskEfsmSkeleton.INPUT_EVENTS.get(inputEvent);
        if (transitionGroups[eventIndex] == null) {
            return result;
        }
        for (TransitionGroup tg : transitionGroups[eventIndex]) {
            result.addAll(tg.getMeaningfulPredicateIds());
        }
        return result;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(numberOfOutputActions);
        for (int i : fixedActionId) {
        	sb.append(i);
        }
        for (TransitionGroup[] eventTransitionGroups : transitionGroups) {
            for (TransitionGroup tg : eventTransitionGroups) {
                sb.append(tg);
            }
        }
        return sb.toString();
    }

    public int getUsedTransitionsCount() {
        int result = 0;
        for (TransitionGroup[] eventTransitionGroups : transitionGroups) {
            for (TransitionGroup tg : eventTransitionGroups) {
                if (tg == null) {
                    continue;
                }

                result += tg.getUsedTransitionsCount();
            }
        }
        return result;
    }

    public int getDefinedTransitionsCount() {
        int result = 0;
        for (TransitionGroup[] eventTransitionGroups : transitionGroups) {
            for (TransitionGroup tg : eventTransitionGroups) {
                result += tg.getDefinedTransitionsCount();
            }
        }
        return result;
    }

    public int getTransitionsCount() {
        int result = 0;
        for (TransitionGroup[] eventTransitionGroups : transitionGroups) {
            for (TransitionGroup tg : eventTransitionGroups) {
                result += tg.getTransitionsCount();
            }
        }
        return result;
    }

    public void clearUsedTransitions() {
        for (TransitionGroup[] eventTransitionGroups : transitionGroups) {
            for (TransitionGroup tg : eventTransitionGroups) {
                tg.clearUsedTransitions();
            }
        }
    }

    public void removeNullTransitionGroups() {
        for (int i = 0; i < transitionGroups.length; i++) {
            List<TransitionGroup> tgs = new ArrayList<TransitionGroup>();
            for (TransitionGroup tg : transitionGroups[i]) {
                if (tg != null) {
                    tgs.add(tg);
                }
            }

            transitionGroups[i] = tgs.toArray(new TransitionGroup[0]);
        }
    }
}