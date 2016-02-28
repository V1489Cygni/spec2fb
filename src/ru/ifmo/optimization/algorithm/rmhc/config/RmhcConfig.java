package ru.ifmo.optimization.algorithm.rmhc.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ru.ifmo.optimization.instance.Hashable;
import ru.ifmo.optimization.instance.InstanceGenerator;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.CanonicalFSMGenerator;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.multimaskefsm.RandomMultiMaskEfsmGenerator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.ChangeMeaningfulPredicatesMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.ChangeNumberOfActionsMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.CounterExampleMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.DestinationStateMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.FBDKAddDeleteTransitionMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.MaskMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.OldCounterExampleMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.SetFixedActionIdMutator;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class RmhcConfig<Instance extends Hashable> {
	
	public static enum MutatorType {
        CHANGE_DEST,
        CANONICAL_CHANGE_DEST,
        VERIFICATION_CHANGE_DEST,
        CHANGE_ACTIONS,
        CHANGE_NUMBER_OF_ACTIONS,
        CHANGE_EVENT,
        CHANGE_INITIAL_STATE,
        EFSM_ADD_DELETE_TRANSITIONS,
        TSAREV_EFSM_MUTATOR,
        BOUNDED_CHANGE_DEST,
        BOUNDED_CHANGE_ACTIONS,
        MULTIPLE,
        MULTIPLE_DEST,
        MULTIPLE_ACTIONS,
        LUCAS_REYNOLDS,
        //FBDK ECC
        FBDK_DESTINATION,
        FBDK_MASK,
        FBDK_ADD_DELETE_TRANSITIONS,
        FBDK_MAKE_VARIABLE_UNIMPORTANT,
        FBDK_COUNTEREXAMPLE,
        FBDK_OLD_COUNTEREXAMPLE,
        CHANGE_TRAN_GROUPS_ORDER,
        CHANGE_PREDICATES,
        FBDK_CHANGE_NUMBER_OF_ACTIONS,
        FBDK_SET_FIXED_ACTION_ID
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
	
	
	  public List<Mutator> getMutators(AbstractOptimizationTask<Instance> t) {
	        List<Mutator> mutators = new ArrayList<Mutator>();
	        for (MutatorType type : getMutatorTypes()) {
	            switch (type) {
	                case FBDK_DESTINATION:
	                    mutators.add(new DestinationStateMutator(Double.parseDouble(properties.getProperty("FBDK_DESTINATION.p", "-1"))));
	                    break;
	                case FBDK_MASK:
	                    mutators.add(new MaskMutator(Double.parseDouble(properties.getProperty("FBDK_MASK.p", "-1"))));
	                    break;
	                case FBDK_ADD_DELETE_TRANSITIONS:
	                    mutators.add(new FBDKAddDeleteTransitionMutator(Double.parseDouble(properties.getProperty("FBDK_ADD_DELETE_TRANSITIONS.p", "-1")), 
	                    		Double.parseDouble(properties.getProperty("add-delete-transition-probability", "0.05"))));
	                    break;
	                case CHANGE_PREDICATES:
	                    mutators.add(new ChangeMeaningfulPredicatesMutator(Double.parseDouble(properties.getProperty("CHANGE_PREDICATES.p", "-1"))));
	                    break;
	                case FBDK_COUNTEREXAMPLE:
	                    mutators.add(new CounterExampleMutator(Integer.parseInt(properties.getProperty("FBDK_COUNTEREXAMPLE.lambda")), 
	                    		Double.parseDouble(properties.getProperty("FBDK_COUNTEREXAMPLE.p"))));
	                    break;
	                case FBDK_OLD_COUNTEREXAMPLE:
	                    mutators.add(new OldCounterExampleMutator(Integer.parseInt(properties.getProperty("FBDK_OLD_COUNTEREXAMPLE.lambda"))));
	                    break;
	                    
	                case FBDK_CHANGE_NUMBER_OF_ACTIONS:
	                	mutators.add(new ChangeNumberOfActionsMutator(Double.parseDouble(properties.getProperty("FBDK_CHANGE_NUMBER_OF_ACTIONS.p", "-1"))));
	                	break;
	                case FBDK_SET_FIXED_ACTION_ID:
	                	mutators.add(new SetFixedActionIdMutator(Double.parseDouble(properties.getProperty("FBDK_SET_FIXED_ACTION_ID.p", "-1"))));
	                	break;
	            }
	        }
	        return mutators;
	    }
	  
	  public InstanceGenerator getInstanceGenerator() {
	        InstanceGeneratorType type;
	        try {
	            type = InstanceGeneratorType.valueOf(properties.getProperty("instance-generator"));
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }

	        switch (type) {
	            case PLAIN:
	                return new InitialFSMGenerator();
	            case CANONICAL:
	                return new CanonicalFSMGenerator();
	            case FBDK_ECC:
	                return new RandomMultiMaskEfsmGenerator();
	        }
	        return null;
	    }
	  
	  private static enum InstanceGeneratorType {
	        PLAIN,
	        CANONICAL,
	        FBDK_ECC
	    }
}
