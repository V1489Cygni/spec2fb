package ru.ifmo.optimization.instance.multimaskefsm.task;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsm;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsmSkeleton;
import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskMetaData;
import ru.ifmo.optimization.instance.multimaskefsm.OutputAction;
import ru.ifmo.optimization.instance.multimaskefsm.State;
import ru.ifmo.optimization.instance.multimaskefsm.TransitionGroup;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class MultiMaskTaskWithTL extends MultiMaskTask {
    public String prefix;
    private int specNum;
    private List<String> names;
    private String vars;
    private String assignments;
    private String spec = "";
    private List<String> outputEvents;
    private int fitnessEvaluations, maxSatisfiedSpecifications;
    private double averageSatisfiedSpecifications;
    private long time, number;
    private int bmcLen;
    private double threshold;
    private double probability, initialProbability, scenariosOkProbability;
    private double scenariosWeight, tlWeight;
    private int maxTotalCounterExampleLength = -1;
    private double counterExampleLengthFitnessPower;
    private double counterExampleLengthWeight;
    private double maxFitness;

    public MultiMaskTaskWithTL(AbstractTaskConfig config) {
        super(config);
        try {
            initOE();
            initNames();
            initVars();
            initAssignments();
            initSpec(config.getProperty("spec-filename"));
            prefix = config.getProperty("prefix");
            bmcLen = Integer.parseInt(config.getProperty("bmc-len"));
            threshold = Double.parseDouble(config.getProperty("tl-eval-threshold"));
            initialProbability = Double.parseDouble(config.getProperty("initial-tl-eval-probability"));
            scenariosOkProbability = Double.parseDouble(config.getProperty("scenarios-ok-tl-eval-probability"));
            probability = initialProbability;
            scenariosWeight = Double.parseDouble(config.getProperty("scenarios-weight"));
            tlWeight = 1 - scenariosWeight;
            counterExampleLengthFitnessPower = Double.parseDouble(config.getProperty("counter-example-ff-power"));
            counterExampleLengthWeight = Double.parseDouble(config.getProperty("counter-example-length-weight"));
            System.out.println("TLFitness initialized with bmc=" + bmcLen + ", threshold=" + threshold +
                    ", initial-probability=" + initialProbability + ", scenarios-ok-probability=" + scenariosOkProbability + ", s-weight=" + scenariosWeight);
        } catch (IOException e) {
            System.err.println("Error while initializing MultiMaskTaskWitFhTL: " + e.getMessage());
            System.exit(1);
        }
    }
    

    @Override
    public FitInstance<MultiMaskEfsmSkeleton> getFitInstance(MultiMaskEfsmSkeleton instance) {
        //first, try with short scenarios
        MultiMaskEfsm labeledInstance = label(instance, shortScenarios);
        double multiplier = 0.3;
        double start = 0;
        RunData f = getF(labeledInstance, shortScenarios, 1.0);

        //if the fitness value is large enough, try with medium scenarios
        if (f.fitness >= startMediumPreciseFitnessCalculation) {
        	multiplier = 0.3;
        	start = 0.3;
            labeledInstance = label(instance, mediumScenarios);
            f = getF(labeledInstance, mediumScenarios, 1.0);

            //if the fitness value is large enough, try with full scenarios
            if (f.fitness >= startPreciseFitnessCalculation) {
            	multiplier = 0.4;
            	start = 0.6;
                labeledInstance = label(instance, scenarios);
                f = getF(labeledInstance, scenarios, 1.0);
            }
        }
        
        double scenariosFitness = start + multiplier * f.fitness;
        
        if (f.fitness > threshold) {
        	probability = scenariosOkProbability;
        }

        
        double tlFitness = getTLFitness(labeledInstance, start + f.fitness * multiplier);
        f.fitness = start + multiplier * (scenariosWeight * f.fitness + tlWeight * tlFitness);

        instance.getCounterExamples().addAll(labeledInstance.getSkeleton().getCounterExamples());
        instance.setFitness(tlFitness);  //not whole fitness, only TL part!!!
        
        if (f.fitness > maxFitness) {
        	maxFitness = f.fitness;
        	if (maxFitness > 0.9) {
        		System.out.println("maxF = " + maxFitness + "; scF = " + scenariosFitness + "; tlF = " + tlFitness);
        	}
        }
        
        if (f.fitness >= 1.0) {
        	if (tlFitness < 1) {
        		throw new RuntimeException("TLFitness = " + tlFitness);
        	}
        	
            storeResult(labeledInstance);
            f.fitness = 1.1;
            return new FitInstance<>(instance, f.fitness);
        }

        f.fitness += nStateChangesWeight * f.numberOfStateChanges;

        return new FitInstance<>(instance, f.fitness);
    }

    @Override
    public FitInstance<MultiMaskEfsmSkeleton> getFitInstance(MultiMaskEfsm instance) {
    	return null;
    }

    private void storeResult(MultiMaskEfsm ind) {
        try {
            Writer writer = new FileWriter(new File(prefix + "result.gv"));
            writer.write(ind.toGraphvizString());
            writer.close();
            writer = new FileWriter(new File(prefix + "result.smv"));
            writer.write(getSMV(ind));
            writer.close();
            FileOutputStream fos = new FileOutputStream(prefix + "result.data");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(ind);
            oos.close();
        } catch (IOException e) {
            System.err.println("Error while storing result: " + e.getMessage());
        }
    }

    public double interact(MultiMaskEfsm instance, String s) {
        try {
            Process p = Runtime.getRuntime().exec("NuSMV-2.5.4-x86_64-unknown-linux-gnu/bin/NuSMV");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            writer.write(s);
            writer.close();
            Scanner sc = new Scanner(new BufferedInputStream(p.getInputStream()));
            int t = 0;
            String last = null;
            int num = 0;
            while (sc.hasNext() || last != null) {
                String nl;
                if (last != null) {
                    nl = last;
                    last = null;
                } else {
                    nl = sc.nextLine();
                }
                if (nl.startsWith("-- specification")) {
                    if (nl.endsWith("true")) {
                        t++;
                        num++;
                    } else if (nl.endsWith("false")) {
                        last = readCounterexample(instance, sc);
                        num++;
                    } else {
                        System.out.println(nl);
                        throw new AssertionError();
                    }
                }
            }
            if (num != specNum) {
                Writer writer1 = new FileWriter(new File(prefix + "error_case.smv"));
                writer1.write(s);
                writer1.close();
                System.err.println("Unexpected number of specifications (see \"" + prefix + "error_case.smv\").");
                throw new AssertionError();
            }
            
            maxSatisfiedSpecifications = Math.max(maxSatisfiedSpecifications, t);
            averageSatisfiedSpecifications += t;
            fitnessEvaluations++;
            
            return (double) t / specNum;
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
    }

    public double interact(MultiMaskEfsm instance, String s, int length) {
        try {
            Process p = Runtime.getRuntime().exec("NuSMV-2.5.4-x86_64-unknown-linux-gnu/bin/NuSMV -bmc -bmc_length " + length);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            writer.write(s);
            writer.close();
            Scanner sc = new Scanner(new BufferedInputStream(p.getInputStream()));
            int t = 0;
            String last = null;
            int num = 0;
            while (sc.hasNext() || last != null) {
                String nl;
                if (last != null) {
                    nl = last;
                    last = null;
                } else {
                    nl = sc.nextLine();
                }
                if (nl.startsWith("-- specification")) {
                    if (nl.endsWith("false")) {
                        last = readCounterexample(instance, sc);
                        num++;
                    } else {
                        System.out.println(nl);
                        throw new AssertionError();
                    }
                } else if (nl.startsWith("-- no counterexample") && nl.endsWith(length + "")) {
                    t++;
                    num++;
                }
            }
            if (num != specNum) {
                Writer writer1 = new FileWriter(new File(prefix + "error_case.smv"));
                writer1.write(s);
                writer1.close();
                System.err.println("Unexpected number of specifications (see \"" + prefix + "error_case.smv\").");
                throw new AssertionError();
            }
            
            maxSatisfiedSpecifications = Math.max(maxSatisfiedSpecifications, t);
            averageSatisfiedSpecifications += t;
            fitnessEvaluations++;
            
            return (double) t / specNum;
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
    }

    private String readCounterexample(MultiMaskEfsm instance, Scanner sc) {
        String nl, last = null;
        String ie = "";
        String oe = "";
        String iv = "";
        for (int i = 0; i < MultiMaskEfsmSkeleton.PREDICATE_NAMES.size(); i++) {
            iv += "0";
        }
        String ov = "";
        List<ScenarioElement> elements = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            ov += "0";
        }
        while (true) {
            nl = sc.nextLine();
            if (nl.startsWith("-> State:")) {
                break;
            }
        }
        while (sc.hasNext()) {
            nl = sc.nextLine();
            if (!nl.startsWith("-") && nl.contains("=")) {
                Scanner scanner = new Scanner(nl);
                String name = scanner.next();
                scanner.next();
                boolean value = scanner.next().equals("TRUE");
                if (MultiMaskEfsmSkeleton.INPUT_EVENTS.containsKey(name)) {
                    if (value) {
                        ie = name;
                    } else if (ie.equals(name)) {
                        ie = "";
                    }
                } else if (outputEvents.contains(name)) {
                    if (value) {
                        oe = name;
                    } else if (oe.equals(name)) {
                        oe = "";
                    }
                } else if (MultiMaskEfsmSkeleton.PREDICATE_NAMES.contains(name)) {
                    int x = MultiMaskEfsmSkeleton.PREDICATE_NAMES.indexOf(name);
                    iv = iv.substring(0, x) + (value ? "1" : "0") + iv.substring(x + 1);
                } else if (names.contains(name)) {
                    int x = names.indexOf(name);
                    ov = ov.substring(0, x) + (value ? "1" : "0") + ov.substring(x + 1);
                }
            } else if (nl.startsWith("-> State:")) {
                elements.add(new ScenarioElement(ie, iv, new OutputAction(ov, oe)));
            } else if (!nl.startsWith("--") || nl.startsWith("-- specification") || nl.startsWith("-- no counterexample")) {
                last = nl;
                break;
            }
        }
        elements.add(new ScenarioElement(ie, iv, new OutputAction(ov, oe)));
        VarsActionsScenario scenario = new VarsActionsScenario(elements);
        instance.getSkeleton().addCounterExample(scenario);
        return last;
    }

    public String getSMV(MultiMaskEfsm instance) {
        assert names.size() == instance.getActions(0).get(0).getAlgorithm().length();
        Map<Integer, String> ie = new HashMap<>();
        for (String s : MultiMaskEfsmSkeleton.INPUT_EVENTS.keySet()) {
            ie.put(MultiMaskEfsmSkeleton.INPUT_EVENTS.get(s), s);
        }
        MultiMaskEfsmSkeleton skeleton = instance.getSkeleton();
        List<String> sNames = new ArrayList<>();
        Map<String, Integer> count = new HashMap<>();
        for (int i = 0; i < instance.getNumberOfStates(); i++) {
            String s = instance.getActions()[i].getAlgorithm();
            if (count.containsKey(s)) {
                count.put(s, count.get(s) + 1);
                s += "_" + count.get(s);
            } else {
                count.put(s, 1);
            }
            sNames.add(s);
        }
        StringBuilder s = new StringBuilder("MODULE main()\n\nVAR _state : {");
        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
            s.append("s_").append(sNames.get(i)).append(i == MultiMaskEfsmSkeleton.STATE_COUNT - 1 ? "" : ", ");
        }
        s.append("};\n");
        for (String ies : MultiMaskEfsmSkeleton.INPUT_EVENTS.keySet()) {
            s.append("VAR ").append(ies).append(" : boolean;\n");
        }
        for (String pn : MultiMaskEfsmSkeleton.PREDICATE_NAMES) {
            s.append("VAR ").append(pn).append(" : boolean;\n");
        }
        for (String oe : outputEvents) {
            s.append("VAR ").append(oe).append(" : boolean;\n");
        }
        for (String n : names) {
            s.append("VAR ").append(n).append(" : boolean;\n");
        }
        s.append(vars).append("\nASSIGN\n\ninit(_state) := s_").append(sNames.get(instance.getInitialState())).append(";\n\nnext(_state) := case\n");
        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
            State state = skeleton.getState(i);
            for(int e = 0; e < MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT; e++) {
                for(int t = 0; t < state.getTransitionGroupCount(e); t++) {
                    TransitionGroup tg = state.getTransitionGroup(e, t);
                    if(tg != null) {
                        List<Integer> m = tg.getMeaningfulPredicateIds();
                        for (int j = 0; j < tg.getTransitionsCount(); j++) {
                            int ns = tg.getNewState(j);
                            if (ns != -1) {
                                s.append("    _state = s_").append(sNames.get(i)).append(" & ").append(ie.get(e));
                                for (int k = 0; k < m.size(); k++) {
                                    boolean tr = ((j >> (m.size() - 1 - k)) & 1) == 1;
                                    s.append(" & ").append(tr ? "" : "!").append(MultiMaskEfsmSkeleton.PREDICATE_NAMES.get(m.get(k)));
                                }
                                s.append(" : s_").append(sNames.get(ns)).append(";\n");
                            }
                        }
                    }
                }
            }
        }
        
        s.append("    TRUE : _state;\nesac;\n\n");
        for (String ss : outputEvents) {
            if (!ss.isEmpty()) {
                s.append(ss).append(" := FALSE");
                for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
                    if (instance.getActions(i).get(0).getOutputEvent().equals(ss)) {
                        s.append(" | _state = s_").append(sNames.get(i));
                    }
                }
                s.append(";\n\n");
            }
        }
        for (int i = 0; i < names.size(); i++) {
            char c = instance.getActions(instance.getInitialState()).get(0).getAlgorithm().charAt(i);
            s.append("init(").append(names.get(i)).append(") := ").append(c == '1' ? "TRUE" : "FALSE").append(";\n\n");
            s.append("next(").append(names.get(i)).append(") := case\n    FALSE");
            for (int j = 0; j < MultiMaskEfsmSkeleton.STATE_COUNT; j++) {
                if (instance.getActions(j).get(0).getAlgorithm().charAt(i) == '1') {
                    s.append(" | next(_state) = s_").append(sNames.get(j));
                }
            }
            s.append(" : TRUE;\n    FALSE");
            for (int j = 0; j < MultiMaskEfsmSkeleton.STATE_COUNT; j++) {
                if (instance.getActions(j).get(0).getAlgorithm().charAt(i) == '0') {
                    s.append(" | next(_state) = s_").append(sNames.get(j));
                }
            }
            s.append(" : FALSE;\n    TRUE : ").append(names.get(i)).append(";\nesac;\n\n");
        }
        s.append(assignments).append("\n");
        s.append(spec);
        return s.toString();
    }

    public void printStats() {
    	if (fitnessEvaluations > 0) {
    		System.out.println("TLFitness: satisfied_specifications: {max: " + maxSatisfiedSpecifications +
    				", average: " + (averageSatisfiedSpecifications / fitnessEvaluations) + "; nevals=" + fitnessEvaluations + "}");
    	}
    	averageSatisfiedSpecifications = 0;
    	maxSatisfiedSpecifications = 0;
    	fitnessEvaluations = 0;
    	if (number > 0) {
    		System.out.println("Time: " + time + ", num: " + number + ", avg: " + time / number);
    	}
    }

    private void initOE() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("oe.txt"));
        outputEvents = new ArrayList<>();
        while (sc.hasNext()) {
            outputEvents.add(sc.next());
        }
    }

    private void initNames() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("names.txt"));
        names = new ArrayList<>();
        while (sc.hasNext()) {
            names.add(sc.next());
        }
    }

    private void initVars() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("vars.txt"));
        vars = "";
        while (sc.hasNext()) {
            vars += sc.nextLine() + "\n";
        }
    }

    private void initAssignments() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("assignments.txt"));
        assignments = "";
        while (sc.hasNext()) {
            assignments += sc.nextLine() + "\n";
        }
    }

    private void initSpec(String specFileName) throws FileNotFoundException {
        if (specFileName != null) {
            Scanner sc = new Scanner(new File(specFileName));
            specNum = sc.nextInt();
            spec = "";
            while (sc.hasNext()) {
                spec += sc.nextLine() + "\n";
            }
        }
    }

    public double getTLFitness(MultiMaskEfsm instance, double fitness) {
        if (fitness < threshold && ThreadLocalRandom.current().nextDouble() > probability && instance.getSkeleton().getFitness() > 0){
            return instance.getSkeleton().getFitness();
        }
        
        instance.getSkeleton().clearCounterExamples();
        String s = getSMV(instance);
        double d;
        if (bmcLen == 0) {
        	d = interact(instance, s);
        } else {
        	d = interact(instance, s, bmcLen);
        }
        
        if (instance.getSkeleton().getCounterExamplesLength() == 0) {
        	instance.getSkeleton().setFitness(d);
        	return d;
        }

        double result = (1.0 - counterExampleLengthWeight) * d + 
        		counterExampleLengthWeight * (1.0 - Math.pow(1.0 + 0.1 * instance.getSkeleton().getCounterExamplesLength(), -counterExampleLengthFitnessPower));
        instance.getSkeleton().setFitness(result);

        if (instance.getSkeleton().getCounterExamplesLength() > maxTotalCounterExampleLength) {
        	maxTotalCounterExampleLength = instance.getSkeleton().getCounterExamplesLength();
        	System.out.println("Max counterexample length = " + maxTotalCounterExampleLength 
        			+ "; f = " + result);
        }

        return result;
    }
    
    @Override
    public InstanceMetaData<MultiMaskEfsmSkeleton> getInstanceMetaData(
            MultiMaskEfsmSkeleton instance) {
        return new MultiMaskMetaData(getFitInstance(instance), label(instance, scenarios));
    }

    @Override
    public InstanceMetaData<MultiMaskEfsmSkeleton> getInstanceMetaData(
            MultiMaskEfsm instance) {
        return new MultiMaskMetaData(getFitInstance(instance), instance);
    }
}
