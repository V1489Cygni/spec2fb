package ru.ifmo.optimization.algorithm.genetic.operator.crossover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.random.RandomProvider;

public class OnePointFsmCrossover implements Crossover<FSM> {
	
	@Override
	public List<FSM> apply(List<FSM> parents) {
		int numberOfStates = parents.get(0).getNumberOfStates();
		int numberOfEvents = parents.get(0).getNumberOfEvents();
		List<String> events = parents.get(0).getEvents();
		
		int point = RandomProvider.getInstance().nextInt(parents.get(0).getNumberOfStates());
		FSM.Transition[][] tr0 = new FSM.Transition[numberOfStates][numberOfEvents]; 
		FSM.Transition[][] tr1 = new FSM.Transition[numberOfStates][numberOfEvents];
		
		for (int state = 0; state < numberOfStates; state++) {
			if (state < point) {
				tr0[state] = Arrays.copyOf(parents.get(0).transitions[state], parents.get(0).transitions[state].length);
				tr1[state] = Arrays.copyOf(parents.get(1).transitions[state], parents.get(1).transitions[state].length);
			} else {
				tr0[state] = Arrays.copyOf(parents.get(1).transitions[state], parents.get(1).transitions[state].length);
				tr1[state] = Arrays.copyOf(parents.get(0).transitions[state], parents.get(0).transitions[state].length);
			}
		}
		
		List<FSM> offspring = new ArrayList<FSM>();
		offspring.add(new FSM(numberOfStates, tr0));
		offspring.add(new FSM(numberOfStates, tr1));
		return offspring;
	}
}
