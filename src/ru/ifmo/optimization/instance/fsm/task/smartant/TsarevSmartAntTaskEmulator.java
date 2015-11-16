package ru.ifmo.optimization.instance.fsm.task.smartant;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class TsarevSmartAntTaskEmulator extends TsarevSmartAntTask implements Runnable {
	private FSM fsm;
	public TsarevSmartAntTaskEmulator(AbstractTaskConfig config, String filename) {
		super(config);
		FSM.setEvents(getEvents());
		fsm = new FSM(filename);
	}
	
	@Override
	public FitInstance<FSM> getFitInstance(FSM instance) {
		long start = System.currentTimeMillis();
		
		//FIXME make not null 
		return null;
//		FitInstance<FSM> result = new SimpleFsmMetaData(instance, 
//				getFitness(instance, false, null, true));
//		TIME_FITNESS_COMPUTATION += (System.currentTimeMillis() - start) / 1000.0;
//		return result;
	}

	@Override
	public void run() {
		FitInstance<FSM> instance = getFitInstance(fsm);
		System.out.println("fitness = " + instance.getFitness());
	}
	
	public static void main(String[] args) {
		new Thread(new SmartAntTaskEmulator(new AbstractTaskConfig(args[0]), args[1])).start();
//		new Thread(new TsarevSmartAntTaskEmulator(new AbstractAutomatonTaskConfig("smart-ant-task.properties"), "fsm.fsm")).start();
	}
}
