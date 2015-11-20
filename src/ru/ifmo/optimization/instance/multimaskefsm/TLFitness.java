package ru.ifmo.optimization.instance.multimaskefsm;

import javafx.util.Pair;
import ru.ifmo.optimization.instance.multimaskefsm.task.MultiMaskTask;
import ru.ifmo.optimization.instance.multimaskefsm.task.ScenarioElement;
import ru.ifmo.optimization.instance.multimaskefsm.task.VarsActionsScenario;

import java.io.*;
import java.util.*;

public class TLFitness {
    private static final Set<VarsActionsScenario> scenarios = new HashSet<>();
    private static boolean initialized;
    private static int specNum;
    private static List<String> names;
    private static String vars;
    private static String assignments;
    private static String spec = "";
    private static List<String> outputEvents;
    private static int fitnessEvaluations, maxSatisfiedSpecifications;
    private static double averageSatisfiedSpecifications, averageNegativeScenariosFitness;
    private static long time, number;

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

    public static void init(String specFileName) throws FileNotFoundException {
        synchronized (scenarios) {
            if (!initialized) {
                initOE();
                initNames();
                initVars();
                initAssignments();
                initSpec(specFileName);
                initialized = true;
            }
        }
    }

    public static Pair<Double, Double> getFitness(MultiMaskEfsm instance, MultiMaskTask task) {
        String s = getSMV(instance);
        synchronized (scenarios) {
            time -= System.currentTimeMillis();
        }
        double d = interact(s);
        synchronized (scenarios) {
            time += System.currentTimeMillis();
            number++;
        }
        double d2 = 0;
        synchronized (scenarios) {
            for (VarsActionsScenario scenario : scenarios) {
                d2 += task.runScenario(instance, scenario).getFitness() == 1.0 ? 1 : 0;
            }
            if (scenarios.size() == 0) {
                d2 = 1;
            } else {
                d2 = 1 - d2 / scenarios.size();
            }
            averageNegativeScenariosFitness += d2;
        }
        return new Pair<>(d, d2);
    }

