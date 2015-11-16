package ru.ifmo.optimization.instance.fsm.landscape;

public class CachedFitnessData {
	private final double baseFitness;
	private int neighbourCount = 0 ;
	private double fitnessSum = 0;
	private double betterFitnessSum = 0;
	private int betterFitnessCount = 0;
	private int neutralFitnessCount = 0;
	private double worseFitnessSum = 0;
	private int worseFitnessCount = 0;
	
	public CachedFitnessData(double baseFitness) {
		this.baseFitness = baseFitness;
	}
	
	public CachedFitnessData(
			double baseFitness,
			int neighbourCount,
			double fitnessSum,
			double betterFitnessSum,
			int betterFitnessCount,
			double neutralFitnessSum,
			double worseFitnessSum,
			int worseFitnessCount) {
		this.baseFitness = baseFitness;
		this.neighbourCount = neighbourCount;
		this.fitnessSum = fitnessSum;
		this.betterFitnessSum = betterFitnessSum;
		this.betterFitnessCount = betterFitnessCount;
		this.worseFitnessSum = worseFitnessSum;
		this.worseFitnessCount = worseFitnessCount;
	}
	
	public void add(double fitness) {
		neighbourCount++;
		fitnessSum += fitness;
		if (fitness > baseFitness) {
			betterFitnessSum += fitness;
			betterFitnessCount++;
		} else if (fitness < baseFitness) {
			worseFitnessSum += fitness;
			worseFitnessCount++;
		} else {
			neutralFitnessCount++;
		}
	}
	
    public double getBaseFitness() {
		return baseFitness;
	}
    
    public int getNeighbourCount() {
    	return neighbourCount;
    }
	
	public double getMeanNeighbourFitness() {
		return (neighbourCount == 0) ? 0 : fitnessSum / (double)neighbourCount;
	}
	
	public double getMeanBetterFitness() {
		 return (betterFitnessCount == 0) ? 0 : betterFitnessSum / (double)betterFitnessCount; 
	}
	
	public double getBetterFitnessCount() {
		return betterFitnessCount;
	}
	
	public double getMeanWorseFitness() {
		return (worseFitnessCount == 0) ? 0 : worseFitnessSum / (double)worseFitnessCount;
	}
	
	public double getIncreaseProbability() {
		return neighbourCount == 0 ? 0 : (double)betterFitnessCount / (double)neighbourCount;
	}
	
	public double getDecreaseProbability() {
		return neighbourCount == 0 ? 0 : (double)worseFitnessCount / (double)neighbourCount;
	}
	
	public double getNeutralProbability() {
		return neighbourCount == 0 ? 0 : (double)neutralFitnessCount / (double)neighbourCount;
	}
}
