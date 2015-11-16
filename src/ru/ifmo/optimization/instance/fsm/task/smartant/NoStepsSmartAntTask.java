package ru.ifmo.optimization.instance.fsm.task.smartant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.comparator.MaxSingleObjectiveComparator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.SimpleFsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class NoStepsSmartAntTask extends AbstractAutomatonTask {
	public static double TIME_FITNESS_COMPUTATION = 0;
	private int maxNumberOfSteps;
	protected String[] actions = new String[]{"L", "R", "M"};
	protected List<String> events = Arrays.asList(new String[]{"N", "F"});
	
	int n;
	int m;
	int[][] field;

	int[] dr = new int[] { 0, 1, 0, -1 };
	int[] dc = new int[] { 1, 0, -1, 0 };
	
	public NoStepsSmartAntTask(AbstractTaskConfig config) {
		this.config = config;
		this.desiredFitness = config.getDesiredFitness();
		this.desiredNumberOfStates = Integer.parseInt(config.getProperty("desired-number-of-states"));
		maxNumberOfSteps = Integer.parseInt(config.getProperty("max-number-of-steps"));
		if (config.getProperty("fitness-calculator") == null) {
			throw new IllegalArgumentException("Null field fitness-calculator");
		}
		readField(config.getProperty("field"));
		comparator = new MaxSingleObjectiveComparator();
	}
	
	private void readField(String filename) {
		Scanner in = null; 
		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		n = in.nextInt();
		m = in.nextInt();
		field = new int[n][m];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				field[i][j] = in.nextInt();
			}
		}
	}
	
	@Override
	public List<String> getEvents() {
		return events;
	}

	@Override
	public String[] getActions() {
		return actions;
	}

	@Override
	public AutomatonTaskConstraints getConstraints() {
		return new AutomatonTaskConstraints();
	}
	
	protected double getFitness(FSM instance, boolean needTransitions, Set<Transition> usedTransitions, boolean dumpField) {
		numberOfFitnessEvaluations++;
		int cr = 0;
		int cc = 0;
		int dir = 0;

		int state = instance.getInitialState();

		int fitness = 0;
		int[][] curField = new int[n][m];
		for (int i = 0; i < n; i++) {

			for (int j = 0; j < m; j++) {
				curField[i][j] = field[i][j];
			}
		}
		int last = maxNumberOfSteps - 1;
		
		for (int i = 0; i < maxNumberOfSteps; i++) {
			if (curField[cr][cc] == 1) {
				last = i;
			}
			curField[cr][cc] = 0;
			
			if (dumpField) {
				dumpField(i, curField);
			}
			
			if (fitness == (int)desiredFitness) {
				break;
			}
			int food = curField[(cr + dr[dir] + n) % n][(cc + dc[dir] + m) % m];

			Transition t = instance.transitions[state][food];
			if (needTransitions) {
				usedTransitions.add(t);
			}
			String action = t.getAction();
			int newState = t.getEndState();
			if (action.equals("R")) {
				dir++;
				dir += 4;
				dir %= 4;
			}
			if (action.equals("L")) {
				dir--;
				dir += 4;
				dir %= 4;
			}
			if (action.equals("M")) {
				fitness += food;
				cr += dr[dir];
				cr += n;
				cr %= n;

				cc += dc[dir];
				cc += m;
				cc %= m;
			}
			state = newState;
		}
//		System.out.println("last = " + last);
		return fitness;	
	}
	
	protected void dumpField(int step, int[][] currentField) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File("step_" + step));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				out.print(currentField[i][j]);
			}
			out.println();
		}
		
		out.close();
	}

	@Override
	public FitInstance<FSM> getFitInstance(FSM instance) {
		long start = System.currentTimeMillis();
		Set<Transition> usedTransitions = new HashSet<Transition>();
		double fitness =  getFitness(instance, true, usedTransitions, false);
		instance.markUsedTransitions(usedTransitions);
		FitInstance<FSM> result = new SimpleFsmMetaData(instance, 
				fitness, instance.getUsedTransitions());
		TIME_FITNESS_COMPUTATION += (System.currentTimeMillis() - start) / 1000.0;
		return result;
	}

	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM instance) {
		Set<Transition> usedTransitions = new HashSet<FSM.Transition>();
		return new FsmMetaData(instance, usedTransitions, getFitness(instance, true, usedTransitions, false));
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Comparator<Double> getComparator() {
		return comparator;
	}
	
	@Override
	public double correctFitness(double fitness, FSM cachedInstance, FSM trueInstance) {
		return 0;
	}

}
