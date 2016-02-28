package ru.ifmo.optimization.instance.multimaskefsm.task;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.instance.multimaskefsm.OutputAction;


public class ScenarioElement {
    private String inputEvent;
    private String variableValues;
    private List<OutputAction> outputActions;

    public ScenarioElement(String inputEvent, String variableValues, List<OutputAction> outputActions) {
        this.inputEvent = inputEvent;
        this.variableValues = variableValues;
        this.outputActions = new ArrayList<OutputAction>();
        this.outputActions.addAll(outputActions);
    }

    public ScenarioElement(String inputEvent, String variableValues, OutputAction outputAction) {
        this.inputEvent = inputEvent;
        this.variableValues = variableValues;
        this.outputActions = new ArrayList<OutputAction>();
        this.outputActions.add(outputAction);
    }
    
    public int getActionsCount() {
    	return outputActions.size();
    }
    
    public String getAlgorithm(int i) {
    	return outputActions.get(i).getAlgorithm();
    }
    
    public String getLastAlgorithm() {
    	return outputActions.get(outputActions.size() - 1).getAlgorithm();
    }
    
    public String getOutputEvent(int i) {
    	return outputActions.get(i).getOutputEvent();
    }
    

    public String getInputEvent() {
        return inputEvent;
    }

    public String getVariableValues() {
        return variableValues;
    }
    
    public OutputAction getAction(int i) {
    	return outputActions.get(i);
    }
    
    public OutputAction getLastAction() {
    	return outputActions.get(outputActions.size() - 1);
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

    public List<OutputAction> getActions() {
    	return outputActions;
    }
    
    public String getStringOutput() {
    	String result = "";
    	for (OutputAction a : outputActions) {
    		result += a.getAlgorithm() + a.getOutputEvent();
    	}
    	return result;
    }
    
    public List<String> getOutput() {
    	List<String> result = new ArrayList<String>();
    	for (OutputAction a : outputActions) {
    		result.add(a.getAlgorithm() + a.getOutputEvent());
    	}
    	return result;
    }

    public void addActions(String actions, String outputEvent) {
        this.outputActions.add(new OutputAction(actions, outputEvent));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(inputEvent);
        sb.append("[");
        sb.append(variableValues);
        sb.append("]; ");
        sb.append(outputActions);
        sb.append(">");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ScenarioElement)) {
            return false;
        }
        ScenarioElement other = (ScenarioElement) obj;
        if (!variableValues.equals(other.variableValues) || outputActions.size() != other.outputActions.size() ||
                !(inputEvent.equals(other.inputEvent))) {
            return false;
        }
        
        for (int i = 0; i < outputActions.size(); i++) {
        	if (!outputActions.get(i).equals(other.outputActions.get(i))) {
        		return false;
        	}
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (inputEvent + variableValues).hashCode() + outputActions.hashCode() * 17;
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
//        for (int i = 0; i < outputActions.getAlgorithm().length(); i++) {
//            if (outputActions.getAlgorithm().charAt(i) == '0') {
//                sb.append("!");
//            }
//            sb.append("o" + i);
//
//            if (i < outputActions.getAlgorithm().length() - 1) {
//                sb.append(" && ");
//            }
//        }
        return sb.toString();
    }
}
