package ru.ifmo.optimization.instance.fsm.crossover;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.util.Util;

public class TestBasedCrossover implements AbstractCrossover<FSM, FsmMutation> {

	@Override
	public List<MutatedInstanceMetaData<FSM, FsmMutation>> apply(FSM first, FSM second) {
		Transition[][] states1 = new Transition[first.getNumberOfStates()][FSM.EVENTS.size()];
		Transition[][] states2 = new Transition[second.getNumberOfStates()][FSM.EVENTS.size()];

		for (int i = 0; i < first.getNumberOfStates(); i++) {

			List<Transition> toStates1 = new ArrayList<Transition>();
			List<Transition> toStates2 = new ArrayList<Transition>();
			{
				for (Transition t : first.transitions[i]) {
					if (first.isTransitionUsed(t)) {
						toStates1.add(new Transition(t));
					}
				}
				for (Transition t : second.transitions[i]) {
					if (second.isTransitionUsed(t)) {
						toStates1.add(new Transition(t));
					}
				}
				for (Transition t : first.transitions[i]) {
					if (!first.isTransitionUsed(t)) {
						toStates1.add(new Transition(t));
					}
				}
			}

			{
				for (Transition t : second.transitions[i]) {
					if (second.isTransitionUsed(t)) {
						toStates2.add(new Transition(t));
					}
				}
				for (Transition t : first.transitions[i]) {
					if (first.isTransitionUsed(t)) {
						toStates2.add(new Transition(t));
					}
				}
				for (Transition t : second.transitions[i]) {
					if (!second.isTransitionUsed(t)) {
						toStates2.add(new Transition(t));
					}
				}
			}

//			Collections.shuffle(toStates1, RandomProvider.getInstance());
//			Collections.shuffle(toStates2, RandomProvider.getInstance());
			
			toStates1 = Util.makeCompliantTransitions(toStates1);
			toStates2 = Util.makeCompliantTransitions(toStates2);
			
			
			for (Transition t : toStates1) {
				states1[i][FSM.EVENTS.indexOf(t.getEvent())] = t;
			}
			
			for (Transition t : toStates2) {
				states2[i][FSM.EVENTS.indexOf(t.getEvent())] = t;
			}
			
			for (int j = 0; j < states1[i].length; j++) {
				if (states1[i][j] == null) {
					states1[i][j] = new Transition(i, -1, FSM.EVENTS.get(j), "1");
				}
				
				if (states2[i][j] == null) {
					states2[i][j] = new Transition(i, -1, FSM.EVENTS.get(j), "1");
				}
			}
		}

		List<MutatedInstanceMetaData<FSM, FsmMutation>> offspring = new ArrayList<MutatedInstanceMetaData<FSM, FsmMutation>>();
		offspring.add(new MutatedInstanceMetaData<FSM, FsmMutation>(new FSM(first.getNumberOfStates(), states1), new MutationCollection<FsmMutation>()));
		offspring.add(new MutatedInstanceMetaData<FSM, FsmMutation>(new FSM(first.getNumberOfStates(), states2), new MutationCollection<FsmMutation>()));
		offspring.get(0).setMutations(first.getMutations(offspring.get(0).getInstance()));
		offspring.get(1).setMutations(first.getMutations(offspring.get(1).getInstance()));
		return offspring;
	}

}
