package test.clock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.factory.FsmTaskFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class NeighborhoodExaminer implements Runnable {
	private AbstractAutomatonTask task;
	private FitInstance<FSM> target;
	private List<FitInstance<FSM>> automata = new ArrayList<FitInstance<FSM>>();
	
	public NeighborhoodExaminer(String taskConfigName, String targetFsmFilename) {
		
		AbstractTaskConfig config = new AbstractTaskConfig(taskConfigName);
		FsmTaskFactory factory = new FsmTaskFactory(config);
		task = (AbstractAutomatonTask) factory.createTask();
		FSM.setEvents(task.getEvents());
		FSM targetFSM = new FSM(targetFsmFilename);
		target = task.getFitInstance(targetFSM);
		
		automata.addAll(getAllMutations(target));
//		List<FitInstance<FSM>> one = getAllMutations(target);
//		for (FitInstance<FSM> md : one) {
//			automata.addAll(getAllMutations(md));
//		}
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File("fitness"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

//		Set<FSM> hashSet = new HashSet<FSM>();
		Map<Long, FSM> map = new HashMap<Long, FSM>();
		for (FitInstance<FSM> fsm : automata) {
//			if (map.containsKey(fsm.getInstance().computeStringHash())) {
//				continue;
//			}
			out.println(fsm.getFitness());
//			map.put(fsm.getInstance().computeStringHash(), fsm.getInstance());
		}
		out.close();
		
		System.out.println("Neighborhood size = " + automata.size());
	}
	
	public static List<FitInstance<FSM>> getAllMutations(FSM fsm, AbstractAutomatonTask task) {
		List<FitInstance<FSM>> result = new ArrayList<FitInstance<FSM>>();
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
					FitInstance<FSM> m = task.getFitInstance(mutated);
					result.add(m);
				}
				
				for (int i = 0; i < task.getActions().length; i++) {
					if (actions.equals(task.getActions()[i])) {
						continue;
					}
					FSM mutated = new FSM(fsm);
					mutated.transitions[state][event].setAction(task.getActions()[i]);
					FitInstance<FSM> m = task.getFitInstance(mutated);
					result.add(m);
				}
			}
		}
		return result;
	}
	
	private List<FitInstance<FSM>> getAllMutations(FitInstance<FSM> metaData) {
		FSM fsm = metaData.getInstance();
		List<FitInstance<FSM>> result = new ArrayList<FitInstance<FSM>>();
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
					FitInstance<FSM> m = task.getFitInstance(mutated);
					result.add(m);
				}
				
				for (int i = 0; i < task.getActions().length; i++) {
					if (actions.equals(task.getActions()[i])) {
						continue;
					}
					FSM mutated = new FSM(fsm);
					mutated.transitions[state][event].setAction(task.getActions()[i]);
					FitInstance<FSM> m = task.getFitInstance(mutated);
					result.add(m);
				}
			}
		}
		return result;
	}

	@Override
	public void run() {
		Map<Integer, List<Double>> data = new TreeMap<Integer, List<Double>>();
		int cnt = 0;
		for (FitInstance<FSM> md : automata) {
			int distance = target.getInstance().dist(md.getInstance());
			List<Double> values = data.get(distance);
			if (values == null) {
				values = new ArrayList<Double>();
				data.put(distance, values);
			}
			values.add(md.getFitness());
			System.out.println("Processed " + 100.0 * cnt / automata.size() + " automata");
			cnt++;
		} 

		PrintWriter out = null;
		try {
			out = new PrintWriter(new File("fitness-distance"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		List<Double> one = data.get(1);
		Collections.sort(one);
		
		for (Double d : one) {
			out.println(d);
		}
		double min = 0; 
		double max = task.getDesiredFitness();
		int numberOfBars = 100;
		double width = (max - min) / numberOfBars;
		double[] d = new double[numberOfBars];
		for (Double v : one) {
			for (int i = 0; i < d.length - 1; i++) {
				if (v >= i * width && v <= (i + 1) * width) {
					d[i]++;
				}
			}
		}
//		
//		for (Double v : d) {
//			out.println(v);
//		}
		
//		for (Entry<Integer, List<Double>> e : data.entrySet()) {
//			int distance = e.getKey();
//			double min = Collections.min(e.getValue());
//			double max = Collections.max(e.getValue());
//			double average = average(e.getValue());
//			
//			out.println(distance + " " + min + " " + average + " " + max);
//		}
		
		out.close();
	}
	
	public static void main(String[] args) {
//		new Thread(new NeighborhoodExaminer(args[0], args[1])).start();
		new Thread(new NeighborhoodExaminer("tests-model-checking.properties", "lift_transitions")).start();
	}
	
}
