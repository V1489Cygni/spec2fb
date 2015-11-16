package ru.ifmo.optimization.instance.fsm.task.testsmodelchecking;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import ru.ifmo.ctddev.genetic.transducer.algorithm.FST;
import ru.ifmo.ctddev.genetic.transducer.verifier.IVerifierFactory;
import ru.ifmo.ctddev.genetic.transducer.verifier.VerifierFactory;
import ru.ifmo.ltl.LtlParseException;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.LabellingTable;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.TestsWithLabelingTask;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import rwth.i2.ltl2ba4j.LTL2BA4J;

public class TestsModelCheckingTask extends TestsWithLabelingTask {

	protected final IVerifierFactory verifier;
	protected final List<String> formulas;
	protected double ltlCost;
	protected double testsCost;
	protected boolean useScenarios = true;
	protected boolean useFormulas;
	protected List<List<AutomatonTest>> testGroups = new ArrayList<List<AutomatonTest>>();
	protected List<Double> groupCost = new ArrayList<Double>();
	protected double minTestCost;
	
	public List<AutomatonTest> getTests() {
		return testGroups.get(0);
	}
	
	public List<Double> getGroupCost() {
		return groupCost;
	}
	
	public int getNumberOfTests() {
		return tests.length;
	}
	 
	private static List<String> loadFormulas(String path) {
		List<String> formulas = new ArrayList<String>();
		Scanner in = null;
		try {
			in = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (in.hasNext()) {
			formulas.add(in.nextLine());
		}
		in.close();
		
		return formulas;
	}
	
	public TestsModelCheckingTask(AbstractTaskConfig config) {
		super(config);
		
		ltlCost = 1;
		testsCost = 1;
		String[] setOfInputs = new String[events.size()];
		for (int i = 0; i < events.size(); i++) {
			setOfInputs[i] = events.get(i);
		}
		
		useFormulas = Boolean.parseBoolean(testsProperties.getProperty("use-formulas"));
		if (useFormulas) {
			verifier = new VerifierFactory(setOfInputs, actions);
			try {
				formulas = loadFormulas(testsProperties.getProperty("formulas"));
				verifier.prepareFormulas(formulas);
			} catch (LtlParseException e) {
				throw new RuntimeException(e);   
			}
		} else {
			verifier = null;
			formulas = new ArrayList<String>();
		}
		
		List<AutomatonTest> testList = new ArrayList<AutomatonTest>();
		for (AutomatonTest test : tests) {
			testList.add(test);
		}
		
		testGroups.add(testList);
		groupCost.add(testsCost);
	}
	
	@Override
	public FitInstance<FSM> getFitInstance(FSM fsm) {
		InstanceMetaData<FSM> instanceMD = getInstanceMetaData(fsm);
		return new FitInstance<FSM>(instanceMD.getInstance(), instanceMD.getFitness());
	}
	
	public double[] getTestFitness(FSM fsm) {
		FSM labeledFSM = labelFSM(fsm, useScenarios);
		double[] result = new double[tests.length];
		for (int i = 0; i < tests.length; i++) {
			result[i] = 1.0 - runFsmOnTest(labeledFSM, tests[i], true).getFitness();
		}
		return result;
	}
	
	
	protected FsmMetaData getTestsFF(FSM labeledFsm, List<AutomatonTest> testGroup, double groupCost) {
		Set<Transition> visitedTransitions = new HashSet<Transition>(labeledFsm.getNumberOfStates() * labeledFsm.getNumberOfEvents());
		double f1 = 0;
		int numberOfSuccesses = 0;
		for (AutomatonTest test : testGroup) {
			FsmMetaData fsmRunData = runFsmOnTest(labeledFsm, test, useScenarios);
			if (fsmRunData.getFitness() < 1e-10) {
				numberOfSuccesses++;
			}
			f1 += 1.0 - fsmRunData.getFitness();
			
			visitedTransitions.addAll(fsmRunData.getVisitedTransitions());
		}
		
		double fitness = groupCost;
        if (testGroup.size() > 0) {
            fitness = (numberOfSuccesses == testGroup.size()) ? groupCost : groupCost * (f1 / (double)testGroup.size());
        }

		return new FsmMetaData(labeledFsm, visitedTransitions, fitness);
	}
	
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM fsm) {
		numberOfFitnessEvaluations++;
		
		FSM labeled = labelFSM(fsm, useScenarios);
		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
		
		double testsFF = 0;
		for (int groupId = 0; groupId < testGroups.size(); groupId++) {
			FsmMetaData testsMd = getTestsFF(labeled, testGroups.get(groupId), groupCost.get(groupId));
			testsFF += testsMd.getFitness();
			visitedTransitions.addAll(testsMd.getVisitedTransitions());
			fsm.markUsedTransitions(visitedTransitions);
		}
		
        FST fst = FstFactory.createFST(labeled, actions);
        
		if (fst.getUsedTransitionsCount() == 0) {
			return new FsmMetaData(fsm, visitedTransitions, 0);
		}
		
		double ltlFF = ltlCost;
        if (formulas.size() > 0) {
        	verifier.configureStateMachine(fst);
    		int verificationResult[] = verifier.verify();
            ltlFF = ltlCost * verificationResult[0] / formulas.size() / fst.getUsedTransitionsCount();
            if ((ltlFF > ltlCost) || (ltlFF < 0)) {
                throw new RuntimeException(String.valueOf(ltlFF));
            }
        }
        
        boolean[][] transitionUsedInCounterexample = new boolean[fsm.getNumberOfStates()][fsm.getNumberOfEvents()];
        for (int i = 0; i < transitionUsedInCounterexample.length; i++) {
        	Arrays.fill(transitionUsedInCounterexample[i], false);
        }
        
        for (int state = 0; state < fsm.getNumberOfStates(); state++) {
        	for (int event = 0; event < fsm.getNumberOfEvents(); event++) {
        		ru.ifmo.ctddev.genetic.transducer.algorithm.Transition fstTransition = fst.getTransition(state, FSM.EVENTS.get(event));
        		if (fstTransition == null) {
        			continue;
        		}
        		if (fstTransition.isUsedByVerifier()) {
        			transitionUsedInCounterexample[state][event] = true;
        		} 
        	}
        }
        
        fsm.setTransitionsInCounterexample(transitionUsedInCounterexample);
        
        
        double fitness = testsFF + ltlFF;
        double transitionsFF = 0.0001 * (100 - fsm.getNumberOfTransitions()) * Collections.min(groupCost).doubleValue() / (double)tests.length;;
        if (fitness < 2) {
        	fitness = 0.75 * fitness + transitionsFF;
        } else {
        	fitness += transitionsFF;
        }
        return new FsmMetaData(fsm, visitedTransitions, fitness);
	}
	
