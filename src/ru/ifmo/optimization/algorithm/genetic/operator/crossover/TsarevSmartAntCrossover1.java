package ru.ifmo.optimization.algorithm.genetic.operator.crossover;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.ifmo.optimization.instance.fsm.FSM;

public class TsarevSmartAntCrossover1 implements Crossover<FSM> {

	private Random random = new Random();

	@Override
	public List<FSM> apply(List<FSM> parents) {
		int numberOfStates = parents.get(0).getNumberOfStates();
		int numberOfEvents = parents.get(0).getNumberOfEvents();
		List<String> events = parents.get(0).getEvents();

		FSM.Transition[][] tr0 = new FSM.Transition[numberOfStates][numberOfEvents];
		FSM.Transition[][] tr1 = new FSM.Transition[numberOfStates][numberOfEvents];

		for (int state = 0; state < numberOfStates; state++) {
			int crossoverVersion = random.nextInt(3);
			switch (crossoverVersion) {
			case 0:
				tr0[state][0] = new FSM.Transition(parents.get(0).getTransition(state, 0));
				tr0[state][1] = new FSM.Transition(parents.get(1).getTransition(state, 1));
				
				tr1[state][0] = new FSM.Transition(parents.get(1).getTransition(state, 0));
				tr1[state][1] = new FSM.Transition(parents.get(0).getTransition(state, 1));
				break;
			case 1:
				tr0[state][0] = new FSM.Transition(parents.get(1).getTransition(state, 0));
				tr0[state][1] = new FSM.Transition(parents.get(0).getTransition(state, 1));
				
				tr1[state][0] = new FSM.Transition(parents.get(0).getTransition(state, 0));
				tr1[state][1] = new FSM.Transition(parents.get(1).getTransition(state, 1));
				break;
			case 2:
				tr0[state][0] = new FSM.Transition(parents.get(0).getTransition(state, 0));
				tr0[state][1] = new FSM.Transition(parents.get(0).getTransition(state, 1));
				
				tr1[state][0] = new FSM.Transition(parents.get(1).getTransition(state, 0));
				tr1[state][1] = new FSM.Transition(parents.get(1).getTransition(state, 1));
				break;
			case 3:
				tr0[state][0] = new FSM.Transition(parents.get(1).getTransition(state, 0));
				tr0[state][1] = new FSM.Transition(parents.get(1).getTransition(state, 1));
				
				tr1[state][0] = new FSM.Transition(parents.get(0).getTransition(state, 0));
				tr1[state][1] = new FSM.Transition(parents.get(0).getTransition(state, 1));
				break;
			}
		}
		List<FSM> offspring = new ArrayList<FSM>();
		offspring.add(new FSM(numberOfStates, tr0));
		offspring.add(new FSM(numberOfStates, tr1));
		return offspring;
	}
}
