package test.clock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.factory.FsmTaskFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class FitnessDistanceCalculator implements Runnable {

	private FitInstance<FSM> target;
	private AbstractAutomatonTask task;
	private List<FitInstance<FSM>> automata = new ArrayList<FitInstance<FSM>>();
	
	public FitnessDistanceCalculator(String taskConfigName, String targetFsmFilename, String fsmsDirname) {
		
		AbstractTaskConfig config = new AbstractTaskConfig(taskConfigName);
		FsmTaskFactory factory = new FsmTaskFactory(config);
		//FIXME
		task = null;//factory.createTask();
		FSM.setEvents(task.getEvents());
		FSM targetFSM = new FSM(targetFsmFilename);
		target = task.getFitInstance(targetFSM);
		
		File dir = new File(fsmsDirname);
		File[] files = dir.listFiles();
		for (File file : files) {
			automata.add(task.getFitInstance(new FSM(file.getPath())));
		}
	}
	
	private double average(Collection<Double> collection) {
		double sum = 0;
		for (Double c : collection) {
			sum += c;
		}
		return sum / (double)collection.size();
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
		
		for (Entry<Integer, List<Double>> e : data.entrySet()) {
			int distance = e.getKey();
			double min = Collections.min(e.getValue());
			double max = Collections.max(e.getValue());
			double average = 0; 
			for (Double v : e.getValue()) {
				average += v;
			}
			average /= e.getValue().size();
			
			out.println(distance + " " + min + " " + average + " " + max);
		}
		
		out.close();
	}
	
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Usage: task-config-name target-fsm-file fsms-file");
		}
		new Thread(new FitnessDistanceCalculator(args[0], args[1], args[2])).start();
	}
}
