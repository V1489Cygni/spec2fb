package ru.ifmo.optimization.instance.multimaskefsm.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.OutputAction;
import ru.ifmo.util.StringUtils;

public class EccUtils {
	public static VarsActionsScenario[] readScenarios(String scenariosFile, int cutScenarios) {
		Scanner in = null;
		try {
			in = new Scanner(new File(scenariosFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		VarsActionsScenario[] scenarios = new VarsActionsScenario[in.nextInt()];
		in.nextLine();
		int scenarioIndex = 0;	
		
		MultiMaskEfsmSkeleton.INPUT_EVENTS = new HashMap<String, Integer>();
		int maxOutputActionCount = 0;
		
		while (in.hasNext()) {
			String[] io = in.nextLine().split(";"); 

			scenarios[scenarioIndex] = new VarsActionsScenario();
			
			int i = 0;
			
			//TODO replace
			String input = null;
			String inputEvent = "";
			String output = "";
			List<OutputAction> outputActions = new ArrayList<OutputAction>();
			
			while (i < io.length) {
				if (i > cutScenarios) {
					break;
				}
				
				if (io[i].contains("in=")) {
					if (input != null) {
						if (outputActions.isEmpty()) {
							outputActions.add(new OutputAction(output, ""));
						} 
						if (outputActions.size() > maxOutputActionCount) {
							maxOutputActionCount = outputActions.size();
						}
						scenarios[scenarioIndex].add(new ScenarioElement(inputEvent, input, outputActions));
					}
						
					outputActions.clear();
					input = io[i].replace("in=", "").trim();
					inputEvent = input.substring(0, input.indexOf("["));
					input = input.substring(input.indexOf("[") + 1 , input.indexOf("]"));
					if (MultiMaskEfsmSkeleton.PREDICATE_COUNT == -1) {
						MultiMaskEfsmSkeleton.PREDICATE_COUNT = input.length();
					}
					
					if (!MultiMaskEfsmSkeleton.INPUT_EVENTS.containsKey(inputEvent)) {
						MultiMaskEfsmSkeleton.INPUT_EVENTS.put(inputEvent, MultiMaskEfsmSkeleton.INPUT_EVENTS.size());
					}
					i++;
				} else if (io[i].contains("out=")) {
					int j = i;
					while (io[j].contains("out=")) {
						output = io[j].replace("out=", "").trim();
						String outputEvent = output.substring(0, output.indexOf("["));
						output = output.substring(output.indexOf("[") + 1, output.indexOf("]"));
						outputActions.add(new OutputAction(output, outputEvent));
						j++;
					}
					i += j - i;
				} else {
					i++;
				}
			}
			scenarioIndex++;
		}
		in.close();
		
		MultiMaskEfsmSkeleton.MAX_OUTPUT_ACTION_COUNT = maxOutputActionCount;
		
		MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT = MultiMaskEfsmSkeleton.INPUT_EVENTS.size();
		
		int outputVariablesCount = scenarios[0].get(scenarios[0].size() - 1).getActions().get(0).getAlgorithm().length();
		for (int i = 0; i < scenarios.length; i++) {
			for (int j = 0; j < scenarios[i].size(); j++) {
				if (scenarios[i].get(j).getActions().isEmpty()) {
					scenarios[i].get(j).addActions(getActions('0', outputVariablesCount), "");
				} else if (scenarios[i].get(j).getActions().get(0).getAlgorithm().isEmpty()) {
					scenarios[i].get(j).getActions().clear();
					scenarios[i].get(j).addActions(getActions('0', outputVariablesCount), "");
				}
			}
		}
		
		return scenarios;
	}
	
	private static List<OutputAction> collapseEqualOutputActions(List<OutputAction> actions) {
		OutputAction a = actions.get(0);
		for (int i = 1; i < actions.size(); i++) {
			if (!actions.get(i).equals(a)) {
				return actions;
			}
		}
		
		List<OutputAction> result = new ArrayList<OutputAction>();
		result.add(a);
		return result;
	}
	
	public static String getActions(char c, int outputVariablesCount) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < outputVariablesCount; i++) {
			sb.append(c);
		}
		return sb.toString();
	}
	
	public static void readPredicateNames(String filename) {
		Scanner in = null;
		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		MultiMaskEfsmSkeleton.PREDICATE_NAMES = new ArrayList<String>();
		while (in.hasNext()) {
			MultiMaskEfsmSkeleton.PREDICATE_NAMES.add(in.next());
		}
		
		in.close();
	}
	
	private static boolean areActionsEqual(List<OutputAction> first, List<OutputAction> second) {
		if (first.size() != second.size()) {
			return false;
		}
		for (int i = 0; i < first.size(); i++) {
			if (first.get(i).getOutputEvent().isEmpty() || second.get(i).getOutputEvent().isEmpty()) {
				continue;
			}
			if (!first.get(i).equals(second.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	public static VarsActionsScenario[] removeScenarioNondeterminism(VarsActionsScenario[] scenarios) {
		VarsActionsScenario[] result  = new VarsActionsScenario[scenarios.length];
		
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
					if (scenarios[i].get(j).getVariableValues().equals(currentElement.getVariableValues()) &&
							!areActionsEqual(scenarios[i].get(j).getActions(), currentElement.getActions())) {
//							!scenarios[i].get(j).getActions().equals(currentElement.getActions())) {
						processed.add(currentElement);
						//remove nondeterminism
					} else {
						//add all elements
						for (int k = 0; k < numberOfRepeats; k++) {
							processed.add(currentElement);
						}
					}
					currentElement = scenarios[i].get(j);
					numberOfRepeats = 1;
					j++;
				}
				
				if (j == scenarios[i].size()) {
					for (int k = 0; k < numberOfRepeats; k++) {
						processed.add(currentElement);
					}
				}
			}
			
			result[i] = new VarsActionsScenario(processed);
		}
		return result;
	}
}
