package ru.ifmo.optimization.algorithm.muaco.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.heuristicdist.HeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.CanonicalInstanceData;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.mutation.MutationInstanceBuilder;
import ru.ifmo.util.Pair;
import ru.ifmo.util.Util;

public class SearchGraph<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> {
	private Node<Instance> root;
	private Instance rootInstance;
	//TODO deal with hardcoded constant
	protected Map<Long, Node<Instance>> nodesMap = new ConcurrentHashMap<Long, Node<Instance>>(3000000);
	
	protected List<Edge> edges = new ArrayList<Edge>();
	protected PheromoneUpdater<Instance, MutationType> pheromoneUpdater;
	protected HeuristicDistance<Instance> heuristicDistance;
	protected Node<Instance> bestNode;
	protected double bestFitness;
	private MutationInstanceBuilder<Instance> mutationInstanceBuilder = new MutationInstanceBuilder<Instance>();
	
	public SearchGraph(PheromoneUpdater<Instance, MutationType> pheromoneUpdater, 
			                 HeuristicDistance<Instance> heuristicDistance, FitInstance<Instance> metaData) {
		this.pheromoneUpdater = pheromoneUpdater;
		this.heuristicDistance = heuristicDistance;
		root = new Node<Instance>(null, null, metaData, 1);
		rootInstance = metaData.getInstance();
		nodesMap.put(rootInstance.computeStringHash(), root);
		bestNode = root;
		bestFitness = metaData.getFitness();
	}
	
	public void init(FitInstance<Instance> metaData) {
		root = new Node<Instance>(null, null, metaData, 1);
		rootInstance = metaData.getInstance();
		nodesMap.put(rootInstance.computeStringHash(), root);
		bestNode = root;
		bestFitness = metaData.getFitness();
	}
	
	public Node<Instance> getBestNode() {
		return bestNode;
	}
	
	public Node<Instance> getRoot() {
		return root;
	}
	
	public Instance getNodeInstance(Node<Instance> node) {
		if (node == null) {
			return null;
		}

		List<MutationCollection<MutationType>> mutationCollections = new ArrayList<MutationCollection<MutationType>>();
		Node<Instance> currentNode = node;
		while (true) {
			Node<Instance> parent = currentNode.getParent();
			if (parent == null) {
				break;
			}
			MutationCollection<MutationType> mutationCollection = currentNode.getMutations();
			mutationCollections.add(mutationCollection);
			currentNode = parent;
		}
		Collections.reverse(mutationCollections);
		List<InstanceMutation<Instance>> mutations = new ArrayList<InstanceMutation<Instance>>();
		for (MutationCollection<MutationType> mc : mutationCollections) {
			mutations.addAll(mc.getMutations());
		}
		
		Instance result = mutationInstanceBuilder.buildInstance(rootInstance, mutations);
		result.setFitnessDependentData(node.getFitnessDependentData());
		return result;
	}
	
	public int getNumberOfNodes() {
		return nodesMap.values().size();
	}
	
	public Node<Instance> getNode(Instance instance) {
		return nodesMap.get(instance.computeStringHash());
	}
	
	public Node<Instance> getRandomNode() {
		List<Node<Instance>> nodes = new ArrayList<Node<Instance>>();
		if (nodes.size() == 0) {
			return root;
		}
		nodes.addAll(nodesMap.values());
		return nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
	}
	
	public void clear() {
		root.clear();
		nodesMap.clear();
		edges.clear();
		bestNode = null;
	}
	
	public Edge addNode(Node<Instance> node, MutationCollection<MutationType> mutations, 
			FitInstance<Instance> metaData, int fitnessEvaluationCount) {
		Edge newEdge = node.addChild(mutations, metaData, heuristicDistance, fitnessEvaluationCount);
		pheromoneUpdater.initializePheromone(newEdge);
		nodesMap.put(metaData.getInstance().computeStringHash(), newEdge.getDest());
		edges.add(newEdge);
		
		if (newEdge.getDest().getFitness() > bestFitness) {
			bestFitness = newEdge.getDest().getFitness();
			bestNode = newEdge.getDest();
		}
		return newEdge;
	}
	
	public Edge addEdge(Node<Instance> node, MutationCollection<MutationType> mutations, Node<Instance> other) {
		Edge edge = node.addChild(mutations, other, heuristicDistance);
		pheromoneUpdater.initializePheromone(edge);
		edges.add(edge);
		
		if (edge.getDest().getFitness() > bestFitness) {
			bestFitness = edge.getDest().getFitness();
			bestNode = edge.getDest();
		}
		return edge;
	}
	
