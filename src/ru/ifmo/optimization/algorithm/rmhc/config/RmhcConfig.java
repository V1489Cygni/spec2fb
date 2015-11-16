package ru.ifmo.optimization.algorithm.rmhc.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ru.ifmo.optimization.instance.Hashable;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeFinalStateMutator;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeOutputActionMutator;
import ru.ifmo.optimization.instance.fsm.mutator.LucasReynoldsMutator;
import ru.ifmo.optimization.instance.fsm.mutator.MultipleChangesMutator;
import ru.ifmo.optimization.instance.fsm.mutator.MultipleFinalStateMutator;
import ru.ifmo.optimization.instance.fsm.mutator.MultipleOutputActionsMutator;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class RmhcConfig<Instance extends Hashable> {
	
	public static enum MutatorType {
		CHANGE_DEST,
		CHANGE_ACTIONS, 
		MULTIPLE,
		MULTIPLE_DEST,
		MULTIPLE_ACTIONS,
		LUCAS_REYNOLDS
	}
	
	private Properties properties = new Properties();
	
	public RmhcConfig(String propertiesFileName) {
		try {
			properties.load(new FileInputStream(new File(propertiesFileName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public List<MutatorType> getMutatorTypes() {
		String[] listOfMutators = properties.getProperty("mutators").split(",");
		List<MutatorType> mutators = new ArrayList<MutatorType>();
		for (String s : listOfMutators) {
			MutatorType type = MutatorType.valueOf(s);
			mutators.add(type);
		}
		return mutators;
	}
	
	public List<Mutator<FSM, FsmMutation>> getMutators(AbstractOptimizationTask<Instance> t) {
		AbstractAutomatonTask task = (AbstractAutomatonTask)t;
		List<Mutator<FSM, FsmMutation>> mutators = new ArrayList<Mutator<FSM, FsmMutation>>();
		for (MutatorType type : getMutatorTypes()) {
			switch (type) {
			case CHANGE_ACTIONS:
				mutators.add(new ChangeOutputActionMutator(task.getActions(), task.getConstraints()));
				break;
			case CHANGE_DEST:
				mutators.add(new ChangeFinalStateMutator());
				break;
			case MULTIPLE:
				mutators.add(new MultipleChangesMutator(task.getActions(), task.getConstraints(), task));
				break;
			case MULTIPLE_DEST:
				mutators.add(new MultipleFinalStateMutator(task));
				break;
			case MULTIPLE_ACTIONS:
				mutators.add(new MultipleOutputActionsMutator(task.getActions(), task.getConstraints(), task));
				break;
			case LUCAS_REYNOLDS:
				mutators.add(new LucasReynoldsMutator(task.getActions(), task.getConstraints(), task));
			}
		}
		return mutators;
	}
}
