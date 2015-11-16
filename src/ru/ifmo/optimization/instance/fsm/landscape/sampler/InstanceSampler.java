package ru.ifmo.optimization.instance.fsm.landscape.sampler;

import java.util.List;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;

public abstract class InstanceSampler {
	private InitialFSMGenerator fsmGenerator = new InitialFSMGenerator();
	private AbstractAutomatonTask task;
	
	public InstanceSampler(AbstractAutomatonTask task) {
		this.task = task;
	}
	
	protected FSM createRandomFSM() {
		return fsmGenerator.createInstance(task);
	}
	
	protected FitInstance<FSM> applyFitness(FSM fsm) {
		return task.getFitInstance(fsm);
	}
	
	public abstract List<FitInstance<FSM>> sample(int sampleSize);
}
