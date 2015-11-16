package ru.ifmo.optimization.algorithm.muaco.graph.fsm.canonical;

import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.heuristicdist.HeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.instance.CanonicalInstancesCache;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.CanonicalInstanceData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.util.Pair;

public class ConstructionGraphWithActiveCanonicalCache extends FsmConstructionGraph {
	private CanonicalInstancesCache<FSM, CanonicalInstanceData<Node<FSM>>> canonicalInstancesCache;
	
	public ConstructionGraphWithActiveCanonicalCache(
			PheromoneUpdater<FSM, FsmMutation> pheromoneUpdater,
			HeuristicDistance<FSM> heuristicDistance,
			FitInstance<FSM> metaData,
			int maxCanonicalCacheSize) {
		super(pheromoneUpdater, heuristicDistance, metaData);
		canonicalInstancesCache = new CanonicalInstancesCache<FSM, CanonicalInstanceData<Node<FSM>>>(maxCanonicalCacheSize);
	}
	
	@Override
	public Edge addNode(Node<FSM> node,
			MutationCollection<FsmMutation> mutations,
			FitInstance<FSM> metaData,
			int fitnessEvaluationCount) {
		Edge newEdge = node.addChild(mutations, metaData, heuristicDistance, fitnessEvaluationCount);
		pheromoneUpdater.initializePheromone(newEdge);
		nodesMap.put(metaData.getInstance().computeStringHash(), newEdge.getDest());
		edges.add(newEdge);
		
		if (newEdge.getDest().getFitness() > bestFitness) {
			bestFitness = newEdge.getDest().getFitness();
			bestNode = newEdge.getDest();
		}
		
		//FIXME bad code, applicable to FSMs only. Add getCanonicalInstance to Instance interface
		int newId[] = new int[metaData.getInstance().getNumberOfStates()];
		FSM canonicalInstance = metaData.getInstance().getCanonicalFSM(newId);
		if (!canonicalInstancesCache.contains(canonicalInstance)) {
			canonicalInstancesCache.add(new CanonicalInstanceData<Node<FSM>>(newEdge.getDest(), newId), canonicalInstance);
		}
		
		return newEdge;
	}
	
	@Override
	public Node<FSM> getNode(FSM instance) {
		return nodesMap.get(instance.computeStringHash());
	};
	
	@Override
	public Pair<Node<FSM>, MutatedInstanceMetaData<FSM, FsmMutation>> getPathToIsomorphicNode(FSM instance) {
		int newId[] = new int[instance.getNumberOfStates()];
		FSM canonicalInstance = instance.getCanonicalFSM(newId);
		CanonicalInstanceData<Node<FSM>> newResult = canonicalInstancesCache.getFirstNonCanonicalInstance(canonicalInstance);
		if (newResult == null) {
			return null;
		}
		
		FSM oldInstance = getNodeInstance(newResult.getData());
		if (oldInstance.equals(instance)) {
			return null;
		}
		
		MutationCollection<FsmMutation> mutations = instance.getMutations(oldInstance);
		return new Pair(newResult.getData(), new MutatedInstanceMetaData<FSM, FsmMutation>(oldInstance, mutations));
	}
	
	@Override
	public void clear() {
		super.clear();
		canonicalInstancesCache.clear();
	}
}
