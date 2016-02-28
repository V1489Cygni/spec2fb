package ru.ifmo.optimization.instance.multimaskefsm.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.comparator.MaxSingleObjectiveComparator;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsm;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskMetaData;
import ru.ifmo.optimization.instance.multimaskefsm.OutputAction;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Pair;
import ru.ifmo.util.StringUtils;

public class MultiMaskTask extends AbstractOptimizationTask<MultiMaskEfsmSkeleton> {
	
	private enum StringDistanceType {
		EQUALS,
		HAMMING,
		LEVEN
	}
	
	private enum LabelingType {
		TRIVIAL,
		MERGES
	}
	
	private enum AlgorithmGenerationScheme {
		FAVOR_X,
		FAVOR_CONCRETE
	}
	
    protected VarsActionsScenario[] scenarios;
    protected VarsActionsScenario[] mediumScenarios;
    protected VarsActionsScenario[] shortScenarios;
    protected double startPreciseFitnessCalculation;
    protected double startMediumPreciseFitnessCalculation;
    protected int shorteningScale;
    protected int mediumShorteningScale;
    protected int outputVariablesCount;
    protected int maxFirstErrorPosition = 0;
    protected List<OutputAction> precalculatedActions;
    private boolean usePrecalculatedActions;
    private StringDistanceType stringDistanceType;
    private LabelingType labelingType;
    private AlgorithmGenerationScheme algorithmGenerationScheme;
    protected double nStateChangesWeight;
    

    public MultiMaskTask(AbstractTaskConfig config) {
        desiredFitness = config.getDesiredFitness();
        comparator = new MaxSingleObjectiveComparator();
        scenarios = EccUtils.readScenarios(config.getProperty("scenarios"), Integer.parseInt(config.getProperty("cut-scenarios")));
        usePrecalculatedActions = Boolean.parseBoolean(config.getProperty("use-precalculated-actions"));
        stringDistanceType = StringDistanceType.valueOf(config.getProperty("string-distance-type"));
        labelingType = LabelingType.valueOf(config.getProperty("labeling-type"));
        algorithmGenerationScheme = AlgorithmGenerationScheme.valueOf(config.getProperty("algorithm-generation-scheme"));
        nStateChangesWeight = Double.parseDouble(config.getProperty("n-state-changes-weight"));
        
        EccUtils.readPredicateNames(config.getProperty("predicate-names"));
        shorteningScale = Integer.parseInt(config.getProperty("shortening-scale"));
        mediumShorteningScale = Integer.parseInt(config.getProperty("medium-shortening-scale"));
        startPreciseFitnessCalculation = Double.parseDouble(config.getProperty("start-precise-fitness-calculation"));
        startMediumPreciseFitnessCalculation = Double.parseDouble(config.getProperty("start-medium-precise-fitness-calculation"));
        shortScenarios = preprocessScenarios(shorteningScale, scenarios);
        mediumScenarios = preprocessScenarios(mediumShorteningScale, scenarios);
        MultiMaskEfsmSkeleton.STATE_COUNT = Integer.parseInt(config.getProperty("desired-number-of-states"));
        MultiMaskEfsmSkeleton.MEANINGFUL_PREDICATES_COUNT = Integer.parseInt(config.getProperty("meaningful-predicates-count"));
        MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT = Integer.parseInt(config.getProperty("transition-groups-count"));
        MultiMaskEfsmSkeleton.PREDICATE_COUNT = MultiMaskEfsmSkeleton.PREDICATE_NAMES.size();
        outputVariablesCount = scenarios[0].get(scenarios[0].size() - 1).getActions().get(0).getAlgorithm().length();
        
        if (usePrecalculatedActions) {
        	ExactEccGenerator generator = new ExactEccGenerator();
        	precalculatedActions = generator.calculateActions();
        }
    }
    
    public int getPrecalculatedActionsCount() {
    	if (precalculatedActions == null) {
    		return 0;
    	}
    	return precalculatedActions.size();
    }
    
    protected VarsActionsScenario[] removeLeadingPassiveElements(VarsActionsScenario[] scenarios) {
    	VarsActionsScenario[] result = new VarsActionsScenario[scenarios.length];
    	
    	for (int i = 0; i < scenarios.length; i++) {
    		List<ScenarioElement> elements = new ArrayList<ScenarioElement>();
    		elements.addAll(scenarios[i].getElements());
    		boolean changed = true;
    		loop: while (changed) {
    			for (int j = 0; j < elements.size() - 1; j++) {
    				ScenarioElement current = elements.get(j);
    				ScenarioElement next = elements.get(j + 1);
    				if (current.getInputEvent().equals(next.getInputEvent()) && current.getVariableValues().equals(next.getVariableValues())) {
    					if (current.getOutputEvent(0).isEmpty() && !next.getOutputEvent(0).isEmpty()) {
    						elements.remove(j);
    						changed = true;
    						continue loop;
    					}
    				}
    			}
    			changed = false;
    		}
    		result[i] = new VarsActionsScenario(elements);
    	}
    	
    	return result;
    }
    
    
    protected VarsActionsScenario[] removePassiveElements() {
    	VarsActionsScenario[] result = new VarsActionsScenario[scenarios.length];
    	
    	for (int i = 0; i < scenarios.length; i++) {
            List<ScenarioElement> processed = new ArrayList<ScenarioElement>();
            for (ScenarioElement e : scenarios[i].getElements()) {
            	if (!e.getActions().get(0).getOutputEvent().isEmpty()) {
            		processed.add(e);
            	}
            }
            result[i] = new VarsActionsScenario(processed);
    	}
    	
    	for (int i = 0; i < scenarios.length; i++) {
    		System.out.println(scenarios[i].size() + " " + result[i].size());
    	}
    	System.out.println();
    	return result;
    }
    
