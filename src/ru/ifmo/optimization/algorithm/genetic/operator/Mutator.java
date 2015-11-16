package ru.ifmo.optimization.algorithm.genetic.operator;

public interface Mutator <Individual> {
    public abstract Individual apply(Individual individual);
}
