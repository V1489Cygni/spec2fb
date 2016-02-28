package ru.ifmo.optimization.instance.fsm.task.factory;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.languagelearning.LanguageLearningTask;
import ru.ifmo.optimization.instance.fsm.task.smartant.NoStepsSmartAntTask;
import ru.ifmo.optimization.instance.fsm.task.smartant.PenaltySmartAntTask;
import ru.ifmo.optimization.instance.fsm.task.smartant.SmartAntTask;
import ru.ifmo.optimization.instance.fsm.task.smartant.TsarevSmartAntTask;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.BFSTestsModelCheckingTask;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.TestsModelCheckingTask;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.TestsModelCheckingTaskWithConsistencyGraph;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.TestsWithLabelingTask;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.errors.TestsWithErrorsTask;
import ru.ifmo.optimization.instance.fsm.task.transducer.TestsWithoutLabelingTask;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

/**
 * 
 * @author Daniil Chivilikhin
 *
 */
public class FsmTaskFactory extends AbstractTaskFactory<FSM> {
	private static enum TaskName {
		TESTS,
		NOVELTY_TESTS,
		SMART_ANT,
		PENALTY_SMART_ANT,
		TSAREV_SMART_ANT,
		NO_STEPS_SMART_ANT,
		LANGUAGE,
		NOISY_DFA,
		TRANSDUCER,
		TESTS_WITHOUT_LABELING,
		MODEL_CHECKING,
		MODEL_CHECKING_CONSISTENCY,
		MODEL_CHECKING_BFS,
		TESTS_ERRORS
	}
	
	
	public FsmTaskFactory(AbstractTaskConfig config) {
		super(config);
	}
	
	public AbstractOptimizationTask<FSM> createTask() {
		TaskName taskName = TaskName.valueOf(config.getTaskName());
		AbstractOptimizationTask<FSM> fsmTask = null;
		switch (taskName) {
		case SMART_ANT:
			fsmTask =  new SmartAntTask(config);
			break;
		case PENALTY_SMART_ANT:
			fsmTask = new PenaltySmartAntTask(config);
			break;
		case LANGUAGE:
			fsmTask = new LanguageLearningTask(config);
			break;
		case TESTS_WITHOUT_LABELING:
			fsmTask = new TestsWithoutLabelingTask(config);
			break;
		case TESTS:
			fsmTask = new TestsWithLabelingTask(config);
			break;
		case TSAREV_SMART_ANT:
			fsmTask = new TsarevSmartAntTask(config);
			break;
		case NO_STEPS_SMART_ANT:
			fsmTask = new NoStepsSmartAntTask(config);
			break;
		case MODEL_CHECKING:
			fsmTask = new TestsModelCheckingTask(config);
			break;
		case MODEL_CHECKING_CONSISTENCY:
			fsmTask = new TestsModelCheckingTaskWithConsistencyGraph(config);
			break;
		case MODEL_CHECKING_BFS:
			fsmTask = new BFSTestsModelCheckingTask(config);
			break;
		case TESTS_ERRORS:
			fsmTask = new TestsWithErrorsTask(config);
			break;
		default:
			throw new IllegalStateException();
		}
		
		FSM.setEvents(((AbstractAutomatonTask)fsmTask).getEvents());
		return fsmTask;
	}
}
