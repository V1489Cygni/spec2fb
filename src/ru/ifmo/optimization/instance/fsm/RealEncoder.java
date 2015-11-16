package ru.ifmo.optimization.instance.fsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RealEncoder <Value> {
	private class Interval {
		private Value codedValue;
		private double start;
		private double finish;
		public Interval(Value codedValue, double start, double finish) {
			this.codedValue = codedValue;
			this.start = start;
			this.finish = finish;
		}
		public Value getCodedValue() {
			return codedValue;
		}
		
		public double getMiddle() {
			return 0.5 * (finish + start);
		}
		boolean isInside(double value) {
			return value >= start && value <= finish;
		}
	}
	
	private List<Interval> intervals = new ArrayList<Interval>(); 
	private Map<Value, Double> forwardMap = new TreeMap<Value, Double>();
	
	public RealEncoder(List<Value> values) {
		double intervalStep = 1.0 / values.size();
		double counter = 0;
		for (Value value : values) {
			Interval interval = new Interval(value, counter, counter + intervalStep);
			intervals.add(interval);
			counter += intervalStep;
			forwardMap.put(value, interval.getMiddle());
		}
	}
	
	public double encode(Value value) {
		if (forwardMap.containsKey(value)) {
			return forwardMap.get(value);
		}
		return -1;
	}
	
	public Value decode(double value) {
		for (Interval interval : intervals) {
			if (interval.isInside(value)) {
				return interval.getCodedValue();
			}
		}
		System.out.println("Error whlie decoding value: " + value);
		return null;
	}
	
	public static void main(String[] args) {
		List<String> values = new ArrayList<String>();
		values.add("x1");
		values.add("x2");
		RealEncoder<String> coder = new RealEncoder<String>(values);
		System.out.println(coder.encode("x1"));
		System.out.println(coder.encode("x3"));
		System.out.println(coder.decode(0.1));
		System.out.println(coder.decode(0.4));
		System.out.println(coder.decode(0.6));
		System.out.println(coder.decode(0.9));
	}
}
