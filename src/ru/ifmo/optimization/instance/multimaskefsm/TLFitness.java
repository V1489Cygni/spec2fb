package ru.ifmo.optimization.instance.multimaskefsm;

import ru.ifmo.optimization.instance.multimaskefsm.task.ScenarioElement;
import ru.ifmo.optimization.instance.multimaskefsm.task.VarsActionsScenario;
import ru.ifmo.random.RandomProvider;

import java.io.*;
import java.util.*;

public class TLFitness {
    public static String prefix;
    private static boolean initialized;
    private static int specNum;
    private static List<String> names;
    private static String vars;
    private static String assignments;
    private static String spec = "";
    private static List<String> outputEvents;
    private static int fitnessEvaluations, maxSatisfiedSpecifications;
    private static double averageSatisfiedSpecifications;
    private static long time, number;
    private static int bmcLen;
    private static double threshold, probability;

    private static void initOE() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("oe.txt"));
        outputEvents = new ArrayList<>();
        while (sc.hasNext()) {
            outputEvents.add(sc.next());
        }
    }

    private static void initNames() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("names.txt"));
        names = new ArrayList<>();
        while (sc.hasNext()) {
            names.add(sc.next());
        }
    }

    private static void initVars() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("vars.txt"));
        vars = "";
        while (sc.hasNext()) {
            vars += sc.nextLine() + "\n";
        }
    }

    private static void initAssignments() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("assignments.txt"));
        assignments = "";
        while (sc.hasNext()) {
            assignments += sc.nextLine() + "\n";
        }
    }

    private static void initSpec(String specFileName) throws FileNotFoundException {
        if (specFileName != null) {
            Scanner sc = new Scanner(new File(specFileName));
            specNum = sc.nextInt();
            spec = "";
            while (sc.hasNext()) {
                spec += sc.nextLine() + "\n";
            }
        }
    }

    public static void init(String specFileName, String prefix1, int bmc, double threshold1, double probability1) throws FileNotFoundException {
        synchronized (TLFitness.class) {
            if (!initialized) {
                initOE();
                initNames();
                initVars();
                initAssignments();
                initSpec(specFileName);
                prefix = prefix1;
                bmcLen = bmc;
                threshold = threshold1;
                probability = probability1;
                initialized = true;
                System.out.println("TLFitness initialized with bmc=" + bmcLen + ", threshold=" + threshold + ", probability=" + probability);
            }
        }
    }

    public static double getFitness(MultiMaskEfsm instance, double fitness) {
        if (fitness < threshold && RandomProvider.getInstance().nextDouble() > probability) {
            return instance.getSkeleton().getFitness();
        }
        instance.getSkeleton().clearCounterExamples();
        String s = getSMV(instance);
        synchronized (TLFitness.class) {
            time -= System.currentTimeMillis();
        }
        double d;
        if (bmcLen == 0) {
            d = interact(instance, s);
        } else {
            d = interact(instance, s, bmcLen);
        }
        synchronized (TLFitness.class) {
            time += System.currentTimeMillis();
            number++;
        }
        instance.getSkeleton().setFitness(d);
        return d;
    }

    private static double interact(MultiMaskEfsm instance, String s) {
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
                Writer writer1 = new FileWriter(new File(p + "error_case.smv"));
                writer1.write(s);
                writer1.close();
                System.err.println("Unexpected number of specifications (see \"" + prefix + "error_case.smv\").");
                throw new AssertionError();
            }
            synchronized (TLFitness.class) {
                maxSatisfiedSpecifications = Math.max(maxSatisfiedSpecifications, t);
                averageSatisfiedSpecifications += t;
                fitnessEvaluations++;
            }
            return (double) t / specNum;
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
    }

    private static double interact(MultiMaskEfsm instance, String s, int length) {
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
            synchronized (TLFitness.class) {
                maxSatisfiedSpecifications = Math.max(maxSatisfiedSpecifications, t);
                averageSatisfiedSpecifications += t;
                fitnessEvaluations++;
            }
            return (double) t / specNum;
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
    }

    private static String readCounterexample(MultiMaskEfsm instance, Scanner sc) {
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

    public static String getSMV(MultiMaskEfsm instance) {
        Map<Integer, String> ie = new HashMap<>();
        for (String s : MultiMaskEfsmSkeleton.INPUT_EVENTS.keySet()) {
            ie.put(MultiMaskEfsmSkeleton.INPUT_EVENTS.get(s), s);
        }
        MultiMaskEfsmSkeleton skeleton = instance.getSkeleton();
        List<List<StringBuilder>> transitions = new ArrayList<>();
        List<String> algorithms = new ArrayList<>();
        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
            transitions.add(new ArrayList<>());
        }
        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
            State state = skeleton.getState(i);
            for (int e = 0; e < MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT; e++) {
                for (int t = 0; t < state.getTransitionGroupCount(e); t++) {
                    TransitionGroup tg = state.getTransitionGroup(e, t);
                    if (tg != null) {
                        List<Integer> m = tg.getMeaningfulPredicateIds();
                        for (int j = 0; j < tg.getTransitionsCount(); j++) {
                            int ns = tg.getNewState(j);
                            if (ns != -1) {
                                StringBuilder s = new StringBuilder("_state = state" + i + " & " + ie.get(e));
                                for (int k = 0; k < m.size(); k++) {
                                    boolean tr = ((j >> (m.size() - 1 - k)) & 1) == 1;
                                    s.append(" & ").append(tr ? "" : "!").append(MultiMaskEfsmSkeleton.PREDICATE_NAMES.get(m.get(k)));
                                }
                                transitions.get(ns).add(s);
                            }
                        }
                    }
                }
            }
            OutputAction action = instance.getActions(i);
            algorithms.add(action.getAlgorithm());
        }
        StringBuilder s = new StringBuilder("MODULE main()\n\nVAR _state : {");
        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
            s.append("state").append(i).append(i == MultiMaskEfsmSkeleton.STATE_COUNT - 1 ? "" : ", ");
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
        assert names.size() == algorithms.get(0).length();
        for (String n : names) {
            s.append("VAR ").append(n).append(" : boolean;\n");
        }
        s.append(vars).append("\nASSIGN\n\ninit(_state) := state").append(instance.getInitialState()).append(";\n\nnext(_state) := case\n");
        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
            s.append("    ");
            for (int j = 0; j < transitions.get(i).size(); j++) {
                s.append(transitions.get(i).get(j)).append(j == transitions.get(i).size() - 1 ? " : state" + i + ";\n" : " | ");
            }
        }
        s.append("    TRUE : _state;\nesac;\n\n");
        for (String ss : outputEvents) {
            if (!ss.isEmpty()) {
                s.append(ss).append(" := FALSE");
                for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
                    if (instance.getActions(i).getOutputEvent().equals(ss)) {
                        s.append(" | _state = state").append(i);
                    }
                }
                s.append(";\n\n");
            }
        }
        for (int i = 0; i < names.size(); i++) {
            char c = instance.getActions(instance.getInitialState()).getAlgorithm().charAt(i);
            s.append("init(").append(names.get(i)).append(") := ").append(c == '1' ? "TRUE" : "FALSE").append(";\n\n");
            s.append("next(").append(names.get(i)).append(") := case\n    FALSE");
            for (int j = 0; j < MultiMaskEfsmSkeleton.STATE_COUNT; j++) {
                if (instance.getActions(j).getAlgorithm().charAt(i) == '1') {
                    s.append(" | next(_state) = state").append(j);
                }
            }
            s.append(" : TRUE;\n    FALSE");
            for (int j = 0; j < MultiMaskEfsmSkeleton.STATE_COUNT; j++) {
                if (instance.getActions(j).getAlgorithm().charAt(i) == '0') {
                    s.append(" | next(_state) = state").append(j);
                }
            }
            s.append(" : FALSE;\n    TRUE : ").append(names.get(i)).append(";\nesac;\n\n");
        }
        s.append(assignments).append("\n");
        s.append(spec);
        return s.toString();
    }

    public static void printStats() {
        synchronized (TLFitness.class) {
            System.out.println("TLFitness: satisfied_specifications: {max: " + maxSatisfiedSpecifications +
                    ", average: " + (averageSatisfiedSpecifications / fitnessEvaluations) + "}");
            averageSatisfiedSpecifications = 0;
            maxSatisfiedSpecifications = 0;
            fitnessEvaluations = 0;
            System.out.println("Time: " + time + ", num: " + number + ", avg: " + time / number);
        }
    }
}
