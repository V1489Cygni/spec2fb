package ru.ifmo.optimization.instance.multimaskefsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransitionGroup {
    private boolean mask[];
    private int[] newState;
    private boolean[] transitionUsed;

    public TransitionGroup(int meaningfulPredicatesCount) {
        mask = new boolean[MultiMaskEfsmSkeleton.PREDICATE_COUNT];
        Arrays.fill(mask, false);
        newState = new int[(int) Math.pow(2, meaningfulPredicatesCount)];
        Arrays.fill(newState, -1);
        transitionUsed = new boolean[newState.length];
        Arrays.fill(transitionUsed, false);
    }

    public TransitionGroup(TransitionGroup other) {
        if (other != null) {
            this.mask = Arrays.copyOf(other.mask, other.mask.length);
            this.newState = Arrays.copyOf(other.newState, other.newState.length);
        } else {
            mask = new boolean[MultiMaskEfsmSkeleton.PREDICATE_COUNT];
            Arrays.fill(mask, false);
            newState = new int[(int) Math.pow(2, MultiMaskEfsmSkeleton.MEANINGFUL_PREDICATES_COUNT)];
            Arrays.fill(newState, -1);
        }
        transitionUsed = new boolean[newState.length];
        Arrays.fill(transitionUsed, false);
    }

    public void markTransitionsUnused() {
        Arrays.fill(transitionUsed, false);
    }

    public void setPredicateMeaningful(int i, boolean isMeaningful) {
        mask[i] = isMeaningful;
    }

    public void setTransitionUsed(int transitionIndex) {
        transitionUsed[transitionIndex] = true;
    }

    public boolean isTransitionUsed(int transitionIndex) {
        return transitionUsed[transitionIndex];
    }

    public List<Integer> getMeaningfulPredicateIds() {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < mask.length; i++) {
            if (mask[i]) {
                result.add(i);
            }
        }
        return result;
    }

    public List<Integer> getUnmeaningfulPredicateIds() {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < mask.length; i++) {
            if (!mask[i]) {
                result.add(i);
            }
        }
        return result;
    }

    public int getNewState(int transitionIndex) {
        return newState[transitionIndex];
    }

    public void setMaskElement(int i, boolean value) {
        mask[i] = value;
    }

    public void setNewState(int transitionIndex, int newState) {
        this.newState[transitionIndex] = newState;
    }

    public int getTransitionsCount() {
        return newState.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (boolean v : mask) {
            sb.append(v ? 1 : 0);
        }
        for (int v : newState) {
            sb.append(v);
        }
        return sb.toString();
    }

    public boolean hasUndefinedTransitions() {
        for (int v : newState) {
            if (v == -1) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTransitions() {
        for (int v : newState) {
            if (v != -1) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getUndefinedTransitionIds() {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < newState.length; i++) {
            if (newState[i] == -1) {
                result.add(i);
            }
        }
        return result;
    }

    public List<Integer> getDefinedTransitionIds() {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < newState.length; i++) {
            if (newState[i] != -1) {
                result.add(i);
            }
        }
        return result;
    }

    public int getUsedTransitionsCount() {
        int result = 0;
        for (boolean b : transitionUsed) {
            if (b) {
                result++;
            }
        }
        return result;
    }

    public int getDefinedTransitionsCount() {
        return getDefinedTransitionIds().size();
    }

    public int getMeaningfulPredicatesCount() {
        return getMeaningfulPredicateIds().size();
    }

    public int getUnmeaningfulPredicatesCount() {
        return getUnmeaningfulPredicateIds().size();
    }

    public void removePredicate(int predicateId) {
        int relativePredicateId = getMeaningfulPredicateIds().indexOf(predicateId);

        int[] changedNewState = new int[(int) (newState.length / 2)];
        int newStateCounter = 0;
        boolean[] visited = new boolean[newState.length];
        Arrays.fill(visited, false);

        for (int i = 0; i < newState.length; i++) {
            if (visited[i]) {
                continue;
            }
            visited[i] = true;
            int pairedTransitionId = i + (int) Math.pow(2, getMeaningfulPredicatesCount() - relativePredicateId - 1);
            if (pairedTransitionId >= newState.length) {
                continue;
            }
//			if (RandomProvider.getInstance().nextBoolean()) {
            if (newStateCounter % 2 == 0) {
                changedNewState[newStateCounter++] = newState[i];
            } else {
                changedNewState[newStateCounter++] = newState[pairedTransitionId];
            }
            visited[pairedTransitionId] = true;
        }

        mask[predicateId] = false;
        newState = Arrays.copyOf(changedNewState, changedNewState.length);
        transitionUsed = new boolean[changedNewState.length];
        Arrays.fill(transitionUsed, false);
    }


    public void removePredicateForSimplifying(int predicateId, boolean doLeaveFirst) {
        int relativePredicateId = getMeaningfulPredicateIds().indexOf(predicateId);

        int[] changedNewState = new int[(int) (newState.length / 2)];
        int newStateCounter = 0;
        boolean[] visited = new boolean[newState.length];
        Arrays.fill(visited, false);

        for (int i = 0; i < newState.length; i++) {
            if (visited[i]) {
                continue;
            }
            visited[i] = true;
            int pairedTransitionId = i + (int) Math.pow(2, getMeaningfulPredicatesCount() - relativePredicateId - 1);
            if (pairedTransitionId >= newState.length) {
                continue;
            }

            if (doLeaveFirst) {
                changedNewState[newStateCounter++] = newState[i];
            } else {
                changedNewState[newStateCounter++] = newState[pairedTransitionId];
            }
            visited[pairedTransitionId] = true;
        }

        mask[predicateId] = false;
        newState = Arrays.copyOf(changedNewState, changedNewState.length);
        transitionUsed = new boolean[changedNewState.length];
        Arrays.fill(transitionUsed, false);
    }

    public void addPredicate(int predicate) {
        int[] changedNewState = new int[2 * newState.length];
        int newStateCounter = 0;
        for (int i = 0; i < newState.length; i++) {
            changedNewState[newStateCounter++] = newState[i];
            changedNewState[newStateCounter++] = newState[i];
        }

        mask[predicate] = true;
        newState = Arrays.copyOf(changedNewState, changedNewState.length);
        transitionUsed = new boolean[changedNewState.length];
        Arrays.fill(transitionUsed, false);
    }

    public void clearUsedTransitions() {
        Arrays.fill(transitionUsed, false);
    }
}