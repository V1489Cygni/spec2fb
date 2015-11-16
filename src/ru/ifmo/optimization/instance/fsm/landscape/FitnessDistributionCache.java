package ru.ifmo.optimization.instance.fsm.landscape;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class FitnessDistributionCache {
	private Map<Double, CachedFitnessData> dist = new TreeMap<Double, CachedFitnessData>();
	
	public FitnessDistributionCache() {
	}
	
	private Double normalizeDouble(Double value) {
		return 0.01 * ((int)(value * 100));
	}
	
	public void add(Double fitness, Double neighbourFitness) {
		Double normalizedFitness = normalizeDouble(fitness);
		Double normalizedNeighbourFitness = normalizeDouble(neighbourFitness);
		
		if (dist.containsKey(normalizedFitness)) {
			dist.get(normalizedFitness).add(normalizedNeighbourFitness);
		} else {
			CachedFitnessData cfd = new CachedFitnessData(normalizedFitness);
			cfd.add(normalizedNeighbourFitness);
			dist.put(normalizedFitness, cfd);
		}
	}
	
	public void add(Double fitness, Collection<Double> neighbourFitnesses) {
		for (Double neighbourFitness : neighbourFitnesses) {
			add(fitness, neighbourFitness);
		}
	}

	public boolean contains(Double fitness) {
		return dist.containsKey(normalizeDouble(fitness));
	}
	
	public CachedFitnessData get(Double fitness) {
		return dist.get(normalizeDouble(fitness));
	}
	
	public double approximateMeanIncreaseProbability(Double fitness) {
		double succesfulMutations = 0;
		double totalMutations = 0;
		for (Entry<Double, CachedFitnessData> e : dist.entrySet()) {
			succesfulMutations += e.getValue().getBetterFitnessCount();
			totalMutations += e.getValue().getNeighbourCount();
		}
		return succesfulMutations / totalMutations;
	}
	
	public double approximateMeanNeighborFitness(Double fitness) {
		double meanFitness = 0;
		double totalMutations = 0;
		for (Entry<Double, CachedFitnessData> e : dist.entrySet()) {
			meanFitness += e.getValue().getMeanNeighbourFitness();
//			totalMutations += e.getValue().getNeighbourCount();
		}
		
		return meanFitness / dist.size();
	}
	
	public CachedFitnessData approximateClosestMatch(Double fitness, int thresholdNeighborCount) {
		CachedFitnessData closestCFD = null;
		double smallestDeltaFitness = Double.MAX_VALUE;
		
		for (Entry<Double, CachedFitnessData> e : dist.entrySet()) {
			double deltaFitness = Math.abs(e.getKey() - fitness);
			if (deltaFitness < 1e-5) {
				continue;
			}
			if (e.getValue().getNeighbourCount() < thresholdNeighborCount) {
				continue;
			}
			if (deltaFitness < smallestDeltaFitness) {
				smallestDeltaFitness = deltaFitness;  
				closestCFD = e.getValue();
			}
		}
		return closestCFD;
	}
	
	public void dumpIncreaseProbability(int iterationNumber) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File("increase-probability." + iterationNumber));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (Entry<Double, CachedFitnessData> e : dist.entrySet()) {
			out.println(e.getKey() + " " +  e.getValue().getIncreaseProbability());
		}
		out.close();
	}
	
	public void dumpFitnessDistribution(int iterationNumber) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File("fitness-distribution." + iterationNumber));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (Entry<Double, CachedFitnessData> e : dist.entrySet()) {
			out.println(e.getKey() + " " +  e.getValue().getNeighbourCount());
		}
		out.close();
	}
}
