package ru.ifmo.optimization.instance.multimaskefsm.mutator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.TransitionGroup;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.DestinationStateMutation;
import ru.ifmo.optimization.instance.multimaskefsm.mutation.MultiMaskEfsmMutation;
import ru.ifmo.optimization.instance.multimaskefsm.task.ScenarioElement;
import ru.ifmo.optimization.instance.multimaskefsm.task.VarsActionsScenario;

public class OldCounterExampleMutator implements Mutator<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> {

	private final int lambda;

	public OldCounterExampleMutator(int lambda) {
		this.lambda = lambda;
	}


	@Override
	public MutatedInstanceMetaData<MultiMaskEfsmSkeleton, MultiMaskEfsmMutation> apply(MultiMaskEfsmSkeleton individual) {
		List<Step> transitions = getTransitions(individual);
		if (transitions.isEmpty()) {
			return new MutatedInstanceMetaData<>(new MultiMaskEfsmSkeleton(individual), new MutationCollection<>());
		}
		MyMap num = new MyMap();
		for (Step s : transitions) {
			num.put(s, 1);
		}
		for (VarsActionsScenario s : individual.getCounterExamples()) {
			getTrace(individual, s, num);
		}
		double sum = 0;
		for (Step s : transitions) {
			sum += num.get(s);
		}
		int i = 0;
		double cur = num.get(transitions.get(0)) / sum;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		while (random.nextDouble() > cur) {
			i++;
			cur += num.get(transitions.get(i)) / sum;
		}
		Step s = transitions.get(i);
		MultiMaskEfsmSkeleton ind = new MultiMaskEfsmSkeleton(individual);
		int x = random.nextInt(MultiMaskEfsmSkeleton.STATE_COUNT);
		ind.getState(s.state).getTransitionGroup(s.event, s.group).setNewState(s.index, x);
		return new MutatedInstanceMetaData<>(ind, new MutationCollection<>(new DestinationStateMutation(s.state, s.event, s.group, s.index, x)));
	}
	
	@Override
	public MultiMaskEfsmSkeleton applySimple(MultiMaskEfsmSkeleton individual) {
		return apply(individual).getInstance();
	}

	@Override
	public double probability() {
		return 1;
	}

	@Override
	public void setProbability(double probability) {
	}



	private List<Step> getTransitions(MultiMaskEfsmSkeleton individual) {
		List<Step> transitions = new ArrayList<>();
		for (int state = 0; state < MultiMaskEfsmSkeleton.STATE_COUNT; state++) {
			for (int event = 0; event < MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT; event++) {
				for (int group = 0; group < individual.getState(state).getTransitionGroupCount(event); group++) {
					for (int index = 0; index < individual.getState(state).getTransitionGroup(event, group).getTransitionsCount(); index++) {
						if (individual.getState(state).getTransitionGroup(event, group).getNewState(index) != -1) {
							transitions.add(new Step(state, event, group, index));
						}
					}
				}
			}
		}
		return transitions;
	}


	private void getTrace(MultiMaskEfsmSkeleton individual, VarsActionsScenario scenario, MyMap num) {
		int state = individual.getInitialState();
		for (int i = 0; i < scenario.size(); i++) {
			ScenarioElement element = scenario.get(i);
			int eid = MultiMaskEfsmSkeleton.INPUT_EVENTS.get(element.getInputEvent());
			String variableValues = element.getVariableValues();
			int res = state, group = -1, index = 0;
			for (int j = 0; j < individual.getState(state).getTransitionGroupCount(eid); j++) {
				TransitionGroup g = individual.getState(state).getTransitionGroup(eid, j);
				if (g == null) {
					continue;
				}
				if (!g.hasTransitions()) {
					continue;
				}
				List<Integer> meaningfulPredicateIds = g.getMeaningfulPredicateIds();
				StringBuilder meaningfulPredicateValues = new StringBuilder();
				for (Integer k : meaningfulPredicateIds) {
					meaningfulPredicateValues.append(variableValues.charAt(k));
				}
				int transitionIndex = Integer.parseInt(meaningfulPredicateValues.toString(), 2);
				int newState = g.getNewState(transitionIndex);
				if (newState == -1) {
					continue;
				}
				g.setTransitionUsed(transitionIndex);
				res = newState;
				group = j;
				index = transitionIndex;
				break;
			}
			if (group != -1) {
				Step s = new Step(state, eid, group, index);
				num.add(s, lambda);
				state = res;
			}
		}
	}

	private static class Step {
		public int state, event, group, index;

		public Step(int state, int event, int group, int index) {
			this.state = state;
			this.event = event;
			this.group = group;
			this.index = index;
		}

		@Override
		public int hashCode() {
			return state << 26 + event << 20 + group << 14 + index;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Step && state == ((Step) obj).state && event == ((Step) obj).event
					&& group == ((Step) obj).group && index == ((Step) obj).index;
		}
	}


	private static class MyMap {
		private static final int size = 997;
		private Node[] nodes = new Node[size];

		public void put(Step s, int value) {
			int h = (0x7F_FF_FF_FF & s.hashCode()) % size;
			if (nodes[h] == null) {
				nodes[h] = new Node(s, value);
			} else {
				Node n = nodes[h];
				while (n.next != null) {
					n = n.next;
				}
				n.next = new Node(s, value);
			}
		}

		public int get(Step s) {
			Node n = nodes[(0x7F_FF_FF_FF & s.hashCode()) % size];
			while (!s.equals(n.key)) {
				n = n.next;
			}
			return n.value;
		}

		public void add(Step s, int value) {
			Node n = nodes[(0x7F_FF_FF_FF & s.hashCode()) % size];
			while (!s.equals(n.key)) {
				n = n.next;
			}
			n.value += value;
		}

		public boolean containsKey(Step s) {
			Node n = nodes[(0x7F_FF_FF_FF & s.hashCode()) % size];
			while (n != null) {
				if (n.key.equals(s)) {
					return true;
				}
				n = n.next;
			}
			return false;
		}

		public void clear() {
			for (int i = 0; i < size; i++) {
				nodes[i] = null;
			}
		}

		private static class Node {
			private Step key;
			private int value;
			private Node next;

			public Node(Step key, int value) {
				this.key = key;
				this.value = value;
			}
		}
	}


}

