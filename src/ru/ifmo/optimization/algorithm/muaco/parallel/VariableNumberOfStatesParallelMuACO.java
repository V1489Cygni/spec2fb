package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.util.ArrayList;

import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class VariableNumberOfStatesParallelMuACO extends ParallelMuACO<FSM, FsmMutation> {

	public VariableNumberOfStatesParallelMuACO(
			MuACOConfig<FSM, FsmMutation> config,
			AbstractTaskFactory<FSM> taskFactory) {
		super(config, taskFactory);
		
		int minNumberOfStates = Integer.parseInt(config.getProperty("min-number-of-states"));
		int maxNumberOfStates = Integer.parseInt(config.getProperty("max-number-of-states"));
		
		int threadsPerValue = numberOfThreads / (maxNumberOfStates - minNumberOfStates + 1);
		
		algorithms = new ArrayList<MuACO<FSM, FsmMutation>>();
		
		for (int nstates = minNumberOfStates; nstates <= maxNumberOfStates; nstates++) {
			for (int j = 0; j < threadsPerValue; j++) {
				AbstractOptimizationTask<FSM> task = taskFactory.createTask();
				((AbstractAutomatonTask)task).setDesiredNumberOfStates(nstates);
				System.out.println(j + ": nstates=" + nstates);
				algorithms.add(new MuACO<FSM, FsmMutation>(config, task));
			}
		}
		
		while (algorithms.size() < numberOfThreads) {
			AbstractOptimizationTask<FSM> task = taskFactory.createTask();
			((AbstractAutomatonTask)task).setDesiredNumberOfStates(maxNumberOfStates);
			algorithms.add(new MuACO<FSM, FsmMutation>(config, task));
		}
	}
}
