package ru.ifmo.optimization.instance.multimaskefsm;

import java.io.Serializable;

public class OutputAction implements Serializable {
    private String algorithm;
    private String outputEvent;

    public OutputAction(String algorithm, String outputEvent) {
        this.algorithm = algorithm;
        this.outputEvent = outputEvent;
    }

    public OutputAction(OutputAction other) {
        this.algorithm = other.algorithm;
        this.outputEvent = other.outputEvent;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getOutputEvent() {
        return outputEvent;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OutputAction)) {
            return false;
        }

        OutputAction other = (OutputAction) obj;
        return algorithm.equals(other.algorithm) && outputEvent.equals(other.outputEvent);
    }

    @Override
    public int hashCode() {
        return (algorithm + outputEvent).hashCode();
    }

    @Override
    public String toString() {
        return outputEvent + "[" + algorithm + "]";
    }
}