package ru.ifmo.optimization.instance.multimaskefsm.task;

import ru.ifmo.optimization.instance.multimaskefsm.OutputAction;


public class ScenarioElement {
    private String inputEvent;
    private String variableValues;
    private OutputAction outputAction;

    public ScenarioElement(String inputEvent, String variableValues, OutputAction outputAction) {
        this.inputEvent = inputEvent;
        this.variableValues = variableValues;
        this.outputAction = outputAction;
    }

    public String getInputEvent() {
        return inputEvent;
    }

    public String getVariableValues() {
        return variableValues;
    }

    public String getMeaningfulVariableValues(boolean mask[]) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mask.length; i++) {
            if (mask[i]) {
                sb.append(variableValues.charAt(i));
            }
        }
        return sb.toString();
    }

    public String getActions() {
        return outputAction.getAlgorithm();
    }

    public void setActions(String actions, String outputEvent) {
        this.outputAction = new OutputAction(actions, outputEvent);
    }

    public String getOutputEvent() {
        return outputAction.getOutputEvent();
    }

    public String getChangesMask(ScenarioElement other) {
        return getChangesMask(other.outputAction.getAlgorithm());
    }

    public String getChangesMask(String otherValues) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < outputAction.getAlgorithm().length(); i++) {
            if (outputAction.getAlgorithm().charAt(i) == otherValues.charAt(i)) {
                sb.append("x");
            } else {
                sb.append(outputAction.getAlgorithm().charAt(i));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(inputEvent);
        sb.append("[");
        sb.append(variableValues);
        sb.append("]; ");
        sb.append(outputAction);
        sb.append(">");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ScenarioElement)) {
            return false;
        }
        ScenarioElement other = (ScenarioElement) obj;
        if (!variableValues.equals(other.variableValues) || !outputAction.equals(other.outputAction) ||
                !(inputEvent.equals(other.inputEvent))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (inputEvent + variableValues).hashCode() + outputAction.hashCode() * 17;
    }

    public String inputToG4LTL() {
        StringBuilder sb = new StringBuilder();
        sb.append("!x239 && ");
        for (int i = 0; i < variableValues.length(); i++) {
            if (variableValues.charAt(i) == '0') {
                sb.append("!");
            }
            sb.append("x" + i);

            if (i < variableValues.length() - 1) {
                sb.append(" && ");
            }

        }
        return sb.toString();
    }

    public String outputToG4LTL() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < outputAction.getAlgorithm().length(); i++) {
            if (outputAction.getAlgorithm().charAt(i) == '0') {
                sb.append("!");
            }
            sb.append("o" + i);

            if (i < outputAction.getAlgorithm().length() - 1) {
                sb.append(" && ");
            }
        }
        return sb.toString();
    }
}