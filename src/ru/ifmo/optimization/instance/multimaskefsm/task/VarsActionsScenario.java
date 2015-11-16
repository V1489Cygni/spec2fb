package ru.ifmo.optimization.instance.multimaskefsm.task;

import java.util.ArrayList;
import java.util.List;

public class VarsActionsScenario {
    private List<ScenarioElement> elements = new ArrayList<ScenarioElement>();

    public VarsActionsScenario() {
    }

    public VarsActionsScenario(List<ScenarioElement> element) {
        this.elements.addAll(element);
    }

    public void add(ScenarioElement e) {
        elements.add(e);
    }

    public ScenarioElement get(int i) {
        return elements.get(i);
    }

    public int size() {
        return elements.size();
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
            outputs[i] = elements.get(i).getActions() + elements.get(i).getOutputEvent();
        }
        return outputs;
    }

    public String getActions(int i) {
        return elements.get(i).getActions();
    }

    public String toG4LTL() {
        return printScenario(0);
    }

    private String printScenario(int i) {
        if (i < elements.size() - 1) {
            return "(" + elements.get(i).inputToG4LTL() + ") -> (" + elements.get(i).outputToG4LTL() + " && X(" + printScenario(i + 1) + ")" + ")";
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
