package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public class SearchGraphBundle<Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> {

	private List<SearchGraph<Instance, MutationType>> graphs;
	
	public SearchGraphBundle(List<SearchGraph<Instance, MutationType>> graphs) {
		this.graphs = graphs;
	}
	
	public Node<Instance> getNode(Instance instance) {
		for (SearchGraph<Instance, MutationType> graph : graphs) {
			Node<Instance> node = graph.getNode(instance);
			if (node != null) {
				return node;
			}
		}
		return null;
	}
}
