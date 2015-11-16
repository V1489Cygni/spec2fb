package ru.ifmo.optimization.algorithm.muaco.graph.fsm.canonical;

import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.heuristicdist.HeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;

public class FsmConstructionGraph extends SearchGraph<FSM, FsmMutation> {

	public FsmConstructionGraph(PheromoneUpdater<FSM, FsmMutation> pheromoneUpdater,
			HeuristicDistance<FSM> heuristicDistance, FitInstance<FSM> metaData) {
		super(pheromoneUpdater, heuristicDistance, metaData);
	}
}
