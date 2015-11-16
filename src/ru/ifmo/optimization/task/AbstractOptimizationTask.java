package ru.ifmo.optimization.task;

import java.util.Comparator;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.Hashable;
import ru.ifmo.optimization.instance.InstanceMetaData;

public abstract class AbstractOptimizationTask<Instance extends Hashable> {
	protected double desiredFitness;
	protected int numberOfFitnessEvaluations = 0;
	protected int numberOfAttemptedFitnessEvaluations = 0;
	public Comparator<Double> comparator;
	
	public abstract FitInstance<Instance> getFitInstance(Instance instance);
	
	public abstract InstanceMetaData<Instance> getInstanceMetaData(Instance instance);
	
	public abstract double correctFitness(double fitness, Instance cachedInstance, Instance trueInstance);
	
	public abstract Comparator<Double> getComparator();
	
	public void setDesiredFitness(double desiredFitness) {
		this.desiredFitness = desiredFitness;
	}
	
	public double getDesiredFitness() {
		return desiredFitness;
	}
	
	public int getNumberOfFitnessEvaluations() {
		return numberOfFitnessEvaluations;
	}
	
	public int getNumberOfAttemptedFitnessEvaluations() {
		return numberOfAttemptedFitnessEvaluations;
	}
	
	public void increaseNumberOfAttemptedFitnessEvaluations(int value) {
		numberOfAttemptedFitnessEvaluations += value;
	}
	
	public abstract int getNeighborhoodSize();
	
	public void reset() {
	}
}
