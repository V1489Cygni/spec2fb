package ru.ifmo.optimization.instance.fsm;

import java.util.List;

import ru.ifmo.util.Util;

public class FsmRealEncoder {
	private static RealEncoder<Integer> stateEncoder;
	private static RealEncoder<String> actionEncoder; 
	private static FsmRealEncoder instance;
	
	public static FsmRealEncoder getInstance() {
		return instance;
	}
	
	public static void init(int numberOfStates, List<String> actions) {
		stateEncoder = new RealEncoder<Integer>(Util.intRange(0, numberOfStates - 1));		
		actionEncoder = new RealEncoder<String>(actions);
	}
	
	public static RealEncoder<Integer> stateEncoder() {
		return stateEncoder;
	}
	
	public static RealEncoder<String> actionEncoder() {
		return actionEncoder;
	}
}
