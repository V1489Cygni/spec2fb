package ru.ifmo.optimization.instance.fsm.task.testswithlabelling;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.instance.fsm.FSM;

public class RandomFSMGeneratorForTests {
	public static FSM generateRandomFSM (int numberOfStates, List<String> events, List<String> actions, int maxNumberOfActions) {
		FSM.Transition transitions[][] = new FSM.Transition[numberOfStates][events.size()];
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < events.size(); j++) {
				int numberOfActions = Math.max(random.nextInt(maxNumberOfActions), 1);
				String sequence = "";
				for (int k = 0; k < numberOfActions; k++) {
					sequence += actions.get(random.nextInt(actions.size()));
				}
				int nextState = random.nextInt(numberOfStates);
				transitions[i][j] = new FSM.Transition(i, nextState, events.get(j), sequence);
			}
		}
		
		return new FSM(numberOfStates, transitions);
	}
}
