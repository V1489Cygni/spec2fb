package ru.ifmo.optimization.algorithm.muaco.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class Path implements Comparable<Path> {
	private List<Edge> edges = new ArrayList<Edge>();
	private Collection<Node<?>> nodes = new TreeSet<Node<?>>();
	private double bestFitness = Double.MIN_VALUE;
	private int lastBestIndex = -1;
	
	public Path() {
	}
	
	public Path(List<Edge> edges) {
		bestFitness = Double.MIN_VALUE;
		this.edges.addAll(edges);
		for (int i = 0; i < edges.size(); i++) {
			if (edges.get(i).getDest().getFitness() > bestFitness) {
				lastBestIndex = i;
				bestFitness = edges.get(i).getDest().getFitness();
			}
		}
	}
	
	public Path(Path other) {
		this.bestFitness = other.getBestFitness();
		edges.clear();
		edges.addAll(other.edges);
		nodes.addAll(other.nodes);
		lastBestIndex = other.lastBestIndex;
	}
	
	public void add(Edge edge) {
		edges.add(edge);
		nodes.add(edge.getSource());
		nodes.add(edge.getDest());
		if (edge.getDest().getFitness() > bestFitness) {
			lastBestIndex = edges.size() - 1;
			bestFitness = edge.getDest().getFitness();
		}
	}
	
	public int getLength() {
		return edges.size();
	}
	
	public Node getBestNode() {
		if (edges.size() == 0) {
			return null;
		}
		if (lastBestIndex == -1) {
			return edges.get(0).getSource();
		}
		return edges.get(lastBestIndex).getDest();
	}
	
	public void updateBestPheromone() {
		for (Edge e : getAllEdgesUpToBest()) {
    		e.setBestPheromone(bestFitness);
    	}
	}
	
	public void clear() {
		edges.clear();
		bestFitness = Double.MIN_VALUE;
		lastBestIndex = -1;
	}
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	public Collection<Edge> getAllEdgesUpToBest() {
		Collection<Edge> result = new ArrayList<Edge>();
		for (int i = 0; i < lastBestIndex; i++) {
			result.add(edges.get(i));
		}
		return result;
	}
	
	public Path getRisingPath(SearchGraph graph) {
		if (edges.size() == 0) {
			return this;
		}
		List<Edge> result = new ArrayList<Edge>();
		double localBestFitness = edges.get(0).getSource().getFitness();
		Node lastRisingNode = edges.get(0).getSource();
		MutationCollection mutations = new MutationCollection();
		
		for (Edge edge : getAllEdgesUpToBest()) {
			mutations.addAll(edge.getMutations());
			if (edge.getDest().getFitness() > localBestFitness) {
				result.add(graph.addEdge(lastRisingNode, mutations, edge.getDest()));
				lastRisingNode = edge.getDest();
				localBestFitness = edge.getDest().getFitness();
				mutations = new MutationCollection();
			}
		}
		return new Path(result);
	}
	
	public double getBestFitness() {
		return bestFitness;
	}
	
	public double getCurrentFitness() {
		if (edges.isEmpty()) {
			return Double.MIN_VALUE;
		}
		return edges.get(edges.size() - 1).getDest().getFitness();
	}
	
	public boolean contains(Edge edge) {
		return edges.contains(edge);
	}
	
	public boolean contains(Node node) {
		return nodes.contains(node);
	}
	
	@Override
	public int compareTo(Path other) {
		return Double.compare(getBestFitness(), other.getBestFitness());
	}
}
