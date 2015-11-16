package ru.ifmo.optimization.instance.comparator;

import java.util.Comparator;

public class MaxSingleObjectiveComparator implements Comparator<Double>{
	@Override
	public int compare(Double arg0, Double arg1) {
		return Double.compare(arg0, arg1);
	}
}
