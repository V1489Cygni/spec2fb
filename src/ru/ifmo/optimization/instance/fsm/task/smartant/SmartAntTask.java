	package ru.ifmo.optimization.instance.fsm.task.smartant;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.comparator.MaxSingleObjectiveComparator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.util.Pair;

/**
 * Class implementing calculation of the fitness function
 * for the "Smart ant - 1" problem.
 * @author Daniil Chivilikhin
 */
public class SmartAntTask extends AbstractAutomatonTask {
	public static double TIME_FITNESS_COMPUTATION;
	public SmartAnt antTask;
	protected int currentState;
	protected String[] actions = new String[]{"L", "R", "M"};
	protected List<String> events = Arrays.asList(new String[]{"N", "F"});
	protected int maxNumberOfSteps;
	private final FitnessCalculator fitnessCalculator;
	
	private interface FitnessCalculator {
		double getFitness(int numberOfEatenApples, int numberOfSteps, int maxNumberOfSteps, int numberOfVisitedStates);
	}
	
	private class CanonicalFitnessCalculator implements FitnessCalculator {
		@Override
		public double getFitness(int numberOfEatenApples, int numberOfSteps,
				int maxNumberOfSteps, int numberOfVisitedStates) {
//			return numberOfEatenApples + 1.0 - (double)numberOfSteps / (double)(maxNumberOfSteps);
			return numberOfEatenApples + (maxNumberOfSteps - 1 - numberOfSteps) / (double)maxNumberOfSteps;
		}
	}
	
	private class NumberOfUsedStatesFitnessCalculator implements FitnessCalculator {
		@Override
		public double getFitness(int numberOfEatenApples, int numberOfSteps,
				int maxNumberOfSteps, int numberOfVisitedStates) {
			if (numberOfEatenApples < 89) {
				return numberOfEatenApples + 0.01 * (1.0 - numberOfSteps / maxNumberOfSteps);
			}
			return numberOfEatenApples + 0.1 * (desiredNumberOfStates - numberOfVisitedStates) + 0.01 * (1.0 - numberOfSteps / maxNumberOfSteps);
		}
	}
	
	public SmartAntTask(AbstractTaskConfig config) {
		antTask = new SmartAnt(config.getProperty("field"));
		this.config = config;
		this.desiredFitness = config.getDesiredFitness();
		this.desiredNumberOfStates = Integer.parseInt(config.getProperty("desired-number-of-states"));
		maxNumberOfSteps = Integer.parseInt(config.getProperty("max-number-of-steps"));
		if (config.getProperty("fitness-calculator") == null) {
			throw new IllegalArgumentException("Null field fitness-calculator");
		}
		if (config.getProperty("fitness-calculator").equals("canonical")) {
			fitnessCalculator = new CanonicalFitnessCalculator();
		} else if (config.getProperty("fitness-calculator").equals("number-of-states")) {
			fitnessCalculator = new NumberOfUsedStatesFitnessCalculator();
		} else {
			throw new IllegalArgumentException("Illegal field fitness-calculator");
		}
		
		comparator = new MaxSingleObjectiveComparator();
	}
	
	@Override
	public FitInstance<FSM> getFitInstance(FSM automaton) {
		long start = System.currentTimeMillis();
		numberOfFitnessEvaluations++;
		antTask.reset();
		Set<Integer> visitedStates = new HashSet<Integer>();
		int numberOfEatenApples = 0;
		int numberOfSteps = 0;
		FSM fsm = new FSM(automaton);
		currentState = automaton.getInitialState();
		visitedStates.add(currentState);
		for (; numberOfSteps < maxNumberOfSteps; numberOfSteps++) {
			numberOfEatenApples += makeSimpleMove(automaton);
			visitedStates.add(currentState);
			if (numberOfEatenApples == SmartAnt.NUMBER_OF_APPLES) {
				break;
			}
		}
		TIME_FITNESS_COMPUTATION += (System.currentTimeMillis() - start) / 1000.0;
		//FIXME make not null
		return null;
//		return new SimpleFsmMetaData(fsm, 
//				fitnessCalculator.getFitness(numberOfEatenApples, numberOfSteps, maxNumberOfSteps, visitedStates.size()));
	}
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM automaton) {
		numberOfFitnessEvaluations++;
		antTask.reset();
		int numberOfEatenApples = 0;
		int numberOfSteps = 0;
		FSM dfa = new FSM(automaton);
		Set<Transition> visitedTransitions = new HashSet<Transition>();
		Set<Integer> visitedStates = new HashSet<Integer>();
		currentState = automaton.getInitialState();
		for (int i = 0; i < maxNumberOfSteps; i++) {
			Pair<Integer, Transition> p = makeMove(automaton);
			visitedTransitions.add(p.second);
			numberOfEatenApples += p.first;
			numberOfSteps = i;
			visitedStates.add(currentState);
			if (numberOfEatenApples == SmartAnt.NUMBER_OF_APPLES) {
				break;
			}
		}
		return new FsmMetaData(dfa, visitedTransitions, 				
						fitnessCalculator.getFitness(numberOfEatenApples, numberOfSteps, maxNumberOfSteps, visitedStates.size()));
	}

	
	@Override
	public String[] getActions() {
		return actions;
	}
	
	@Override
	public List<String> getEvents() {
		return events;
	}
	
	protected Pair<Integer, Transition> makeMove(FSM automaton) {
		int event = antTask.nextIsFood() ? 1 : 0;
    	FSM.Transition transition =  automaton.getTransition(currentState, event);    	    					
		int endState = transition.getEndState();
		Transition newTransition = new Transition(transition);
		currentState = endState;
		
		if (endState != -1) {
			String action = transition.getAction();
			if (action.equals("M")) {
				return new Pair<Integer, Transition>(antTask.move(), newTransition);				
			}			
			if (action.equals("L")) {
				antTask.turnLeft();
				return new Pair<Integer,Transition>(0, newTransition);
			}
			if (action.equals("R")) {
				antTask.turnRight();
				return new Pair<Integer, Transition>(0, newTransition);
			}
		} 
		return new Pair<Integer, Transition>(0, newTransition);
	}
	
	protected int makeSimpleMove(FSM automaton) {
		int event = antTask.nextIsFood() ? 1 : 0;
    	FSM.Transition transition =  automaton.getTransition(currentState, event);    	    					
		int endState = transition.getEndState();
		currentState = endState;
		
		if (endState != -1) {
			String action = transition.getAction();
			if (action.equals("M")) {
				return antTask.move();				
			}			
			if (action.equals("L")) {
				antTask.turnLeft();
				return 0;
			}
			if (action.equals("R")) {
				antTask.turnRight();
				return 0;
			}
		} 
		return 0;
	}
	
	@Override
	public AutomatonTaskConstraints getConstraints() {
		return new AutomatonTaskConstraints();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Comparator<Double> getComparator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public double correctFitness(double fitness, FSM cachedInstance, FSM trueInstance) {
		return 0;
	}
}