    private static double interact(String s) {
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
                        last = readCounterexample(sc);
                        num++;
                    } else {
                        System.out.println(nl);
                        throw new AssertionError();
                    }
                }
            }
            if (num != specNum) {
                Writer writer1 = new FileWriter(new File("error_case.smv"));
                writer1.write(s);
                writer1.close();
                System.err.println("Unexpected number of specifications (see \"error_case.smv\").");
                throw new AssertionError();
            }
            synchronized (scenarios) {
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

    private static double interact(String s, int length) {
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
                        last = readCounterexample(sc);
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
                Writer writer1 = new FileWriter(new File("error_case.smv"));
                writer1.write(s);
                writer1.close();
                System.err.println("Unexpected number of specifications (see \"error_case.smv\").");
                throw new AssertionError();
            }
            synchronized (scenarios) {
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

    private static String readCounterexample(Scanner sc) {
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
        synchronized (scenarios) {
            scenarios.add(scenario);
        }
        return last;
    }

    public static String getSMV(MultiMaskEfsm instance) {
        Map<Integer, String> ie = new HashMap<>();
        for (String s : MultiMaskEfsmSkeleton.INPUT_EVENTS.keySet()) {
            ie.put(MultiMaskEfsmSkeleton.INPUT_EVENTS.get(s), s);
        }
        MultiMaskEfsmSkeleton skeleton = instance.getSkeleton();
        List<List<String>> transitions = new ArrayList<>();
        List<String> algorithms = new ArrayList<>();
        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
            transitions.add(new ArrayList<>());
        }
        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
            State state = skeleton.getState(i);
            for (int e = 0; e < MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT; e++) {
                for (int t = 0; t < state.getTGSize(e); t++) {
                    TransitionGroup tg = state.getTransitionGroup(e, t);
                    if (tg != null) {
                        List<Integer> m = tg.getMeaningfulPredicateIds();
                        for (int j = 0; j < tg.getTransitionsCount(); j++) {
                            int ns = tg.getNewState(j);
                            if (ns != -1) {
                                String s = "_state = state" + i + " & " + ie.get(e);
                                for (int k = 0; k < m.size(); k++) {
                                    boolean tr = ((j >> (m.size() - 1 - k)) & 1) == 1;
                                    s += " & " + (tr ? "" : "!") + MultiMaskEfsmSkeleton.PREDICATE_NAMES.get(m.get(k));
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
        String s = "MODULE main()\n\nVAR _state : {";
        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
            s += "state" + i + (i == MultiMaskEfsmSkeleton.STATE_COUNT - 1 ? "" : ", ");
        }
        s += "};\n";
        for (String ies : MultiMaskEfsmSkeleton.INPUT_EVENTS.keySet()) {
            s += "VAR " + ies + " : boolean;\n";
        }
        for (String pn : MultiMaskEfsmSkeleton.PREDICATE_NAMES) {
            s += "VAR " + pn + " : boolean;\n";
        }
        for (String oe : outputEvents) {
            s += "VAR " + oe + " : boolean;\n";
        }
        assert names.size() == algorithms.get(0).length();
        for (String n : names) {
            s += "VAR " + n + " : boolean;\n";
        }
        s += vars + "\nASSIGN\n\ninit(_state) := state" + instance.getInitialState() + ";\n\nnext(_state) := case\n";
        for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
            s += "    ";
            for (int j = 0; j < transitions.get(i).size(); j++) {
                s += transitions.get(i).get(j) + (j == transitions.get(i).size() - 1 ? " : state" + i + ";\n" : " | ");
            }
        }
        s += "    TRUE : _state;\nesac;\n\n";
        for (String ss : outputEvents) {
            if (!ss.isEmpty()) {
                s += ss + " := FALSE";
                for (int i = 0; i < MultiMaskEfsmSkeleton.STATE_COUNT; i++) {
                    if (instance.getActions(i).getOutputEvent().equals(ss)) {
                        s += " | _state = state" + i;
                    }
                }
                s += ";\n\n";
            }
        }
        for (int i = 0; i < names.size(); i++) {
            char c = instance.getActions(instance.getInitialState()).getAlgorithm().charAt(i);
            s += "init(" + names.get(i) + ") := " + (c == '1' ? "TRUE" : "FALSE") + ";\n\n";
            s += "next(" + names.get(i) + ") := case\n    FALSE";
            for (int j = 0; j < MultiMaskEfsmSkeleton.STATE_COUNT; j++) {
                if (instance.getActions(j).getAlgorithm().charAt(i) == '1') {
                    s += " | next(_state) = state" + j;
                }
            }
            s += " : TRUE;\n    FALSE";
            for (int j = 0; j < MultiMaskEfsmSkeleton.STATE_COUNT; j++) {
                if (instance.getActions(j).getAlgorithm().charAt(i) == '0') {
                    s += " | next(_state) = state" + j;
                }
            }
            s += " : FALSE;\n    TRUE : " + names.get(i) + ";\nesac;\n\n";
        }
        s += assignments + "\n";
        s += spec;
        return s;
    }

    public static void printStats() {
        synchronized (scenarios) {
            System.out.println("TLFitness: satisfied_specifications: {max: " + maxSatisfiedSpecifications +
                    ", average: " + (averageSatisfiedSpecifications / fitnessEvaluations) +
                    "}, average_negative_scenarios_fitness: " + (averageNegativeScenariosFitness / fitnessEvaluations));
            averageNegativeScenariosFitness = 0;
            averageSatisfiedSpecifications = 0;
            maxSatisfiedSpecifications = 0;
            fitnessEvaluations = 0;
            System.out.println("NSSize: " + scenarios.size());
            System.out.println("Time: " + time + ", num: " + number + ", avg: " + time / number);
        }
    }
}