    protected Pair<List<ScenarioElement>, Integer> getEqualElements(VarsActionsScenario scenario, List<Integer> positions, int distance) {
    	if (positions.size() < 2) {
    		return null;
    	}
    	
    	for (int i = 1; i < distance; i++) {
    		ScenarioElement e = scenario.get(positions.get(0) + i);
    		for (int j = 1; j < positions.size(); j++) {
    			if (positions.get(j) + i >= scenario.size()) {
    				List<Integer> newPositions = new ArrayList<Integer>();
    				newPositions.addAll(positions);
    				newPositions.remove(newPositions.size() - 1);
    				return getEqualElements(scenario, newPositions, distance);
    			}
    			if (!scenario.get(positions.get(j) + i).equals(e)) {
    				List<Integer> newPositions = new ArrayList<Integer>();
    				newPositions.addAll(positions);
    				newPositions.remove(newPositions.size() - 1);
    				return getEqualElements(scenario, newPositions, distance);
    			}
    		}
    	}
    	
    	List<ScenarioElement> elements = new ArrayList<ScenarioElement>();
    	for (int i = 0; i < distance; i++) {
    		elements.add(scenario.get(positions.get(0) + i));
    	}
    	
    	return new Pair<List<ScenarioElement>, Integer>(elements, positions.size());
    }
    
    protected Pair<List<ScenarioElement>, Integer> getEqualElements(VarsActionsScenario scenario, int position) {
    	List<ScenarioElement> elements = new ArrayList<ScenarioElement>();
    	List<Integer> positions = new ArrayList<Integer>();
    	int distance = -1;
    	
    	ScenarioElement element = scenario.get(position);
    	elements.add(element);
    	positions.add(position);
    	
    	int last = position;
    	for (int i = position + 1; i < scenario.size(); i++) {
    		if (scenario.get(i).equals(element)) {
    			if (distance == -1) {
    				distance = i - last;    				
    				positions.add(i);
    			} else if (i - last != distance) {
    				break;
    			} else {
    				positions.add(i);
    			}
    			last = i;
    		}
    	}
    	
    	return getEqualElements(scenario, positions, distance);
    }

    protected VarsActionsScenario[] preprocessScenariosNew(int scale, VarsActionsScenario[] scenarios) {
        VarsActionsScenario[] result = new VarsActionsScenario[scenarios.length];

        for (int i = 0; i < scenarios.length; i++) {
            List<ScenarioElement> processed = new ArrayList<ScenarioElement>();
            int j = 0;

            while (j < scenarios[i].size()) {
                ScenarioElement currentElement = scenarios[i].get(j);
            	Pair<List<ScenarioElement>, Integer> sequence = getEqualElements(scenarios[i], j);
            	if (sequence == null) {
            		processed.add(currentElement);
            		j++;
            		continue;
            	}
            	for (int k = 0; k < Math.min(scale, sequence.second); k++) {
            		processed.addAll(sequence.first);
            	}
            	j += sequence.first.size() * sequence.second;
            }

            result[i] = new VarsActionsScenario(processed);
        }

        for (int i = 0; i < scenarios.length; i++) {
            System.out.println(scenarios[i].size() + " " + result[i].size());
        }
        System.out.println();

        return result;
    }

    
    
    protected VarsActionsScenario[] preprocessScenarios(int scale, VarsActionsScenario[] scenarios) {
        VarsActionsScenario[] result = new VarsActionsScenario[scenarios.length];

        for (int i = 0; i < scenarios.length; i++) {
            List<ScenarioElement> processed = new ArrayList<ScenarioElement>();
            int j = 0;
            ScenarioElement currentElement = scenarios[i].get(j++);
            int numberOfRepeats = 1;
            while (j < scenarios[i].size()) {
                if (scenarios[i].get(j).equals(currentElement)) {
                    j++;
                    numberOfRepeats++;
                } else {
                    for (int k = 0; k < Math.min(numberOfRepeats, scale); k++) {
                        processed.add(currentElement);
                    }
                    currentElement = scenarios[i].get(j);
                    numberOfRepeats = 1;
                    j++;
                }

                if (j == scenarios[i].size()) {
                    for (int k = 0; k < Math.min(numberOfRepeats, scale); k++) {
                        processed.add(currentElement);
                    }
                }
            }

            result[i] = new VarsActionsScenario(processed);
        }

        for (int i = 0; i < scenarios.length; i++) {
            System.out.println(scenarios[i].size() + " " + result[i].size());
        }
        System.out.println();

        return result;
    }

