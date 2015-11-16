package ru.ifmo.optimization.instance.fsm;

import ru.ifmo.optimization.instance.FitInstance;

public class SimpleFsmMetaData extends FitInstance<FSM> {
	public SimpleFsmMetaData(FSM fsm, double fitness,  Object fitnessDependentData) {
		super(fsm, fitness);
	}
}
