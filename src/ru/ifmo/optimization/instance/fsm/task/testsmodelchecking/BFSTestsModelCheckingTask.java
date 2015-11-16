package ru.ifmo.optimization.instance.fsm.task.testsmodelchecking;

import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class BFSTestsModelCheckingTask extends TestsModelCheckingTask {

	public BFSTestsModelCheckingTask(AbstractTaskConfig config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM fsm) {
		InstanceMetaData<FSM> result = super.getInstanceMetaData(fsm);
		
		int[] newId = fsm.getBfsStateMapping();
		
		double bfsFF = 0;
		for (int i = 0; i < newId.length; i++) {
			if (newId[i] != i) {
				bfsFF++;
			}
		}
		bfsFF /= newId.length;
		bfsFF = 1.0 - bfsFF;
		
		FsmMetaData tmp = (FsmMetaData)result;
		
		return new FsmMetaData(result.getInstance(), tmp.getVisitedTransitions(), result.getFitness() + 0.1 * bfsFF); 
	}
	
}
