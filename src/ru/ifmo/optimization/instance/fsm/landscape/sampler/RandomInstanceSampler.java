package ru.ifmo.optimization.instance.fsm.landscape.sampler;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;

public class RandomInstanceSampler extends InstanceSampler {

	public RandomInstanceSampler(AbstractAutomatonTask task) {
		super(task);
	}

	@Override
	public List<FitInstance<FSM>> sample(int sampleSize) {
		List<FitInstance<FSM>> result = new ArrayList<FitInstance<FSM>>();
		for (int i = 0; i < sampleSize; i++) {
			result.add(applyFitness(createRandomFSM()));
		}
		return result;
	}
}
