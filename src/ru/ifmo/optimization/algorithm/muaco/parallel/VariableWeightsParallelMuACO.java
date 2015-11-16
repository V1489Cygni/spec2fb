package ru.ifmo.optimization.algorithm.muaco.parallel;

import java.util.ArrayList;

import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.task.factory.VariableWeightsModelCheckingTaskFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.random.RandomProvider;

public class VariableWeightsParallelMuACO extends ParallelMuACO<FSM, FsmMutation> {

	public VariableWeightsParallelMuACO(MuACOConfig<FSM, FsmMutation> config,
			AbstractTaskFactory<FSM> taskFactory) {
		super(config, taskFactory);
		
		VariableWeightsModelCheckingTaskFactory factory = new VariableWeightsModelCheckingTaskFactory(taskFactory.getConfig());
		algorithms = new ArrayList<MuACO<FSM, FsmMutation>>();
		for (int i = 0; i < numberOfThreads; i++) {
			double ltlCost = 1.0 + (RandomProvider.getInstance().nextBoolean() ? 0.5 * RandomProvider.getInstance().nextDouble() : -0.5 * RandomProvider.getInstance().nextDouble()); 
			double testsCost =  2.0 - ltlCost;
			System.out.println(i + ": ltlCost=" + ltlCost + ", testsCost=" + testsCost);
			algorithms.add(new MuACO<FSM, FsmMutation>(config, factory.createTask(ltlCost, testsCost)));
		}
	}
}