    protected OutputAction getBestMask(List<OutputAction[]> pairs) {
        if (pairs.isEmpty()) {
            return null;
        }

        //generate algorithm
        StringBuilder label = new StringBuilder();
        for (int i = 0; i < outputVariablesCount; i++) {
            int[][] map = new int[2][2];
            map[0][0] = 0;
            map[0][1] = 0;
            map[1][0] = 0;
            map[1][1] = 0;

            for (int j = 0; j < pairs.size(); j++) {
                map[Character.getNumericValue(pairs.get(j)[0].getAlgorithm().charAt(i))]
                        [Character.getNumericValue(pairs.get(j)[1].getAlgorithm().charAt(i))] += 1;
            }


            int setZero = 0;
            int setOne = 0;
            int leave = 0;
            switch (algorithmGenerationScheme) {
            case FAVOR_X: {
            	setZero = map[1][0];
            	setOne = map[0][1];
            	leave = map[0][0] + map[1][1];
            	break;
            }
            case FAVOR_CONCRETE: {
            	setZero = map[1][0] + map[0][0];
                setOne = map[0][1] + map[1][1];
                leave = map[0][0] + map[1][1];
                break;
            }
            }
            

            int max = setZero;
            char c = '0';
            if (setOne > max) {
                max = setOne;
                c = '1';
            }
            if (leave > max) {
                max = leave;
                c = 'x';
            }

            label.append(c);
        }

        //select output event
        Map<String, Integer> outputEventMap = new HashMap<String, Integer>();
        for (OutputAction[] p : pairs) {
            String outputEvent = p[1].getOutputEvent();
            if (!outputEventMap.containsKey(outputEvent)) {
                outputEventMap.put(outputEvent, 1);
            } else {
                outputEventMap.put(outputEvent, outputEventMap.get(outputEvent) + 1);
            }
        }
        int maxOccurences = 0;
        String bestOutputEvent = "";
        for (Entry<String, Integer> e : outputEventMap.entrySet()) {
            if (e.getValue() > maxOccurences) {
                maxOccurences = e.getValue();
                bestOutputEvent = e.getKey();
            }
        }

        return new OutputAction(label.toString(), bestOutputEvent);
    }
    
    protected OutputAction getBestMask2(List<Pair<OutputAction, OutputAction>> pairs) {
        if (pairs.isEmpty()) {
            return null;
        }

        //generate algorithm
        StringBuilder label = new StringBuilder();
        for (int i = 0; i < outputVariablesCount; i++) {
            int[][] map = new int[2][2];
            map[0][0] = 0;
            map[0][1] = 0;
            map[1][0] = 0;
            map[1][1] = 0;

            for (int j = 0; j < pairs.size(); j++) {
                map[Character.getNumericValue(pairs.get(j).first.getAlgorithm().charAt(i))]
                        [Character.getNumericValue(pairs.get(j).second.getAlgorithm().charAt(i))] += 1;
            }


            int setZero = 0;
            int setOne = 0;
            int leave = 0;
            switch (algorithmGenerationScheme) {
            case FAVOR_X: {
            	setZero = map[1][0];
            	setOne = map[0][1];
            	leave = map[0][0] + map[1][1];
            	break;
            }
            case FAVOR_CONCRETE: {
            	setZero = map[1][0] + map[0][0];
                setOne = map[0][1] + map[1][1];
                leave = map[0][0] + map[1][1];
                break;
            }
            }


            int max = setZero;
            char c = '0';
            if (setOne > max) {
                max = setOne;
                c = '1';
            }
            if (leave > max) {
                max = leave;
                c = 'x';
            }

            label.append(c);
        }

        //select output event
        Map<String, Integer> outputEventMap = new HashMap<String, Integer>();
        for (Pair<OutputAction, OutputAction> p : pairs) {
            String outputEvent = p.second.getOutputEvent();
            if (!outputEventMap.containsKey(outputEvent)) {
                outputEventMap.put(outputEvent, 1);
            } else {
                outputEventMap.put(outputEvent, outputEventMap.get(outputEvent) + 1);
            }
        }
        int maxOccurences = 0;
        String bestOutputEvent = "";
        for (Entry<String, Integer> e : outputEventMap.entrySet()) {
            if (e.getValue() > maxOccurences) {
                maxOccurences = e.getValue();
                bestOutputEvent = e.getKey();
            }
        }

        return new OutputAction(label.toString(), bestOutputEvent);
    }

    public MultiMaskEfsm label(MultiMaskEfsmSkeleton instance) {
        return label(instance, scenarios);
    }
    
    public MultiMaskEfsm label(MultiMaskEfsmSkeleton instance, VarsActionsScenario[] scenarios) {
    	switch (labelingType) {
    	case TRIVIAL:
    		return labelTrivial(instance, scenarios);
    	case MERGES:
    		return labelWithMerges(instance, scenarios);
    	default:
    		throw new RuntimeException();
    	}
    }

    public MultiMaskEfsm labelTrivial(MultiMaskEfsmSkeleton instance, VarsActionsScenario[] scenarios) {
        List<Pair<OutputAction, OutputAction>>[] table = new List[MultiMaskEfsmSkeleton.STATE_COUNT];

        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
            table[i] = new ArrayList<Pair<OutputAction, OutputAction>>();
        }

