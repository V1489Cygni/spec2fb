package ru.ifmo.optimization.algorithm.muaco.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;

public class AntStats<Instance> {
	private double bestFitness;
	private Node bestNode;
	private Instance bestInstance;
	private int lastBestFitnessColonyIterationNumber;
	private int colonyIterationIndex;
	private double bestNodeGenerationTime;
	private double totalAntPathLength = 0;
	private double totalAntPathCount = 0;
	private List<Double> bestFitnessHistory = new ArrayList<Double>();
	private List<Integer> stepsHistory = new ArrayList<Integer>();
	private PrintWriter bestLog;
	private double startTime;
	private boolean writeThreadLogs;
	
	public AntStats(int colonyIterationIndex, double bestFitness, Node bestNode, int lastBestFitnessOccurence, boolean writeThreadLogs) {
		this.colonyIterationIndex = colonyIterationIndex;
		this.bestFitness = bestFitness;
		this.bestNode = bestNode;
		this.lastBestFitnessColonyIterationNumber = lastBestFitnessOccurence;
		this.startTime = OptimizationAlgorithmCutoff.getInstance().getStartTime();
		this.writeThreadLogs = writeThreadLogs;
	}
	
	public void initLog() {
		if (writeThreadLogs) {
			try {
				bestLog = new PrintWriter(new File(Thread.currentThread().getId() + ".thread-log"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}	

			File file = new File(Thread.currentThread().getId() + "-best");
			file.mkdir();
		}
	}
	
	public double getBestFitness() {
		return bestFitness;
	}
	
	public Node getBestNode() {
		return bestNode;
	}
	
	public Instance getBestInstance() {
		return bestInstance;
	}
	
	public int getLastBestFitnessOccurence() {
		return lastBestFitnessColonyIterationNumber;
	}
	
	public void setBest(Node bestNode, Instance bestInstance, double bestTime) {
		this.bestNode = bestNode;
		this.bestInstance = bestInstance;
		this.bestNodeGenerationTime = bestTime;
		this.bestFitness = bestNode.getFitness();
		
		if (bestLog != null) {
			bestLog.append((System.currentTimeMillis() - startTime) / 1000.0 + " " + bestFitness + "\n");
			bestLog.flush();
		}
		
		if (writeThreadLogs && bestFitness > 0.9) {
			File file = new File(Thread.currentThread().getId() + "-best");
			if (!file.exists()) {
				return;
			}

			PrintWriter out = null;
			try {
				out = new PrintWriter(new File(Thread.currentThread().getId() + "-best/" + bestNode.getFitness()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			out.println(bestInstance);
			out.close();
		}
	}
	
	public void setLastBestFitnessColonyIterationNumber(int lastBestFitnessOccurence) {
		this.lastBestFitnessColonyIterationNumber = lastBestFitnessOccurence;
	}
	
	public void addAntPathData(List<Path> paths) {
		for (Path path : paths) {
			totalAntPathLength += path.getLength();
		}
		totalAntPathCount += paths.size();
	}
	
	public double getMeanAntPathLength() {
		return totalAntPathCount == 0 ? 0 : totalAntPathLength / totalAntPathCount;
	}
	
	public void addHistory(int steps, double bestFitness) {
		stepsHistory.add(steps);
		bestFitnessHistory.add(bestFitness);
	}
	
	public List<Double> getBestFitnessHistory() {
		return bestFitnessHistory;
	}
	
	public List<Integer> getStepsHistory() {
		return stepsHistory;
	}
	
	public int getColonyIterationNumber() {
		return colonyIterationIndex;
	}
	
	public void setColonyIterationNumber(int colonyIterationIndex) {
		this.colonyIterationIndex = colonyIterationIndex;
	}
	
	public double getBestNodeGenerationTime() {
		return bestNodeGenerationTime;
	}

    public double getLastFitness() {
        if (stepsHistory.isEmpty()) {
            return 0;
        }
        return stepsHistory.get(stepsHistory.size() - 1);
    }
}
