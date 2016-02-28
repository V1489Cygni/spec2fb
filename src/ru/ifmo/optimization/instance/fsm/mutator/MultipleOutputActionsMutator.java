package ru.ifmo.optimization.instance.fsm.mutator;

import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;

public class MultipleOutputActionsMutator implements Mutator<FSM, FsmMutation>{

	private String[] actions;
	private AutomatonTaskConstraints constraints;
	private double mutationProbability;
	
	public MultipleOutputActionsMutator(String[] actions, AutomatonTaskConstraints constraints, AbstractAutomatonTask task) {
		this.actions = actions;
		this.constraints = constraints;
		this.mutationProbability = 1.0 / ((double)task.getDesiredNumberOfStates() * task.getEvents().size());
	}

	@Override
	public MutatedInstanceMetaData<FSM, FsmMutation> apply(FSM individual) {
		MutationCollection<FsmMutation> mutations = new MutationCollection<FsmMutation>();
		FSM mutated = new FSM(individual);
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		for (int state = 0; state < individual.getNumberOfStates(); state++) {
			for (int event = 0; event < individual.getNumberOfEvents(); event++) {
				if (random.nextDouble() > mutationProbability) {
					continue;
				}
				String newAction = actions[random.nextInt(actions.length)];
				if (constraints.hasConstraints(event)) {
					if (constraints.getConstraints(event).contains(newAction)) {
						continue;
					}
				}
				if (newAction.equals(mutated.transitions[state][event].getAction())) {
					continue;
				}
				mutations.add(new FsmTransitionMutation(state, event, mutated.transitions[state][event].getEndState(), newAction, false));
				mutated.transitions[state][event].setAction(newAction);
			}
		}
		return new MutatedInstanceMetaData<FSM, FsmMutation>(mutated, mutations);
	}

	@Override
	public FSM applySimple(FSM individual) {
		return (FSM) apply(individual).getInstance();
	}
	
	private double probability;
	
	public double probability() {
		return probability;
	}
	
	@Override
	public void setProbability(double probability) {
		this.probability = probability;
	}
}
