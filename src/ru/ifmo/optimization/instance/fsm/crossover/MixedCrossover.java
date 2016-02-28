package ru.ifmo.optimization.instance.fsm.crossover;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;

public class MixedCrossover implements AbstractCrossover<FSM, FsmMutation> {

	private SimpleCrossover simpleCrossover = new SimpleCrossover();
	private TestBasedCrossover testBasedCrossover = new TestBasedCrossover();
	
	@Override
	public List<MutatedInstanceMetaData<FSM, FsmMutation>> apply(FSM first, FSM second) {
		if (ThreadLocalRandom.current().nextBoolean()) {
			return simpleCrossover.apply(first, second);
		}
		return testBasedCrossover.apply(first, second);
	}
}
