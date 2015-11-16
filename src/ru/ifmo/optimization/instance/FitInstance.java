package ru.ifmo.optimization.instance;

public class FitInstance<Instance extends Hashable> implements Comparable<FitInstance<Instance>> {
	protected Instance instance;
	protected double fitness;
	
	public FitInstance(Instance instance, double fitness) {
		this.instance = instance;
		this.fitness = fitness;
	}
	
	public FitInstance(FitInstance<Instance> other) {
		instance = other.instance;
		fitness = other.fitness;
	}
	
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	public double getFitness() {
		return fitness;
	}
	
	public Instance getInstance() {
		return instance;
	}
	
	@Override
	public int compareTo(FitInstance<Instance> arg0) {
		return Double.compare(fitness, arg0.fitness);
	}
	
	@Override
	public String toString() {
		return fitness + "";
	}
}
