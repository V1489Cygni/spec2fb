package ru.ifmo.optimization.algorithm.muaco;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class MultiStartMuACO extends MuACO<FSM, FsmMutation> {
	
	private List<Node> starts = new ArrayList<Node>();
	
	private boolean restartWithCspSolution;

	public MultiStartMuACO(MuACOConfig<FSM, FsmMutation> config,
			AbstractOptimizationTask<FSM> task, 
			FSM initialSolution) {
		super(config, task, initialSolution);
		restartWithCspSolution = Boolean.parseBoolean(config.getProperty("restart-with-csp-solution"));
		
		for (FSM fsm : loadInitialSolutions(config.getProperty("initial-solutions-dir"))) {
			fsm.dropLabels();
			FitInstance<FSM> fi = task.getFitInstance(fsm);
			Node newNode = graph.addNode(graph.getRoot(), initialSolution.getMutations(fsm), fi, task.getNumberOfFitnessEvaluations()).getDest();
			starts.add(newNode);
		}
	}
	
	public MultiStartMuACO(MuACOConfig config,
			AbstractTaskFactory<FSM> taskFactory) {
		super(config, taskFactory);
		restartWithCspSolution = Boolean.parseBoolean(config.getProperty("restart-with-csp-solution"));
		
		for (FSM fsm : loadInitialSolutions(config.getProperty("initial-solutions-dir"))) {
			fsm.dropLabels();
			FitInstance<FSM> fi = task.getFitInstance(fsm);
			Node newNode = graph.addNode(graph.getRoot(), graph.getNodeInstance(graph.getRoot()).getMutations(fsm), fi, task.getNumberOfFitnessEvaluations()).getDest();
			starts.add(newNode);
		}		
		
//		FSM s = loadInitialSolutions(config.getProperty("initial-solutions-dir")).get(0);
//		System.out.println(s.computeStringHash() + ": " + task.getFitInstance(s, new FitnessValue(0)));
//		System.exit(1);
	}

	private List<FSM> loadInitialSolutions(String dir) {
		List<FSM> result = new ArrayList<FSM>();
		
		File directory = new File(dir);
		for (String file : directory.list()) {
			try {
				result.add(FSM.loadFromGV(dir + "/" + file, ((AbstractAutomatonTask)task).getDesiredNumberOfStates()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	@Override
	protected List<Node<FSM>> getStartNodes() {
		if (starts.isEmpty()) {
			return super.getStartNodes();
		}
		if (task.getNumberOfFitnessEvaluations() <= starts.size() + 5) {
			List<Node<FSM>> startNodes = new ArrayList<Node<FSM>>();
			startNodes.add(graph.getBestNode());
			for (int i = 0; i < starts.size(); i++) {
				startNodes.add(starts.get(i));
			}
			while (startNodes.size() < numberOfAnts) {
				startNodes.add(starts.get(ThreadLocalRandom.current().nextInt(starts.size())));
			}
			return startNodes;
		}
		return super.getStartNodes();
	}
	
	@Override
	protected FitInstance<FSM> getInitialSolutionForRestart() {
		if (restartWithCspSolution && !starts.isEmpty()) {
			System.out.println("Restarting with provided initial solution");
			return new FitInstance<FSM>(graph.getNodeInstance(starts.get(0)), starts.get(0).getFitness());
		}
		return super.getInitialSolutionForRestart();
	}
	
}
