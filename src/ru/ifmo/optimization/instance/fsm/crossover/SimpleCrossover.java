package ru.ifmo.optimization.instance.fsm.crossover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.util.Util;

public class SimpleCrossover implements AbstractCrossover<FSM, FsmMutation> {

	@Override
	public List<MutatedInstanceMetaData<FSM, FsmMutation>> apply(FSM first, FSM second) {
		Transition[][] states1 = new Transition[first.getNumberOfStates()][];
		Transition[][] states2 = new Transition[first.getNumberOfStates()][];
		
		for (int i = 0; i < first.getNumberOfStates(); i++) {
			ArrayList<Transition> list = new ArrayList<Transition>();
			for (Transition t : first.transitions[i]) {
				list.add(t);
			}
			for (Transition t : second.transitions[i]) {
				list.add(t);
			}
			Collections.shuffle(list, ThreadLocalRandom.current());

			states1[i] = new Transition[first.transitions[i].length];
			states2[i] = new Transition[second.transitions[i].length];
			
			for (int j = 0; j < first.transitions[i].length; j++) {
				states1[i][j] = new Transition(list.get(j));
				states1[i][j].setStartState(i);
				states1[i][j].setEvent(FSM.EVENTS.get(j));
			}
			for (int j = 0; j < second.transitions[i].length; j++) {
				states2[i][j] = new Transition(list.get(first.transitions[i].length + j));
				states2[i][j].setStartState(i);
				states2[i][j].setEvent(FSM.EVENTS.get(j));
			}
		}
		List<MutatedInstanceMetaData<FSM, FsmMutation>> offspring = new ArrayList<MutatedInstanceMetaData<FSM, FsmMutation>>();
		offspring.add(Util.makeCompliantFSM(new MutatedInstanceMetaData<FSM, FsmMutation>(new FSM(first.getNumberOfStates(), states1), new MutationCollection<FsmMutation>())));
		offspring.add(Util.makeCompliantFSM(new MutatedInstanceMetaData<FSM, FsmMutation>(new FSM(first.getNumberOfStates(), states2), new MutationCollection<FsmMutation>())));
		offspring.get(0).setMutations(first.getMutations(offspring.get(0).getInstance()));
		offspring.get(1).setMutations(first.getMutations(offspring.get(1).getInstance()));
		return offspring;
	}

}
