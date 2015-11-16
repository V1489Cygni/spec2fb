package ru.ifmo.optimization.algorithm.genetic.operator.crossover;

import java.util.List;

public interface Crossover <Instance> {
    public abstract List<Instance> apply(List<Instance> parents); 
}
