package ru.ifmo.optimization.instance.fsm.task.testsmodelchecking;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ifmo.ctddev.genetic.transducer.algorithm.FST;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.util.NodePair;
import structures.Node;
import structures.ScenariosTree;
import algorithms.AdjacentCalculator;
import bool.MyBooleanExpression;


public class TestsModelCheckingTaskWithConsistencyGraph extends TestsModelCheckingTask {

	private Map<Node, Set<Node>> consistencyGraph;
	private boolean[][] consistencyGraphBoolean;
	private boolean[] hasInconsistency;
	private ScenariosTree scenarioTree;
	private Map<String, String> efPairToEvent = new HashMap<String, String>();
	private Map<String, MyBooleanExpression> efPairToBooleanExpression = new HashMap<String, MyBooleanExpression>();
	private Map<String, Integer> eventsMap = new HashMap<String, Integer>();
	private PrintWriter out;
	private int c = 0, d = 0;
	
	public TestsModelCheckingTaskWithConsistencyGraph(AbstractTaskConfig config) {
		super(config);
		FSM.setEvents(events);
		
		for (int i = 0; i < events.size(); i++) {
			eventsMap.put(events.get(i), i);
		}
		
		scenarioTree = new ScenariosTree();
		try {
			scenarioTree.load("sc");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		consistencyGraph = AdjacentCalculator.getAdjacent(scenarioTree);
		
		consistencyGraphBoolean = new boolean[consistencyGraph.size()][consistencyGraph.size()];
		hasInconsistency = new boolean[consistencyGraph.size()];

		Arrays.fill(hasInconsistency, false);
		for (int i = 0; i < consistencyGraph.size(); i++) {
			Arrays.fill(consistencyGraphBoolean[i], false);
		}
		
		
		for (Node n1 : consistencyGraph.keySet()) {
			for (Node n2 : consistencyGraph.get(n1)) {
				consistencyGraphBoolean[n1.getNumber()][n2.getNumber()] = true;
				consistencyGraphBoolean[n2.getNumber()][n1.getNumber()] = true;
			}
		}
		
		for (int i = 0; i < consistencyGraph.size(); i++) {
			for (int j = 0; j < consistencyGraph.size(); j++) {
				if (consistencyGraphBoolean[i][j]) {
					hasInconsistency[i] = true;
					break;
				}
			}
		}
		
		for (AutomatonTest test : tests) {
			for (String s : test.getInput()) {
				String[] eventGuard = s.split("\\[");
				String event = eventGuard[0].trim();
				efPairToEvent.put(s, event);
				
				MyBooleanExpression guard = null;

				try {
					if (eventGuard.length > 1) {
						guard = MyBooleanExpression.get(eventGuard[1].replace("]", "").trim());
					} else {
						guard = MyBooleanExpression.get("1");
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				efPairToBooleanExpression.put(s, guard);
			}
		}
		
//		try {
//			out = new PrintWriter(new File("tests-consistency-scatter"));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
	}
	
//	@Override
//	public InstanceMetaData<FSM> getInstanceMetaData(FSM fsm) {
//		numberOfFitnessEvaluations++;
//		
//		FSM labelled = labelFSM(fsm, useScenarios);
//		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
//				
//		Set<Node>[] visits = new Set[fsm.getNumberOfStates()];
//		for (int state = 0; state < visits.length; state++) {
//			visits[state] = new HashSet<Node>();
//		}
//		
//		for (AutomatonTest test : tests) {
//			Node node = scenarioTree.getRoot();
//			int currentState = fsm.getInitialState();
//			
//			List<String> answers = new ArrayList<String>();
//			for (int i = 0; i < test.getInput().length; i++) {
//				String efPair = test.getInput()[i];
//				int eventIndex = events.indexOf(efPair);
//				String answer = labelled.transitions[currentState][eventIndex].getAction();
//				int nextState = labelled.transitions[currentState][eventIndex].getEndState();
//				if (nextState == -1) {
//					break;
//				}
//				visits[currentState].add(node);
//				String event = efPairToEvent.get(efPair);
//				MyBooleanExpression guard = efPairToBooleanExpression.get(efPair);
//
//				visitedTransitions.add(new Transition(currentState, nextState, efPair, answer));
//				currentState = nextState;
//				answers.add(answer);
//				
//				if (node.hasTransition(event, guard)) {
//					node = node.getTransition(event, guard).getDst();
//				}
//			}
//		}
//		
//		fsm.markUsedTransitions(visitedTransitions);
//		
//        FST fst = FstFactory.createFST(labelled, actions);
//		
//		if (fst.getUsedTransitionsCount() == 0) {
//			return new FsmMetaData(fsm, visitedTransitions, 0);
//		}
//		
//		double ltlFF = ltlCost;
//        if (formulas.size() > 0) {
//        	verifier.configureStateMachine(fst);
//    		int verificationResult[] = verifier.verify();
//            ltlFF = ltlCost * verificationResult[0] / formulas.size() / fst.getUsedTransitionsCount();
//            if ((ltlFF > ltlCost) || (ltlFF < 0)) {
//                throw new RuntimeException(String.valueOf(ltlFF));
//            }
//        }
//        
//        boolean[][] transitionUsedInCounterexample = new boolean[fsm.getNumberOfStates()][fsm.getNumberOfEvents()];
//        for (int i = 0; i < transitionUsedInCounterexample.length; i++) {
//        	Arrays.fill(transitionUsedInCounterexample[i], false);
//        }
//        
//        for (int state = 0; state < fsm.getNumberOfStates(); state++) {
//        	for (int event = 0; event < fsm.getNumberOfEvents(); event++) {
//        		ru.ifmo.ctddev.genetic.transducer.algorithm.Transition fstTransition = fst.getTransition(state, FSM.EVENTS.get(event));
//        		if (fstTransition == null) {
//        			continue;
//        		}
//        		if (fstTransition.isUsedByVerifier()) {
//        			transitionUsedInCounterexample[state][event] = true;
//        		} 
//        	}
//        }
//        
//        fsm.setTransitionsInCounterexample(transitionUsedInCounterexample);
//        
//        double consistencyFF = 0;
//        for (int state = 0; state < visits.length; state++) {
//        	if (visits[state].isEmpty()) {
//        		continue;
//        	}
//        	Set<NodePair> bad = new HashSet<NodePair>();
//        	Set<Node> set = visits[state];
//
//        	for (Node n1 : set) {
//        		if (!hasInconsistency[n1.getNumber()]) {
//        			continue;
//        		}
//        		for (Node n2 : set) {
//        			if (n2 == n1) {
//        				continue;
//        			}
//        			if (consistencyGraphBoolean[n1.getNumber()][n2.getNumber()]) {
//        				NodePair badPair = new NodePair(n1, n2);
//        				if (!bad.contains(badPair.getComplimentaryPair())) {
//        					bad.add(badPair);
//        				}
//        			}
//        		}
//        	}
//
//        	consistencyFF += bad.size() / Math.pow((double)visits[state].size(), 2);
//        }
//        consistencyFF /= fsm.getNumberOfStates();
//        consistencyFF = 1.0 - consistencyFF;
//        if (consistencyFF > 1.0 || consistencyFF < 0) {
//        	throw new RuntimeException("Consistency metric out of range: " + consistencyFF);
//        }
//        
//        double f1 = 0;
//		int numberOfSuccesses = 0;
//        if (consistencyFF >= 1.0) {
//        	for (AutomatonTest test : tests) {
//        		int currentState = fsm.getInitialState();
//
//        		List<String> answers = new ArrayList<String>();
//        		for (int i = 0; i < test.getInput().length; i++) {
//        			String efPair = test.getInput()[i];
//        			int eventIndex = events.indexOf(efPair);
//        			String answer = labelled.transitions[currentState][eventIndex].getAction();
//        			int nextState = labelled.transitions[currentState][eventIndex].getEndState();
//        			if (nextState == -1) {
//        				break;
//        			}
//
//        			currentState = nextState;
//        			answers.add(answer);
//
//        		}
//        		String answerArray[] = answers.toArray(new String[0]);
//
//        		double f = Math.max(test.getOutput().length, answerArray.length) == 0 
//        				? 1.0
//        				: test.getLevenshteinDistance(answerArray) / Math.max(test.getOutput().length, answerArray.length);
//
//        		if (f < 1e-5) {
//        			numberOfSuccesses++;
//        		}
//        		f1 += 1.0 - f;
//        	}
////        	d++;
//        } else {
////        	c++;
////        	System.out.println(c + " " + d);
//        }
//		
//        
//		double testsFF = testsCost;
//        if (tests.length > 0) {
//            testsFF = (numberOfSuccesses == tests.length) ? testsCost : testsCost * (f1 / tests.length);
//        }
//        
//        if (testsFF > 1.0 || consistencyFF > 1.0) {
//        	throw new RuntimeException();
//        }
//        
//        double fitness = testsFF + ltlFF + consistencyFF + 0.0001 * (100 - fsm.getNumberOfTransitions());
//        
////        if (fitness < 3.0) { 
////        	out.println(testsFF + " " + consistencyFF);
////        } else {
////        	out.close();
////        }
//        
//        return new FsmMetaData(fsm, visitedTransitions, fitness);
//	}

	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM fsm) {
		numberOfFitnessEvaluations++;

		FSM labelled = labelFSM(fsm, useScenarios);
		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
		double f1 = 0;
		int numberOfSuccesses = 0;

		Set<Node>[] visits = new Set[fsm.getNumberOfStates()];
		for (int state = 0; state < visits.length; state++) {
			visits[state] = new HashSet<Node>();
		}

		for (AutomatonTest test : tests) {
			Node node = scenarioTree.getRoot();
			int currentState = fsm.getInitialState();

			List<String> answers = new ArrayList<String>();
			for (int i = 0; i < test.getInput().length; i++) {
				String efPair = test.getInput()[i];
				int eventIndex = events.indexOf(efPair);
				String answer = labelled.transitions[currentState][eventIndex].getAction();
				int nextState = labelled.transitions[currentState][eventIndex].getEndState();
				if (nextState == -1) {
					break;
				}
				visits[currentState].add(node);
				String event = efPairToEvent.get(efPair);
				MyBooleanExpression guard = efPairToBooleanExpression.get(efPair);

				visitedTransitions.add(new Transition(currentState, nextState, efPair, answer));
				currentState = nextState;
				answers.add(answer);

				if (node.hasTransition(event, guard)) {
					node = node.getTransition(event, guard).getDst();
				}
			}
			String answerArray[] = answers.toArray(new String[0]);

			double f = Math.max(test.getOutput().length, answerArray.length) == 0 
					? 1.0
							: test.getLevenshteinDistance(answerArray) / Math.max(test.getOutput().length, answerArray.length);

			if (f < 1e-5) {
				numberOfSuccesses++;
			}
			f1 += 1.0 - f;
		}

		fsm.markUsedTransitions(visitedTransitions);

		double testsFF = testsCost;
		if (tests.length > 0) {
			testsFF = (numberOfSuccesses == tests.length) ? testsCost : testsCost * (f1 / tests.length);
		}

		FST fst = FstFactory.createFST(labelled, actions);

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

		double consistencyFF = 1.0;
		if (testsFF < 1.0) {
			consistencyFF = 0;
			for (int state = 0; state < visits.length; state++) {
				if (visits[state].isEmpty()) {
					continue;
				}
				Set<NodePair> bad = new HashSet<NodePair>();
				Set<Node> set = visits[state];

				for (Node n1 : set) {
					if (!hasInconsistency[n1.getNumber()]) {
						continue;
					}
					for (Node n2 : set) {
						if (n2 == n1) {
							continue;
						}
						if (consistencyGraphBoolean[n1.getNumber()][n2.getNumber()]) {
							NodePair badPair = new NodePair(n1, n2);
							if (!bad.contains(badPair.getComplimentaryPair())) {
								bad.add(badPair);
							}
						}
					}
				}

				consistencyFF += bad.size() / Math.pow((double)visits[state].size(), 2);
			}
			consistencyFF /= fsm.getNumberOfStates();
			if (consistencyFF > 1.0 || consistencyFF < 0) {
				throw new RuntimeException("Consistency metric out of range: " + consistencyFF);
			}
			consistencyFF = 1.0 - consistencyFF;
		}

		double fitness = testsFF + ltlFF + consistencyFF;
		
		double transitionsFF = 0.0001 * (100 - fsm.getNumberOfTransitions()) / (double)tests.length;;
        if (fitness < 3) {
        	fitness = 0.75 * fitness + transitionsFF;
        } else {
        	fitness += transitionsFF;
        }
        
		return new FsmMetaData(fsm, visitedTransitions, fitness);
	}
}
