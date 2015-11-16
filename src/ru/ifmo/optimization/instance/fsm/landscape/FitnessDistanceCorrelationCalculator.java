package ru.ifmo.optimization.instance.fsm.landscape;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeFinalStateMutator;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeOutputActionMutator;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.factory.FsmTaskFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class FitnessDistanceCorrelationCalculator implements Runnable {
	private AbstractAutomatonTask task;
	private FitInstance<FSM> globalOptimum;
	private int radius;
	private TreeSet<FSM> fsms = new TreeSet<FSM>();
	private InitialFSMGenerator fsmGenerator = new InitialFSMGenerator();
	private List<Mutator<FSM, FsmMutation>> mutators = new ArrayList<Mutator<FSM, FsmMutation>>(); 
	
	public FitnessDistanceCorrelationCalculator(AbstractAutomatonTask task, FSM globalOptimum, int radius) {
		this.task = task;
		this.globalOptimum = this.task.getFitInstance(globalOptimum);
		this.radius = radius;
		mutators.add(new ChangeFinalStateMutator());
		mutators.add(new ChangeOutputActionMutator(task.getActions(), task.getConstraints()));
	}
	
	private FSM createRandomFSM() {
		return fsmGenerator.createInstance(task);
	}
	
	private FitInstance<FSM> applyFitness(FSM fsm) {
		return task.getFitInstance(fsm);
	}

	public List<FSM> getAllMutations(FSM fsm) {
		List<FSM> result = new ArrayList<FSM>();
		for (int state = 0; state < fsm.getNumberOfStates(); state++) {
			for (int event = 0; event < fsm.getNumberOfEvents(); event++) {
				FSM.Transition t = fsm.transitions[state][event];
				int endState = t.getEndState();
				String actions = t.getAction();
				
				for (int i = 0; i < fsm.getNumberOfStates(); i++) {
					if (i == endState) {
						continue;
					}
					
					FSM mutated = new FSM(fsm);
					mutated.transitions[state][event].setEndState(i);
					result.add(mutated);
				}
				
				for (int i = 0; i < task.getActions().length; i++) {
					if (actions.equals(task.getActions()[i])) {
						continue;
					}
					FSM mutated = new FSM(fsm);
					mutated.transitions[state][event].setAction(task.getActions()[i]);
					result.add(mutated);
				}
			}
		}
		return result;
	}
	
	private void getNeighborhood() {
		List<TreeSet<FSM>> radiusSet = new ArrayList<TreeSet<FSM>>();
		TreeSet<FSM> seed = new TreeSet<FSM>();
		seed.add(globalOptimum.getInstance());
		radiusSet.add(seed);
		
		for (int i = 1; i <= radius; i++) {
			TreeSet<FSM> set = new TreeSet<FSM>();
			for (FSM fsm : radiusSet.get(i - 1)) {
				set.addAll(getAllMutations(fsm));
			}
			radiusSet.add(set);
		}
		
		for (TreeSet<FSM> set : radiusSet) {
			fsms.addAll(set);
		}
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File("neighborhood"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (FSM fsm : fsms) {
			int hammingDistance = globalOptimum.getInstance().dist(fsm);
			if (hammingDistance == 0) {
				continue;
			}
			double fitnessValue = task.getFitInstance(fsm).getFitness();		
			out.println(hammingDistance + " " + fitnessValue);
		}
		
		out.close();
	}
	
	
	@Override
	public void run() {
		getNeighborhood();
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File("fdc"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 40000; i++) {
			FitInstance<FSM> instance = applyFitness(createRandomFSM());
			FSM fsm = instance.getInstance();
			int hammingDistance = globalOptimum.getInstance().dist(fsm);
			if (hammingDistance == 0) {
				continue;
			}
			out.println(hammingDistance + " " + instance.getFitness());
		}
		
		out.close();
	}
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: java -jar fitness-distance-generator.jar [task config] [global optimum (*.transitions)]");
			System.exit(1);
		}
		String taskConfigFilename = args[0];
		String globalOptimum = args[1];
		int radius = Integer.parseInt(args[2]);
		
		AbstractTaskConfig taskConfig = new AbstractTaskConfig(taskConfigFilename);
		FsmTaskFactory taskFactory = new FsmTaskFactory(taskConfig);
		
		AbstractAutomatonTask task = (AbstractAutomatonTask) taskFactory.createTask();
		FSM.setEvents(task.getEvents());
		new Thread(new FitnessDistanceCorrelationCalculator(task, new FSM(globalOptimum), radius)).start();
	}
}
