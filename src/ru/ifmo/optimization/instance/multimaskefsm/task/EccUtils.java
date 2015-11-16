package ru.ifmo.optimization.instance.multimaskefsm.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.OutputAction;

public class EccUtils {
	public static VarsActionsScenario[] readScenarios(String scenariosFile) {
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
		
		while (in.hasNext()) {
			String[] io = in.nextLine().split(";"); 

			scenarios[scenarioIndex] = new VarsActionsScenario();
			
			int i = 0;
			
			//TODO replace
			String input = null;
			String inputEvent = "";
			String output = "";
			
			while (i < io.length) {				
				String outputEvent = "";
				if (io[i].contains("in=")) {
					input = io[i].replace("in=", "").trim();
					inputEvent = input.substring(0, input.indexOf("["));
					input = input.substring(input.indexOf("[") + 1 , input.indexOf("]"));
					if (MultiMaskEfsmSkeleton.PREDICATE_COUNT == -1) {
						MultiMaskEfsmSkeleton.PREDICATE_COUNT = input.length();
					}
					
					if (!MultiMaskEfsmSkeleton.INPUT_EVENTS.containsKey(inputEvent)) {
						MultiMaskEfsmSkeleton.INPUT_EVENTS.put(inputEvent, MultiMaskEfsmSkeleton.INPUT_EVENTS.size());
					}					
				}
				if (io[i].contains("out=")) {
					output = io[i].replace("out=", "").trim();
					outputEvent = output.substring(0, output.indexOf("["));
					output = output.substring(output.indexOf("[") + 1, output.indexOf("]"));					
					
				}
				i++;
				scenarios[scenarioIndex].add(new ScenarioElement(inputEvent, input, new OutputAction(output, outputEvent)));				
			}
			scenarioIndex++;
		}
		in.close();
		
		MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT = MultiMaskEfsmSkeleton.INPUT_EVENTS.size();
		
		int outputVariablesCount = scenarios[0].get(scenarios[0].size() - 1).getActions().length();
		for (int i = 0; i < scenarios.length; i++) {
			for (int j = 0; j < scenarios[i].size(); j++) {
				if (scenarios[i].get(j).getActions().isEmpty()) {
					scenarios[i].get(j).setActions(getActions('0', outputVariablesCount), "");
				}
			}
		}
		return scenarios;
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
							!scenarios[i].get(j).getActions().equals(currentElement.getActions())) {
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
