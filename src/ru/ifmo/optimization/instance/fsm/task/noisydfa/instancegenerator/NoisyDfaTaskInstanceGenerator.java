package ru.ifmo.optimization.instance.fsm.task.noisydfa.instancegenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.tests.generator.TestsGenerator;
import ru.ifmo.optimization.instance.fsm.task.tests.generator.config.TestGeneratorConfig;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.RandomFSMGeneratorForTests;
import ru.ifmo.random.RandomProvider;

public class NoisyDfaTaskInstanceGenerator extends TestsGenerator {
	
	public class RandomTestLengthProvider extends TestLengthProvider {
		private int maxLength;
		public RandomTestLengthProvider(int maxLength) {
			this.maxLength = maxLength;
		}
		@Override
		public int getTestLength() {
			return RandomProvider.getInstance().nextInt(maxLength);
		};
	}
	
	public class TestExample extends AutomatonTest {
		private boolean accept;
		
		public TestExample(AutomatonTest test, boolean accept) {
			super(test);
			this.accept = accept;
		}
		
		public boolean isAccept() {
			return accept;
		}
	}
	
	private void applyNoise(Collection<AutomatonTest> tests) {
		for (AutomatonTest test : tests) {
		}
	}
	
	@Override
	public void run() {
		TestGeneratorConfig config = new TestGeneratorConfig();
		List<String> events = new ArrayList<String>();
		events.add("0");
		events.add("1");
		List<String> actions = new ArrayList<String>();
		actions.add("n");
		FSM fsm = RandomFSMGeneratorForTests.generateRandomFSM(
				config.getNumberOfStates(), events, 
				actions, 1);
		
		for (int i = 0; i < fsm.getNumberOfStates(); i++) {
			fsm.setStateTerminal(i, RandomProvider.getInstance().nextBoolean());
		}
		
		Set<Transition> visitedTransitions = new HashSet<Transition>();
		for (int i = 0; i < fsm.getNumberOfStates(); i++) {
			for (int j = 0; j < fsm.getNumberOfEvents(); j++) {
				FSM.Transition t = fsm.getTransition(i, j);
				Transition tr = new Transition(i, t.getEndState(), fsm.getEvents().get(j), t.getAction());
				visitedTransitions.add(tr);
			}
		}
		
		FsmMetaData md = new FsmMetaData(fsm, visitedTransitions, 100);
		md.printToGraphViz(".");
		md.printTransitionDiagram(".");
		
		TestLengthProvider testLengthProvider = new RandomTestLengthProvider(config.getLengthOfTestInTestSet());
		
		printTests(generateTests(fsm, events,  testLengthProvider, config.getTrainingSetSize()), "training-set");
		printTests(generateTests(fsm, events,  testLengthProvider, config.getTestSetSize()), "test-set");
	}
	
	public static void main(String[] args) {
		new Thread(new NoisyDfaTaskInstanceGenerator()).start();
	}
}
