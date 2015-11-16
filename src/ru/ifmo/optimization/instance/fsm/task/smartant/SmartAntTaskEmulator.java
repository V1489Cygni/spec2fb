package ru.ifmo.optimization.instance.fsm.task.smartant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class SmartAntTaskEmulator extends SmartAntTask implements Runnable {

	private FSM fsm;
	public SmartAntTaskEmulator(AbstractTaskConfig config, String filename) {
		super(config);
		FSM.setEvents(getEvents());
		fsm = new FSM(filename);
	}

	private void dumpField(int step) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File("step_" + step));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < antTask.fieldHeight; i++) {
			for (int j = 0; j < antTask.fieldWidth; j++) {
				out.print(antTask.field[i][j].hasFood() ? 1 : 0);
			}
			out.println();
		}
		
		out.close();
	}
	
	@Override
	public FitInstance<FSM> getFitInstance(FSM automaton) {
		numberOfFitnessEvaluations++;
		antTask.reset();
		Set<Integer> visitedStates = new HashSet<Integer>();
		int numberOfEatenApples = 0;
		int numberOfSteps = 0;
		FSM dfa = new FSM(automaton);
		currentState = automaton.getInitialState();
		visitedStates.add(currentState);
		dumpField(numberOfSteps);
		for (; numberOfSteps < maxNumberOfSteps; numberOfSteps++) {
			numberOfEatenApples += makeSimpleMove(automaton);
			visitedStates.add(currentState);
			dumpField(numberOfSteps);
			if (numberOfEatenApples == SmartAnt.NUMBER_OF_APPLES) {
				break;
			}
		}
//		double fitness = numberOfEatenApples + 1.0 - (double)numberOfSteps / (double)(maxNumberOfSteps);
		
		double fitness = numberOfEatenApples + (maxNumberOfSteps - 1 - numberOfSteps) / (double)maxNumberOfSteps;
		
		//FIXME make not null
		return null;
//		return new SimpleFsmMetaData(dfa, fitness);
	}

	@Override
	public void run() {
		FitInstance<FSM> instance = getFitInstance(fsm);
		System.out.println("f = " + instance.getFitness());
	}
	
	public static void main(String[] args) {
		new Thread(new SmartAntTaskEmulator(new AbstractTaskConfig(args[0]), args[1])).start();
//		new Thread(new SmartAntTaskEmulator(new AbstractAutomatonTaskConfig("smart-ant-task.properties"), "fsm.fsm")).start();
	}
}
