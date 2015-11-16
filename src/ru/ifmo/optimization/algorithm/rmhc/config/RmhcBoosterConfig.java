package ru.ifmo.optimization.algorithm.rmhc.config;


public class RmhcBoosterConfig {
	private boolean useBefore;
	private boolean useAfter;
	private int maxNumberOfAfterSteps;
	private int maxNumberOfBeforeSteps;
	private double minFitnessValue;
	private double minFitnessRatio;
	
	public RmhcBoosterConfig(boolean useBefore, boolean useAfter, 
			int maxNumberOfAfterSteps, int maxNumberOfBeforeSteps, 
			double minFitnessValue, double minFitnessRatio) {
		this.useBefore = useBefore;
		this.useAfter = useAfter;
		this.maxNumberOfAfterSteps = maxNumberOfAfterSteps;
		this.maxNumberOfBeforeSteps = maxNumberOfBeforeSteps;
		this.minFitnessValue = minFitnessValue;
		this.minFitnessRatio = minFitnessRatio;
	}
	
	public boolean useAfter() {
		return useAfter;
	}
	
	public boolean useBefore() {
		return useBefore;
	}
	
	public int getMaxNumberOfAfterSteps() {
		return maxNumberOfAfterSteps;
	}
	
	public int getMaxNumberOfBeforeSteps() {
		return maxNumberOfBeforeSteps;
	}
	
	public double getMinFitnessRatio() {
		return minFitnessRatio;
	}
	
	public double getMinFitnessValue() {
		return minFitnessValue;
	}
}