	public List<Node<Instance>> getNodes() {
		List<Node<Instance>> result = new ArrayList<Node<Instance>>();
		result.addAll(nodesMap.values());
		return result;
	}
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	public void updatePheromone(List<Path> iterationPaths, Path iterationBestPath, 
			Path bestPath, double evaporationRate, boolean useRisingPaths) {
		pheromoneUpdater.updatePheromone(this, iterationPaths, iterationBestPath, bestPath, evaporationRate, useRisingPaths);
	}
	
	public Map<Integer, Integer> getNodeVisitStats() {
		Map<Integer, Integer> result = new TreeMap<Integer, Integer>();
		
		for (Node<Instance> node : nodesMap.values()) {
			int numberOfVisits = node.getNumberOfVisits();
			if (result.containsKey(numberOfVisits)) {
				result.put(numberOfVisits, result.get(numberOfVisits) + 1);
			} else {
				result.put(numberOfVisits, 1);
			}
		}
		
		return result;
	}
	
	public double meanNodeDegree() {
		if (nodesMap.size() == 0) {
			return 0;
		}
		
		double result = 0;
		for (Entry<Long, Node<Instance>> e : nodesMap.entrySet()) {
			result += e.getValue().getChildren().size();
		}
		return result / nodesMap.size();
	}
	
	public double maxNodeDegree() {
		if (nodesMap.size() == 0) {
			return 0;
		}
		
		double result = 0;
		for (Entry<Long, Node<Instance>> e : nodesMap.entrySet()) {
			if (e.getValue().getChildren().size() > result) {
				result = e.getValue().getChildren().size();
			}
		}
		return result;
	}
	
	public Pair<Node<Instance>, MutatedInstanceMetaData<Instance, MutationType>> getPathToIsomorphicNode(Instance instance) {
		return null;
	}
	
	public CanonicalInstanceData<Node<Instance>> getFirstIsomorphicNode(Instance instance, int newId2[]) {
		return null;
	}

    public void printToGraphViz(String filename) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new File(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        out.println("digraph g{");
        for (Node<Instance> node : nodesMap.values()) {
            out.println(node.getHash() + "[label=\"" + node.getFitnessEvaluationCount() + "\"];");
        }

        for (Edge edge : edges) {
            out.println(edge.getSource().getHash() + " -> " + edge.getDest().getHash() + ";");
        }
        out.print("}");

        out.close();
    }

    public void printToGraphViz(String filename, int iteration, AntStats stats) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new File(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        out.println("digraph g{");
        out.println("node[shape=circle];");
        for (Node<Instance> node : nodesMap.values()) {
            String color = Util.valueToColor(node.getFitness(), 0, 91);
            String style = ",style=filled";
            
            if (node.getFitnessEvaluationCount() <= iteration) {
            	out.printf("%s[label=\"%.2f\"%s, color=%s];\n", node.getHash(), node.getFitness(), style, color);
            } else {
            	out.printf("%s[label=\"%.2f\", style=invisible];\n", node.getHash(), node.getFitness());
            }
        }

        for (Edge edge : edges) {
            if (edge.getDest().getFitnessEvaluationCount() <= iteration && edge.getSource().getFitnessEvaluationCount() <= iteration) {
                out.println(edge.getSource().getHash() + " -> " + edge.getDest().getHash() + ";");
            } else {
                out.println(edge.getSource().getHash() + " -> " + edge.getDest().getHash() + " [color=white];");
            }
        }
        out.print("}");

        out.close();
    }
    
    public void printToGraphViz(String filename, int startIteration, int endIteration, AntStats stats) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new File(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        out.println("digraph g{");
        out.println("node[shape=circle];");
        for (Node<Instance> node : nodesMap.values()) {
            String color = Util.valueToColor(node.getFitness(), 0, 91);
            String style = ",style=filled";
            
            if (node.getFitnessEvaluationCount() >= startIteration && node.getFitnessEvaluationCount() <= endIteration) {
            	out.printf("%s[label=\"%.2f\"%s, color=%s];\n", node.getHash(), node.getFitness(), style, color);
            } else if (node.getFitnessEvaluationCount() >= startIteration) {
            	out.printf("%s[label=\"%.2f\", style=invisible];\n", node.getHash(), node.getFitness());
            }
        }

        for (Edge edge : edges) {
            if (edge.getDest().getFitnessEvaluationCount() >= startIteration && edge.getDest().getFitnessEvaluationCount() <= endIteration 
            		&& edge.getSource().getFitnessEvaluationCount() >= startIteration && edge.getSource().getFitnessEvaluationCount() <= endIteration) {
                out.println(edge.getSource().getHash() + " -> " + edge.getDest().getHash() + ";");
            } else if (edge.getDest().getFitnessEvaluationCount() >= startIteration && edge.getSource().getFitnessEvaluationCount() >= startIteration){
                out.println(edge.getSource().getHash() + " -> " + edge.getDest().getHash() + " [color=white];");
            }
        }
        out.print("}");

        out.close();
    }
}
