package ru.ifmo.optimization.instance.fsm.landscape.sampler;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.random.RandomProvider;

public class MetropolisHastingsInstaceSampler extends InstanceSampler {
	public MetropolisHastingsInstaceSampler(AbstractAutomatonTask task) {
		super(task);
	}
	
	private double alpha(FitInstance<FSM> lastInstance, FitInstance<FSM> newInstance) {
		return Math.min(1.0, newInstance.getFitness() / lastInstance.getFitness());
	}

	@Override
	public List<FitInstance<FSM>> sample(int sampleSize) {
		List<FitInstance<FSM>> samples = new ArrayList<FitInstance<FSM>>(sampleSize);
		samples.add(applyFitness(createRandomFSM()));
	
		while (samples.size() < sampleSize) {
			FitInstance<FSM> sample = applyFitness(createRandomFSM());
			
			double u = RandomProvider.getInstance().nextDouble();
			if (u <= alpha(samples.get(samples.size() - 1), sample)) {
				samples.add(sample);
			}
		}
		return samples;
	}

}
