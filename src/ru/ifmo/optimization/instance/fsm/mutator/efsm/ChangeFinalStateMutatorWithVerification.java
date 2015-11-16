package ru.ifmo.optimization.instance.fsm.mutator.efsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;
import ru.ifmo.random.RandomProvider;
import ru.ifmo.util.Util;

public class ChangeFinalStateMutatorWithVerification implements Mutator<FSM, FsmMutation> {
	private class TranWithWeight implements Comparable<TranWithWeight> {
		int state;
		int event;
		double weight;
		
		public TranWithWeight(int state, int event, double weight) {
			this.state = state;
			this.event = event;
			this.weight = weight;
		}

		@Override
		public int compareTo(TranWithWeight o) {
			return Double.compare(weight, o.weight);
		}
		
		@Override
		public String toString() {
			return "(" + state + ", " + event + "): " + weight;
		}
	}
	
	private double probability;
	private double ordinaryTransitionMutationProbability;
	private double counterexampleTransitionMutationProbability;
	
	public ChangeFinalStateMutatorWithVerification(double ordinaryTransitionMutationProbability, 
			double counterexampleTransitionMutationProbability) {
		this.ordinaryTransitionMutationProbability = ordinaryTransitionMutationProbability;
		this.counterexampleTransitionMutationProbability = counterexampleTransitionMutationProbability;
	}

	@Override
	public MutatedInstanceMetaData<FSM, FsmMutation> apply(FSM individual) {
		FSM mutated = new FSM(individual);

		if (!Util.hasTransitions(mutated)) {
			return null;
		}
		
		List<TranWithWeight> list = new ArrayList<TranWithWeight>();
		
		for (int s = 0; s < individual.getNumberOfStates(); s++) {
			if (Util.numberOfExistingTransitions(mutated.transitions[s]) == 0) {
				continue;
			}
			for (int e = 0; e < individual.getNumberOfEvents(); e++) {
				if (mutated.transitions[s][e].getEndState() == -1) {
					continue;
				}
				list.add(new TranWithWeight(s, e,  
						individual.isTransitionInCounterexample(s, e) 
						? counterexampleTransitionMutationProbability 
						: ordinaryTransitionMutationProbability));
			}
		}
		
		Collections.sort(list);
		
		int size = list.size();
		double weight[] = new double[size];
		weight[0] = list.get(0).weight;

		for (int i = 1; i < size; i++) {
			weight[i] = weight[i - 1] + list.get(i).weight;
		}
		double p = weight[size - 1] * RandomProvider.getInstance().nextDouble();
		int j = 0;

		while (p > weight[j]) {
			j++;
		}
		
		int state = list.get(j).state;
		int event = list.get(j).event;
		
		int currentEndState = mutated.transitions[state][event].getEndState();
		int newState = RandomProvider.getInstance().nextInt(mutated.getNumberOfStates());
		while (newState == currentEndState) {
			newState = RandomProvider.getInstance().nextInt(mutated.getNumberOfStates());
		}
		
		mutated.transitions[state][event].setEndState(newState);
		FsmTransitionMutation mutation = new FsmTransitionMutation(
				state, event, newState, mutated.transitions[state][event].getAction(), false);
		return new MutatedInstanceMetaData<FSM, FsmMutation>(mutated, new MutationCollection<FsmMutation>(mutation));
	}

	@Override
	public FSM applySimple(FSM individual) {
		return (FSM) apply(individual).getInstance();
	}

	@Override
	public double probability() {
		return probability;
	}

	@Override
	public void setProbability(double probability) {
		this.probability = probability;
	}
}