package ru.ifmo.optimization.algorithm.muaco.pathselector.fsm.canonical;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.pathselector.HeuristicAntPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pathselector.config.PathSelectorConfig;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class FsmHeuristicAntPathSelector extends HeuristicAntPathSelector<FSM, FsmMutation> {

	public FsmHeuristicAntPathSelector(AbstractOptimizationTask<FSM> task,
			List<Mutator<FSM, FsmMutation>> mutators, PathSelectorConfig config,
			AntStats antStats) {
		super(task, mutators, config, antStats);
	}
}
