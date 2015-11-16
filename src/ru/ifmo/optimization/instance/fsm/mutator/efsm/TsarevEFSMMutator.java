package ru.ifmo.optimization.instance.fsm.mutator.efsm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.mutation.FsmInitialStateMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;
import ru.ifmo.random.RandomProvider;
import ru.ifmo.util.Util;

public class TsarevEFSMMutator implements Mutator<FSM, FsmMutation> {
	private List<String> events;
	private String[] actions;
	private Set<String> eventsSet;
	private double addDeleteTransitionProbability;
	private double mutateTransitionProbability;
	private double mutateInitialStateProbability;
	
	public TsarevEFSMMutator(List<String> events, String[] actions, 
			double deleteTransitionProbability, double mutateTransitionProbability, double mutateInitialStateProbability) {
		this.events = events;
		this.actions = actions;
		eventsSet = new HashSet<String>();
		eventsSet.addAll(events);
		this.addDeleteTransitionProbability = deleteTransitionProbability;
		this.mutateTransitionProbability = mutateTransitionProbability;
		this.mutateInitialStateProbability = mutateInitialStateProbability;
	}
	
	private boolean hasTransition(Transition[] transitions) {
		for (Transition t : transitions) {
			if (t.getEndState() != -1) {
				return true;
			}
		}
		return false;
	}

	private int getTransitionIdToDelete(Transition[] transitions) {
		int transitionToDelete = RandomProvider.getInstance().nextInt(Util.numberOfExistingTransitions(transitions));
		int existingTransitionCount = 0;
		for (int i = 0; i < transitions.length; i++) {
			if (transitions[i].getEndState() != -1) {
				if (existingTransitionCount == transitionToDelete) {
					return i;
				}
				existingTransitionCount++;
			}
		}
		return -1;
	}
	
	@Override
	public MutatedInstanceMetaData<FSM, FsmMutation> apply(FSM individual) {
		FSM mutated = new FSM(individual);
		MutationCollection<FsmMutation> mutations = new MutationCollection<FsmMutation>();
		
		if (RandomProvider.getInstance().nextDouble() < mutateInitialStateProbability) {
			int newInitialState = RandomProvider.getInstance().nextInt(individual.getNumberOfStates());
			while (newInitialState == individual.getInitialState()) {
				newInitialState = RandomProvider.getInstance().nextInt(individual.getNumberOfStates());
			}

			mutated.setInitialState(newInitialState);
			FsmInitialStateMutation mutation = new FsmInitialStateMutation(newInitialState);
			mutations.add(mutation);
		}
		
		//mutated each transition with a certain probability
		for (int state = 0; state < mutated.getNumberOfStates(); state++) {
			for (int eventId = 0; eventId < events.size(); eventId++) {
				//won't mutate deleted transitions
				if (mutated.transitions[state][eventId].getEndState() == -1) {
					continue;
				}
				if (RandomProvider.getInstance().nextDouble() < mutateTransitionProbability) {
					Transition transition = mutated.transitions[state][eventId];
					int transitionMutationType = RandomProvider.getInstance().nextInt(3);
					switch (transitionMutationType) {
					case 0: {
						//change transition end state
						int currentEndState = mutated.transitions[state][eventId].getEndState();
						int newState = RandomProvider.getInstance().nextInt(individual.getNumberOfStates());
						while (newState == currentEndState) {
							newState = RandomProvider.getInstance().nextInt(individual.getNumberOfStates());
						}
						mutated.transitions[state][eventId].setEndState(newState);
						FsmTransitionMutation mutation = new FsmTransitionMutation(state, eventId, newState, mutated.transitions[state][eventId].getAction());
						mutations.add(mutation);
						break;
					}
					case 1: {
						//change number of actions on transition
						int outputSize = Integer.parseInt(mutated.transitions[state][eventId].getAction());
						int newOutputSize;
						if (RandomProvider.getInstance().nextBoolean()) {
							newOutputSize = Math.min(outputSize + 1, actions.length);
						} else {
							newOutputSize = Math.max(0, outputSize - 1);
						}
					
						String newAction = newOutputSize + "";
						mutated.transitions[state][eventId].setAction(newAction);
						FsmTransitionMutation mutation = new FsmTransitionMutation(state, eventId, mutated.transitions[state][eventId].getEndState(), newAction);
						mutations.add(mutation);
						break;
					}
					case 2: {
						//change transition input event
						int newInputId = RandomProvider.getInstance().nextInt(events.size());
						while (newInputId == eventId) {
							newInputId = RandomProvider.getInstance().nextInt(events.size());
						}
						
						mutated.transitions[state][newInputId].setEndState(transition.getEndState());
						mutated.transitions[state][newInputId].setAction(transition.getAction());

						transition.setEndState(-1);
						transition.setAction("");

						mutations.add(new FsmTransitionMutation(state, eventId, -1, ""));
						mutations.add(new FsmTransitionMutation(state, newInputId, 
								mutated.transitions[state][newInputId].getEndState(),
								mutated.transitions[state][newInputId].getAction()));
					}
						break;
					}
				}
			}
 		}
		
		//add and delete transitions
		for (int state = 0; state < mutated.getNumberOfStates(); state++) {
			if (RandomProvider.getInstance().nextDouble() < addDeleteTransitionProbability) { 
    			if (RandomProvider.getInstance().nextBoolean()) {
    				if (hasTransition(mutated.transitions[state])) {
    					//delete a transition
    					int transitionToDelete = getTransitionIdToDelete(mutated.transitions[state]);
    					if (transitionToDelete == -1) {
    						continue;
    					}
    					
    					FsmTransitionMutation mutation = new FsmTransitionMutation(state, transitionToDelete, -1, "");
    					mutated.transitions[state][transitionToDelete].setEndState(-1);
    					mutated.transitions[state][transitionToDelete].setAction("");
    					mutations.add(mutation);
    				}
    			} else {
    				if (Util.numberOfExistingTransitions(mutated.transitions[state]) < events.size()) {
    					//add a transition
    					
    					//first, get a set of all used input events
    					Set<String> usedEvents = new HashSet<String>();
    					for (Transition t : mutated.transitions[state]) {
    						if (t.getEndState() == -1) {
    							continue;
    						}
    						usedEvents.add(t.getEvent());
    					}
    					List<String> unusedEvents = new ArrayList<String>();
    					for (String e : events) {
    						if (!usedEvents.contains(e)) {
    							unusedEvents.add(e);
    						}
    					}
    					
    					//select random unused event, add a random transition with this event
    					String input = unusedEvents.get(RandomProvider.getInstance().nextInt(unusedEvents.size()));
    					int indexOfInput = events.indexOf(input);
    					String newAction = "1";//actions[RandomProvider.instance().get().nextInt(actions.length)];
    					int newDestinationState = RandomProvider.getInstance().nextInt(mutated.getNumberOfStates());
    					mutated.transitions[state][indexOfInput].setAction(newAction);
    					mutated.transitions[state][indexOfInput].setEndState(newDestinationState);
    					FsmTransitionMutation mutation = new FsmTransitionMutation(state, indexOfInput, newDestinationState, newAction);
    					mutations.add(mutation);
    				}
    			}
    		}
		}
		
		
		return Util.makeCompliantFSM(new MutatedInstanceMetaData<FSM, FsmMutation>(mutated, mutations));
	}

	@Override
	public FSM applySimple(FSM individual) {
		return apply(individual).getInstance();
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
