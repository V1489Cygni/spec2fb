package ru.ifmo.optimization.instance.fsm.task;

import java.util.Comparator;
import java.util.List;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

/**
 * Abstract base class for a fitness function
 * @author Daniil Chivilikhin
 */
public abstract class AbstractAutomatonTask extends AbstractOptimizationTask<FSM> { 
	protected int desiredNumberOfStates;
	protected AbstractTaskConfig config;
	protected Comparator<Double> comparator;
	
	public abstract List<String> getEvents();
	
	public abstract String[] getActions();
	
	public abstract AutomatonTaskConstraints getConstraints();

	public void setDesiredNumberOfStates(int desiredNumberOfStates) {
		this.desiredNumberOfStates = desiredNumberOfStates;
	}
	
	public int getDesiredNumberOfStates() {
		return desiredNumberOfStates;
	}
	
	@Override
	public int getNeighborhoodSize() {
		return getDesiredNumberOfStates() * getEvents().size() * (getDesiredNumberOfStates() + getActions().length - 2);
	}
	
	public abstract void reset();
}