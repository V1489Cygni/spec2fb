package ru.ifmo.optimization.instance.multimaskefsm.task;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class MultiMaskTaskFactory extends AbstractTaskFactory<MultiMaskEfsmSkeleton> {

	public MultiMaskTaskFactory(AbstractTaskConfig config) {
		super(config);
		MultiMaskEfsmSkeleton.MEANINGFUL_PREDICATES_COUNT = Integer.parseInt(config.getProperty("meaningful-predicates-count"));
		MultiMaskEfsmSkeleton.PREDICATE_COUNT = -1;
		MultiMaskEfsmSkeleton.STATE_COUNT = Integer.parseInt(config.getProperty("desired-number-of-states"));
		MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT = Integer.parseInt(config.getProperty("transition-groups-count"));
	}

	@Override
	public AbstractOptimizationTask<MultiMaskEfsmSkeleton> createTask() {
		return new MultiMaskTask(config);
	}

}
