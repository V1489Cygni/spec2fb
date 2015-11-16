package test.clock;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.factory.FsmTaskFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class EditDistanceCounter implements Runnable {
	public static void main(String[] args) {
		new Thread(new EditDistanceCounter()).start();
	}

	@Override
	public void run() {
//		DefiniteFSM fsm = new DefiniteFSM("smart-ant-solution");

		AbstractTaskConfig config = new AbstractTaskConfig("smart-ant-task.properties");
		FsmTaskFactory factory = new FsmTaskFactory(config);
		
		//FIXME
		AbstractAutomatonTask task = null;//factory.createTask();
//		double bestFitness = task.getFitness(fsm, 0).getFitness();
//		System.out.println(bestFitness);
		
		double min = Integer.MAX_VALUE;
		double max = Integer.MIN_VALUE;
		double mean = 0;
		for (int i = 0; i < 10000; i++) {
			//FIXME
			FSM randomFSM1 = null;//InitialFSMGenerator.generateRandomFSM(7, task.getEvents(), task.getActions(), task.getConstraints());
			FSM randomFSM2 = null;//InitialFSMGenerator.generateRandomFSM(7, task.getEvents(), task.getActions(), task.getConstraints());
//			double fitness = task.getFitness(randomFSM, 0).getFitness();
			double distance = 0;
			min = Math.min(min, distance);
			max = Math.max(max, distance);
			mean += distance;
		}
		
		mean /= 10000;
		System.out.println("min = " + min + "; mean = " + mean + "; max = " + max);
	}
}