        for (VarsActionsScenario scenario : scenarios) {
            int currentState = instance.getInitialState();

            for (int i = 0; i < scenario.size(); i++) {
                int nextState = instance.getNewState(currentState, scenario.get(i).getInputEvent(), scenario.get(i).getVariableValues());
                if (nextState != -1) {
                    currentState = nextState;
                    if (i == 0) {
                        continue;
                    }

                    if (!scenario.get(i).getActions().get(0).equals(scenario.get(i - 1).getActions().get(0))) {
                        table[currentState].add(new Pair<OutputAction, OutputAction>(
                                new OutputAction(scenario.get(i - 1).getActions().get(0).getAlgorithm(), scenario.get(i - 1).getActions().get(0).getOutputEvent()),
                                new OutputAction(scenario.get(i).getActions().get(0).getAlgorithm(), scenario.get(i).getActions().get(0).getOutputEvent())));
                    }
                }
            }
        }

        MultiMaskEfsm labeled = new MultiMaskEfsm(instance);
        for (int state = 0; state < MultiMaskEfsmSkeleton.STATE_COUNT; state++) {
            OutputAction bestMask = getBestMask2(table[state]);
            labeled.addActions(state, bestMask == null ? new OutputAction(EccUtils.getActions('x', outputVariablesCount), "") : bestMask);
        }
        return labeled;
    }

    
    public MultiMaskEfsm labelMultipleActions(MultiMaskEfsmSkeleton instance, VarsActionsScenario[] scenarios) {
        List<Pair<OutputAction, OutputAction>>[][] table = new ArrayList[MultiMaskEfsmSkeleton.STATE_COUNT][];

        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
        	table[i] = new ArrayList[instance.getState(i).getNumberOfOutputActions()];
        	for (int j = 0; j < table[i].length; j++) {
        		table[i][j] = new ArrayList<Pair<OutputAction, OutputAction>>();
        	}
        }
        
        String zeros = EccUtils.getActions('0', outputVariablesCount);
        
        for (VarsActionsScenario scenario : scenarios) {
            int currentState = instance.getInitialState();

            if (table[currentState].length > 0) {
            	table[currentState][0].add(new Pair<OutputAction, OutputAction>(
            			new OutputAction(zeros, ""),
            			scenario.get(0).getActions().get(0)
            			));
            }
            
            for (int i = 1; i < scenario.size(); i++) {
                int nextState = instance.getNewState(currentState, scenario.get(i).getInputEvent(), scenario.get(i).getVariableValues());
                if (nextState != -1) {
                	currentState = nextState;

                	ScenarioElement previousElement = scenario.get(i - 1);
                	ScenarioElement thisElement = scenario.get(i);

                	OutputAction last = previousElement.getActions().get(previousElement.getActions().size() - 1);
                	for (int j = 0; j < thisElement.getActions().size(); j++) {
                		if (j >= instance.getState(currentState).getNumberOfOutputActions()) {
                			break;
                		}
                		table[currentState][j].add(new Pair<OutputAction, OutputAction>(
                				last,
                				thisElement.getActions().get(j)
                				));
                		last = thisElement.getActions().get(j); 
                	}
                }
            }
        }

        MultiMaskEfsm labeled = new MultiMaskEfsm(instance);
        for (int state = 0; state < MultiMaskEfsmSkeleton.STATE_COUNT; state++) {
        	if (instance.getState(state).getNumberOfOutputActions() > 0) {
        		for (int actionNum = 0; actionNum < instance.getState(state).getNumberOfOutputActions(); actionNum++) {
        			OutputAction bestMask = getBestMask2(table[state][actionNum]);
        			labeled.addActions(state, bestMask == null ? new OutputAction(EccUtils.getActions('x', outputVariablesCount), "") : bestMask);
        		}
        	} else {
        		labeled.addActions(state, new OutputAction(EccUtils.getActions('x', outputVariablesCount), ""));
        	}
        }
        return labeled;
    }
    
    
    
    private char getMinErrorChar(List<OutputAction[]> table, int i, OutputAction currentActions, OutputAction possibleAlgorithm) {
    	 int[][] map = new int[2][2];
         map[0][0] = 0;
         map[0][1] = 0;
         map[1][0] = 0;
         map[1][1] = 0;

         for (int j = 0; j < table.size(); j++) {
             map[Character.getNumericValue(table.get(j)[0].getAlgorithm().charAt(i))]
                     [Character.getNumericValue(table.get(j)[1].getAlgorithm().charAt(i))] += 1;
         }
         
         String newActions = applyMask(currentActions.getAlgorithm(), possibleAlgorithm.getAlgorithm());
         map[Character.getNumericValue(currentActions.getAlgorithm().charAt(i))]
                 [Character.getNumericValue(newActions.charAt(i))] += 1;
         

         int setZero = 0;
         int setOne = 0;
         int leave = 0;
         switch (algorithmGenerationScheme) {
         case FAVOR_X: {
         	setZero = map[1][0];
         	setOne = map[0][1];
         	leave = map[0][0] + map[1][1];
         	break;
         }
         case FAVOR_CONCRETE: {
         	setZero = map[1][0] + map[0][0];
             setOne = map[0][1] + map[1][1];
             leave = map[0][0] + map[1][1];
             break;
         }
         }

         int max = setZero;
         char c = '0';
         if (setOne > max) {
             max = setOne;
             c = '1';
         }
         if (leave > max) {
             max = leave;
             c = 'x';
         }

         return c;
    }
    
    private OutputAction getMergedAlgorithm(List<OutputAction[]> table, OutputAction currentActions, OutputAction possibleAlgorithm) {
    	StringBuilder currentAlgorithm = new StringBuilder();
    	
    	charloop: for (int i = 0; i < outputVariablesCount; i++) {
    		Set<Character> charSet = new HashSet<Character>();
    		charSet.add(possibleAlgorithm.getAlgorithm().charAt(i));
    		for (OutputAction[] algorithm : table) {
    			Character c = algorithm[2].getAlgorithm().charAt(i);
    			charSet.add(c);
    			if (charSet.size() > 1) {
    				currentAlgorithm.append(getMinErrorChar(table, i, currentActions, possibleAlgorithm));
    				continue charloop;
    			}
    		}
    		currentAlgorithm.append(charSet.iterator().next());
    	}
    	
    	
    	//select output event
        Map<String, Integer> outputEventMap = new HashMap<String, Integer>();
        for (OutputAction[] p : table) {
            String outputEvent = p[1].getOutputEvent();
            if (!outputEventMap.containsKey(outputEvent)) {
                outputEventMap.put(outputEvent, 1);
            } else {
                outputEventMap.put(outputEvent, outputEventMap.get(outputEvent) + 1);
            }
        }
        int maxOccurences = 0;
        String bestOutputEvent = "";
        for (Entry<String, Integer> e : outputEventMap.entrySet()) {
            if (e.getValue() > maxOccurences) {
                maxOccurences = e.getValue();
                bestOutputEvent = e.getKey();
            }
        }

        return new OutputAction(currentAlgorithm.toString(), bestOutputEvent);
    }
    
    public MultiMaskEfsm labelWithMerges(MultiMaskEfsmSkeleton instance, VarsActionsScenario[] scenarios) {
    	 List<OutputAction[]>[] table = new List[MultiMaskEfsmSkeleton.STATE_COUNT];
    	 OutputAction[] currentAlgorithm = new OutputAction[MultiMaskEfsmSkeleton.STATE_COUNT];

    	 for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
    		 table[i] = new ArrayList<OutputAction[]>();
    	 }

    	 for (VarsActionsScenario scenario : scenarios) {
    		 OutputAction currentActions = new OutputAction(EccUtils.getActions('0', outputVariablesCount), "");
    		 
    		 int currentState = instance.getInitialState();

    		 for (int i = 0; i < scenario.size(); i++) {
    			 int nextState = instance.getNewState(currentState, scenario.get(i).getInputEvent(), scenario.get(i).getVariableValues());
    			 if (nextState != -1) {
    				 if (i > 0) {
    					 currentState = nextState;
    				 }

    				 OutputAction possibleAlgorithm = ExactEccGenerator.getPossibleAlgorithm(currentActions, scenario.get(i).getAction(0));
    				 if (currentAlgorithm[currentState] == null) {
    					 currentAlgorithm[currentState] = possibleAlgorithm;
    					 OutputAction newActions = new OutputAction(applyMask(currentActions.getAlgorithm(), 
    							 currentAlgorithm[currentState].getAlgorithm()),
    							 currentAlgorithm[currentState].getOutputEvent());
        				 table[currentState].add(new OutputAction[]{currentActions, newActions, currentAlgorithm[currentState]});
        				 currentActions = newActions;
        				 continue;
    				 }
    				 
    				 currentAlgorithm[currentState] = getMergedAlgorithm(table[currentState], currentActions, possibleAlgorithm);
    				 OutputAction newActions = new OutputAction(applyMask(currentActions.getAlgorithm(), 
    						 currentAlgorithm[currentState].getAlgorithm()), 
    						 currentAlgorithm[currentState].getOutputEvent());
    				 table[currentState].add(new OutputAction[]{currentActions, newActions, currentAlgorithm[currentState]});
//    				 table[currentState].add(new OutputAction[]{currentActions, newActions, possibleAlgorithm});
    				 currentActions = newActions;
    			 }
    		 }
    	 }

    	 MultiMaskEfsm labeled = new MultiMaskEfsm(instance);
    	 for (int state = 0; state < MultiMaskEfsmSkeleton.STATE_COUNT; state++) {
    		 OutputAction bestMask = currentAlgorithm[state]; 
    		 labeled.addActions(state, bestMask == null ? new OutputAction(EccUtils.getActions('x', outputVariablesCount), "") : bestMask);
    	 }
    	 return labeled;	
    }
    
    
    protected RawRunData getRawRunData(MultiMaskEfsm instance, VarsActionsScenario scenario) {
        int currentState = instance.getInitialState();
        List<String> outputs = new ArrayList<String>();
        int numberOfStateChanges = 0;

        String currentActions = scenario.getActions(currentState).get(0).getAlgorithm();
        currentActions = applyMask(currentActions, instance.getActions(currentState).get(0).getAlgorithm());
        outputs.add(currentActions + (instance.getActions(currentState).get(0).getOutputEvent()));
        int firstErrorPosition = -1;
        for (int i = 1; i < scenario.size(); i++) {
            int nextState = instance.getNewState(currentState, scenario.get(i).getInputEvent(), scenario.get(i).getVariableValues());
            if (nextState != -1) {
                numberOfStateChanges++;
                currentState = nextState;
                currentActions = applyMask(currentActions, instance.getActions(currentState).get(0).getAlgorithm());
            }
            if (firstErrorPosition == -1) {
                if (!currentActions.equals(scenario.get(i).getActions().get(0).getAlgorithm())) {
                    firstErrorPosition = i;
                }
                if (nextState != -1 && !instance.getActions(currentState).get(0).getOutputEvent().equals(scenario.get(i).getActions().get(0).getOutputEvent())) {
                    firstErrorPosition = i;
                }
            }
            outputs.add(currentActions + ((nextState != -1) ? instance.getActions(currentState).get(0).getOutputEvent() : ""));
        }

        return new RawRunData(outputs.toArray(new String[0]), numberOfStateChanges, firstErrorPosition);
    }

    
    public RunData runScenario2(MultiMaskEfsm instance, VarsActionsScenario scenario) {
        RawRunData rawRunData = getRawRunData(instance, scenario);
        String[] scenarioOutputs = scenario.getOutputs();

        double f1 = StringUtils.levenshteinDistance(scenarioOutputs, rawRunData.outputs) / Math.max(scenarioOutputs.length, rawRunData.outputs.length);
        return new RunData(Math.max(scenarioOutputs.length, rawRunData.outputs.length) == 0
                ? 1.0
                : f1, (double) rawRunData.numberOfStateChanges / (double) scenario.size(),
                rawRunData.firstErrorPosition == -1 ? 1.0 : (double) rawRunData.firstErrorPosition / (double) (scenario.size() - 1));
    }

    
    private double stringDistance(String first, String second) {
    	switch (stringDistanceType) {
    	case EQUALS:
    		return first.equals(second) ? 0 : 1;
    	case HAMMING:
    		return StringUtils.hammingDistance(first, second);
    	case LEVEN:
    		return StringUtils.levenshteinDistance(first, second);
    	default:
    		throw new RuntimeException();
    	}
    }
    
    public RunData runScenario(MultiMaskEfsm instance, VarsActionsScenario scenario) {
    	 int currentState = instance.getInitialState();
         int numberOfStateChanges = 0;
         double actionsError = 0;
         double eventsError = 0;
         List<String> outputs = new ArrayList<String>();
         int nerrors = 0;

         int firstErrorPosition = -1;
         String currentActions = EccUtils.getActions('0', outputVariablesCount);
         for (int j = 0; j < instance.getActionsCount(currentState); j++) {
        	 OutputAction a = instance.getActions(currentState).get(j);
        	 currentActions = applyMask(currentActions, a.getAlgorithm());
        	 outputs.add(a.getOutputEvent());
        	 if (j < scenario.get(0).getActions().size()) {
        		 double error = stringDistance(scenario.get(0).getAlgorithm(j), currentActions) / outputVariablesCount;
        		 actionsError += error;
        		 if (error > 1e-10) {
        			 nerrors++;
        		 }
        		 if (!a.getOutputEvent().equals(scenario.get(0).getOutputEvent(j))) {
        			 eventsError++;
        		 }        		 
        	 } else {
        		 double error = stringDistance(scenario.get(0).getLastAlgorithm(), currentActions) / outputVariablesCount;
        		 actionsError += error;
        		 
        		 if (error > 1e-10) {
        			 nerrors++;
        		 }
        		 if (!a.getOutputEvent().isEmpty()) {
        			 eventsError++;
        		 }
        	 }
         }
         
         if (instance.getActionsCount(currentState) < scenario.get(0).getActionsCount()) {
        	 for (int j = instance.getActionsCount(currentState); j < scenario.get(0).getActionsCount(); j++) {
        		 double error = stringDistance(scenario.get(0).getAlgorithm(j), currentActions) / outputVariablesCount;
        		 actionsError += error;
        		 if (error > 1e-10) {
        			 nerrors++;
        		 }
        		 if (!scenario.get(0).getOutputEvent(j).isEmpty()) {
        			 eventsError++;
        		 }
        	 }
         }
         
         if (actionsError > 1e-10) {// || eventsError > 1e-10) { 
        	 firstErrorPosition = 0;
         } 
         
         
         for (int i = 1; i < scenario.size(); i++) {
    		 ScenarioElement e = scenario.get(i);
        	 int nextState = instance.getNewState(currentState, scenario.get(i).getInputEvent(), scenario.get(i).getVariableValues());
        	 if (nextState != -1) {
        		 numberOfStateChanges++;
        		 currentState = nextState;
        		 
        		 //number of actions in state >= number of actions in scenario element
        		 for (int j = 0; j < instance.getActionsCount(currentState); j++) {
        			 OutputAction a = instance.getActions(currentState).get(j);
        			 currentActions = applyMask(currentActions, a.getAlgorithm());
        			 outputs.add(a.getOutputEvent());
        			 if (j < e.getActionsCount()) {
        				 double error = stringDistance(e.getAlgorithm(j), currentActions) / outputVariablesCount;
        				 actionsError += error;
        				 
        				 if (error > 1e-10) {
        					 nerrors++;
        				 }
        				 
        				 if (!a.getOutputEvent().equals(e.getOutputEvent(j))) {
        					 eventsError++;
        				 }        				 
        			 } else {
        				 //if number of actions in state > number of actions in scenario element
        				 double error = stringDistance(e.getLastAlgorithm(), currentActions) / outputVariablesCount;
        				 actionsError += error;
        				 
        				 if (error > 1e-10) {
        					 nerrors++;
        				 }
        				 
        				 if (!a.getOutputEvent().isEmpty()) {
        					 eventsError++;
        				 }
        			 }
        		 }    

        		 //number of actions in state < number of actions in scenario element
        		 if (instance.getActionsCount(currentState) < e.getActionsCount()) {
        			 for (int j = instance.getActionsCount(currentState); j < e.getActionsCount(); j++) {
        				 double error = stringDistance(e.getAlgorithm(j), currentActions) / outputVariablesCount;
        				 actionsError += error;
        				 
        				 if (error > 1e-10) {
        					 nerrors++;
        				 }

        				 if (!e.getOutputEvent(j).isEmpty()) {
        					 eventsError++;
        				 }
        			 }
        		 }
        	 } else {
        		 outputs.add("");
        		 for (int j = 0; j < e.getActionsCount(); j++) {
        			 double error = stringDistance(e.getAlgorithm(j), currentActions) / outputVariablesCount;
        			 actionsError += error;
        			 
        			 if (error > 1e-10) {
        				 nerrors++;
        			 }
        			 
        			 if (!e.getOutputEvent(j).isEmpty()) { 
        				 eventsError++;
        			 }
        		 }
        	 }
        	 
        	 if (firstErrorPosition == -1) {
    			 if (actionsError > 1e-10) {// || eventsError > 1e-10) {
    				 firstErrorPosition = i;
    			 }
        	 }
         }


         if (firstErrorPosition > maxFirstErrorPosition) {
         	maxFirstErrorPosition = firstErrorPosition;
         	System.out.println("Max first error position = " + firstErrorPosition);
         }
         
        
//         double f1 = 0.99 * (double)actionsError / Math.max(nerrors, scenario.getOutputCount()) + 0.01 * eventsError / Math.max(eventsError, scenario.getOutputCount());
         double f1 = (double)actionsError / Math.max(nerrors, scenario.getOutputCount());
         
         return new RunData(f1, 
        		 (double) numberOfStateChanges / (double) scenario.size(), 
        		 firstErrorPosition == -1 ? 1.0 : firstErrorPosition / (double) (scenario.size() - 1));
    }

    protected String applyMask(String currentActions, String mask) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < currentActions.length(); i++) {
            result.append(mask.charAt(i) == 'x' ? currentActions.charAt(i) : mask.charAt(i));
        }
        return result.toString();
    }
    
    protected String applyMask(String currentActions, List<OutputAction> outputActions) {
        StringBuilder result = new StringBuilder();
        String actions = currentActions;
        
        for (OutputAction oa : outputActions) {
        	String mask = oa.getAlgorithm();
        	for (int i = 0; i < currentActions.length(); i++) {
        		result.append(mask.charAt(i) == 'x' ? actions.charAt(i) : mask.charAt(i));
        	}
        	actions = result.toString();
        	result = new StringBuilder();
        }
        return actions;
    }

    protected RunData getF(MultiMaskEfsm labeledInstance, VarsActionsScenario[] s, double fitnessMultiplier) {
        labeledInstance.markTransitionsUnused();

        double f = 0;
        double c = 0;
        double e = 0;
        for (VarsActionsScenario scenario : s) {
            RunData runData = runScenario(labeledInstance, scenario);
            f += 1.0 - runData.fitness;
            c += 1.0 - runData.numberOfStateChanges;
            e += runData.firstErrorPosition;
        }

        int[] minOutputActionCount = new int[MultiMaskEfsmSkeleton.STATE_COUNT];
        int[] maxOutputActionCount = new int[MultiMaskEfsmSkeleton.STATE_COUNT];
    	Arrays.fill(maxOutputActionCount, 0);
    	Arrays.fill(minOutputActionCount, Integer.MAX_VALUE);
        
        for (VarsActionsScenario scenario : scenarios) {
            int currentState = labeledInstance.getInitialState();

            for (int i = 0; i < scenario.size(); i++) {
                int nextState = labeledInstance.getNewState(currentState, scenario.get(i).getInputEvent(), scenario.get(i).getVariableValues());
                if (nextState != -1) {
                    currentState = nextState;
                    maxOutputActionCount[currentState] = Math.max(maxOutputActionCount[currentState], scenario.get(i).getActions().size());
                    minOutputActionCount[currentState] = Math.min(minOutputActionCount[currentState], scenario.get(i).getActions().size());
                }
            }
        }
        
        double outputActionCountError = 0;
        double visitedStatesCount = 0;
        for (int i = 0; i < maxOutputActionCount.length; i++) {
        	if (maxOutputActionCount[i] == 0) {
        		continue;
        	}
        	visitedStatesCount++;
        	outputActionCountError += (double)(maxOutputActionCount[i] - minOutputActionCount[i]) / maxOutputActionCount[i];
        }
        outputActionCountError = 1.0 - outputActionCountError / visitedStatesCount;

        f /= (double) s.length;
        c /= (double) s.length;
        e /= (double) s.length;

        if (f > 1)  {
            throw new RuntimeException("Scenario fitness = " + f + " > 1");
        }
        

        f = 0.9 * f + 0.1 * e;
        
        return new RunData(f * fitnessMultiplier, c, e);
    }

    @Override
    public FitInstance<MultiMaskEfsmSkeleton> getFitInstance(MultiMaskEfsmSkeleton instance) {
    	numberOfFitnessEvaluations++;
        //first, try with short scenarios
        MultiMaskEfsm labeledInstance = label(instance, shortScenarios);
        
        RunData f = getF(labeledInstance, shortScenarios, 0.3);

        //if the fitness value is large enough, try with medium scenarios
        if (f.fitness >= startMediumPreciseFitnessCalculation * 0.3) {
            labeledInstance = label(instance, mediumScenarios);
            f = getF(labeledInstance, mediumScenarios, 0.25);
            f.fitness  = 0.35 + f.fitness;

            //if the fitness value is large enough, try with full scenarios
            if (f.fitness >= startPreciseFitnessCalculation * 0.6) {
                labeledInstance = label(instance, scenarios);
                f = getF(labeledInstance, scenarios, 0.35);
                f.fitness = 0.65 + f.fitness;
            }
        }

        if (f.fitness >= 1.0) {
            f.fitness = 1.1;
            return new FitInstance<MultiMaskEfsmSkeleton>(instance, f.fitness);
        }

        f.fitness += nStateChangesWeight * f.numberOfStateChanges;

        return new FitInstance<MultiMaskEfsmSkeleton>(instance, f.fitness);
    }


    public FitInstance<MultiMaskEfsmSkeleton> getFitInstance(MultiMaskEfsm instance) {
    	numberOfFitnessEvaluations++;
        //first, try with short scenarios
        RunData f = getF(instance, shortScenarios, 0.3);

        //if the fitness value is large enough, try with medium scenarios
        if (f.fitness > startMediumPreciseFitnessCalculation * 0.3) {
            f = getF(instance, mediumScenarios, 0.25);
            f.firstErrorPosition = 0.35 + f.fitness;

            //if the fitness value is large enough, try with full scenarios
            if (f.fitness > startPreciseFitnessCalculation * 0.6) {
                f = getF(instance, scenarios, 0.35);
                f.fitness = 0.65 + f.fitness;
            }
        }

        if (f.fitness >= 1.0) {
            f.fitness = 1.1;
            return new FitInstance<MultiMaskEfsmSkeleton>(instance.getSkeleton(), f.fitness);
        }

        f.fitness += nStateChangesWeight * f.numberOfStateChanges;

        return new FitInstance<MultiMaskEfsmSkeleton>(instance.getSkeleton(), f.fitness);
    }

    public double getFitness(MultiMaskEfsm labeledInstance) {
        RunData f = getF(labeledInstance, scenarios, 1.0);

        if (f.fitness >= 1.0) {
            f.fitness = 1.1;
            return 1.1;
        }

        return f.fitness;
    }

    @Override
    public InstanceMetaData<MultiMaskEfsmSkeleton> getInstanceMetaData(
            MultiMaskEfsmSkeleton instance) {
        return new MultiMaskMetaData(getFitInstance(instance), label(instance, scenarios));
    }

    public InstanceMetaData<MultiMaskEfsmSkeleton> getInstanceMetaData(
            MultiMaskEfsm instance) {
        return new MultiMaskMetaData(getFitInstance(instance), instance);
    }

    @Override
    public double correctFitness(double fitness, MultiMaskEfsmSkeleton cachedInstance, MultiMaskEfsmSkeleton trueInstance) {
        return 0;
    }

    @Override
    public Comparator<Double> getComparator() {
        return comparator;
    }

    @Override
    public int getNeighborhoodSize() {
        return Integer.MAX_VALUE;
    }

    public class RunData {
        protected double fitness;
        protected double numberOfStateChanges;
        protected double firstErrorPosition;

        public RunData(double fitness, double numberOfStateChanges, double firstErrorPosition) {
            this.fitness = fitness;
            this.numberOfStateChanges = numberOfStateChanges;
            this.firstErrorPosition = firstErrorPosition;
        }

        public double getFitness() {
            return fitness;
        }
    }

    protected class RawRunData {
        protected String[] outputs;
        protected double numberOfStateChanges;
        protected double firstErrorPosition;

        public RawRunData(String[] outputs, double numberOfStateChanges, double firstErrorPosition) {
            this.outputs = outputs;
            this.numberOfStateChanges = numberOfStateChanges;
            this.firstErrorPosition = firstErrorPosition;
        }
    }

    protected class TraceElement {
        protected String inputEvent;
        protected String inputVariables;
        protected String outputEvent;
        protected String outputVariables;

        public TraceElement(String inputEvent, String inputVariables, String outputEvent, String outputVariables) {
            this.inputEvent = inputEvent;
            this.inputVariables = inputVariables;
            this.outputEvent = outputEvent;
            this.outputVariables = outputVariables;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("in=");
            sb.append(inputEvent);
            sb.append("[");
            sb.append(inputVariables);
            sb.append("]; out=");
            sb.append(outputEvent);
            sb.append("[");
            sb.append(outputVariables);
            sb.append("];");
            return sb.toString();
        }
    }
}
