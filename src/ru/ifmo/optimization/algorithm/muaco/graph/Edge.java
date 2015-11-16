package ru.ifmo.optimization.algorithm.muaco.graph;

import ru.ifmo.util.Array;

public class Edge { 
	private MutationCollection mutations;
	private Node from;
	private Node to;
	private double pheromone;        
	private double bestPheromone;
	private double heuristicDistance;
	
	public Edge(MutationCollection mutations, Node from, Node to, double heuristicDistance){
		this.mutations = mutations;
		this.from = from;
		this.to = to;
		this.heuristicDistance = heuristicDistance;
		bestPheromone = 0;
	}

	public MutationCollection getMutations() {
		return mutations;
	}
	
	public Node getSource() {
		return from;
	}
	
	public Node getDest() {
		return to;
	}
	
	public double getPheromone() {
		return pheromone;
	}
	
	public void setPheromone(double pheromone) {
		this.pheromone = pheromone;
	}
	
	public void setBestPheromone(double pheromone) {
		bestPheromone = Math.max(bestPheromone, pheromone);
	}
	
	public double getBestPheromone() {
		return bestPheromone;
	}

	public double getHeuristicDistance() {
		return heuristicDistance;
	}

	@Override
	public String toString() {
		return to + "[p = " + pheromone + "; d = " + heuristicDistance + "]";
	}
	
	@Override
	public boolean equals(Object o) {
		Edge other = (Edge)o;
		return mutations.equals(other.mutations) && to.equals(other.to) && from.equals(other.from);
	}
	
	@Override
	public int hashCode() {
		return from.hashCode() + to.hashCode();
	}
} 
