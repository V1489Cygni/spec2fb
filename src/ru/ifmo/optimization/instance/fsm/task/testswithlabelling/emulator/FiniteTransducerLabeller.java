package ru.ifmo.optimization.instance.fsm.task.testswithlabelling.emulator;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.TestsWithLabelingTask;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class FiniteTransducerLabeller extends TestsWithLabelingTask implements Runnable {

	private static String fsmFileName;
	
	public FiniteTransducerLabeller() {
		super(new AbstractTaskConfig("tests-labelling.properties"));
	}
	
	public static void main(String[] args) {
		fsmFileName = args[0];
		new Thread(new FiniteTransducerLabeller()).start();
	}
	
	@Override
	public void run() {
		FSM.setEvents(getEvents());
		FSM fsm = labelFSM(new FSM(fsmFileName), false);
		fsm.printTransitionDiagram("labelled-solution");
	}
}
