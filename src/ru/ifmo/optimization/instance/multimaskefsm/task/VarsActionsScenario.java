package ru.ifmo.optimization.instance.multimaskefsm.task;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.instance.multimaskefsm.OutputAction;

public class VarsActionsScenario {
	private int outputCount = 0;
	private int maxCost = 0;
    private List<ScenarioElement> elements = new ArrayList<ScenarioElement>();

    public VarsActionsScenario() {
    }

    public VarsActionsScenario(List<ScenarioElement> elements) {
        this.elements.addAll(elements);
//        for (ScenarioElement e : elements) {
        for (int i = 0; i < elements.size(); i++) {
        	outputCount += elements.get(i).getActions().size();
        	maxCost += (elements.size() - i) * elements.get(i).getActions().size();
        }
    }

    public void add(ScenarioElement e) {
        elements.add(e);
        outputCount += e.getActions().size();
    }

    public ScenarioElement get(int i) {
        return elements.get(i);
    }
    
    public int size() {
        return elements.size();
    }
    
    public int getOutputCount() {
    	return outputCount;
    }
    
    public int getMaxCost() {
    	return maxCost;
    }

    public List<ScenarioElement> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ScenarioElement e : elements) {
            sb.append(e);
        }
        return sb.toString();
    }
    
    public String[] getOutputs() {
        String[] outputs = new String[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
        	outputs[i] = "";
        	for (OutputAction a : elements.get(i).getActions()) {
        		outputs[i] += a.getAlgorithm() + a.getOutputEvent();
        	}
        }
        return outputs;
    }
    
    public String[] getOutputEvents() {
//    	  String[] outputs = new String[outpu];
    	List<String> result = new ArrayList<String>();
          for (int i = 0; i < elements.size(); i++) {
//          	outputs[i] = "";
          	for (OutputAction a : elements.get(i).getActions()) {
//          		outputs[i] += a.getOutputEvent();
          		result.add(a.getOutputEvent());
          	}
          }
          return result.toArray(new String[0]);
    }

    public List<OutputAction> getActions(int i) {
        return elements.get(i).getActions();
    }

    public String toG4LTL() {
        return printScenarioG4LTL(0);
    }

    private String printScenarioG4LTL(int i) {
        if (i < elements.size() - 1) {
            return "(" + elements.get(i).inputToG4LTL() + ") -> (" + elements.get(i).outputToG4LTL() + " && X(" + printScenarioG4LTL(i + 1) + ")" + ")";
        }
        return "(" + elements.get(i).inputToG4LTL() + ") -> (" + elements.get(i).outputToG4LTL() + ")";
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VarsActionsScenario && elements.equals(((VarsActionsScenario) obj).elements);
    }
}