	@Override
	public double correctFitness(double fitness, FSM cachedInstance, FSM trueInstance) {
		if (fitness < 1e-5) {
			return 0;
		}
		double negativeTerm = 0.0001 * (100 - cachedInstance.getNumberOfTransitions());
		double positiveTerm = 0.0001 * (100 - trueInstance.getNumberOfTransitions());
		return positiveTerm - negativeTerm;
	}
	
	protected FSM labelFSM(FSM fsm, boolean scenarios) {
		FSM result = new FSM(fsm);
		LabellingTable table = new LabellingTable(fsm.getNumberOfStates(), fsm.getNumberOfEvents());
		
		for (int i = 0; i < testGroups.size(); i++) {
			for (AutomatonTest test : testGroups.get(i)) {
				editLabellingTable(fsm, test, table, scenarios, groupCost.get(i));
			}
		}
		
		for (int i = 0; i < fsm.getNumberOfStates(); i++) {
			for (int j = 0; j < fsm.getNumberOfEvents(); j++) {
				result.transitions[i][j].setAction(table.getActions(i, j));
			}
		}
		return result;
	}
	
	private void editLabellingTable(FSM fsm, AutomatonTest test, LabellingTable table, boolean scenarios, double testWeight) {
		int currentState = fsm.getInitialState();
		int counter = 0;
		int i = 0; 
		for (String event : test.getInput()) {
			int eventIndex = events.indexOf(event);
			
			Transition t = fsm.transitions[currentState][eventIndex];
			if (t == null) {
				return;
			}
			if (t.getEndState() == -1) {
				return;
			}
			
			if (scenarios) {
				table.add(currentState, eventIndex, test.getOutput()[i], testWeight);
			} else {
				StringBuilder sequence = new StringBuilder();
				int numberOfActions = Integer.parseInt(t.getAction());
				for (int j = 0; j < numberOfActions; j++) {
					if (counter < test.getOutput().length) {
						sequence.append(test.getOutput()[counter++]);
					} else {
						sequence.append("??");
					}
				}
				table.add(currentState, eventIndex, sequence.toString(), testWeight);
			}
			currentState = t.getEndState();
			i++;
		}
	}
}
