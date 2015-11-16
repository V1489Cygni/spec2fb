package ru.ifmo.optimization.instance.fsm.test;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.instance.fsm.FSM;


public class FsmTests {
	public static void main(String[] args) {
		List<String> events = new ArrayList<String>();
		events.add("A");
		events.add("B");
		
		FSM fsm = new FSM(3);
		fsm.setTransition(0, "A", 2, "");
		fsm.setTransition(0, "B", 1, "");
		
		fsm.setTransition(1, "A", 2, "");
		fsm.setTransition(1, "B", 1, "");
		
		fsm.setTransition(2, "A", 1, "");
		fsm.setTransition(2, "B", 0, "");
		
		System.out.print(fsm);
		
		System.out.println("Canonical FSM:\n" + fsm.getCanonicalFSM());
	}
}
