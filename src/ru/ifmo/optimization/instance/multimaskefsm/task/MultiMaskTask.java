package ru.ifmo.optimization.instance.multimaskefsm.task;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.comparator.MaxSingleObjectiveComparator;
import ru.ifmo.optimization.instance.multimaskefsm.*;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Pair;
import ru.ifmo.util.StringUtils;

import java.util.*;
import java.util.Map.Entry;

public class MultiMaskTask extends AbstractOptimizationTask<MultiMaskEfsmSkeleton> {
    private VarsActionsScenario[] scenarios;
    private VarsActionsScenario[] mediumScenarios;
    private VarsActionsScenario[] shortScenarios;
    private double startPreciseFitnessCalculation;
    private double startMediumPreciseFitnessCalculation;
    private int shorteningScale;
    private int mediumShorteningScale;
    private int outputVariablesCount;

    public MultiMaskTask(AbstractTaskConfig config) {
        desiredFitness = config.getDesiredFitness();
        comparator = new MaxSingleObjectiveComparator();
        scenarios = EccUtils.readScenarios(config.getProperty("scenarios"));
        scenarios = EccUtils.removeScenarioNondeterminism(scenarios);
        EccUtils.readPredicateNames(config.getProperty("predicate-names"));
        shorteningScale = Integer.parseInt(config.getProperty("shortening-scale"));
        mediumShorteningScale = Integer.parseInt(config.getProperty("medium-shortening-scale"));
        startPreciseFitnessCalculation = Double.parseDouble(config.getProperty("start-precise-fitness-calculation"));
        startMediumPreciseFitnessCalculation = Double.parseDouble(config.getProperty("start-medium-precise-fitness-calculation"));
        shortScenarios = preprocessScenarios(shorteningScale);
        mediumScenarios = preprocessScenarios(mediumShorteningScale);
        MultiMaskEfsmSkeleton.STATE_COUNT = Integer.parseInt(config.getProperty("desired-number-of-states"));
        MultiMaskEfsmSkeleton.MEANINGFUL_PREDICATES_COUNT = Integer.parseInt(config.getProperty("meaningful-predicates-count"));
        MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT = Integer.parseInt(config.getProperty("transition-groups-count"));
        MultiMaskEfsmSkeleton.PREDICATE_COUNT = MultiMaskEfsmSkeleton.PREDICATE_NAMES.size();
        outputVariablesCount = scenarios[0].get(scenarios[0].size() - 1).getActions().length();
    }

    private VarsActionsScenario[] preprocessScenarios(int scale) {
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

    private OutputAction getBestMask(List<Pair<OutputAction, OutputAction>> pairs) {
        if (pairs.isEmpty()) {
            return null;
        }
        int outputVectorLength = pairs.get(0).first.getAlgorithm().length();

        //generate algorithm
        StringBuilder label = new StringBuilder();
        for (int i = 0; i < outputVectorLength; i++) {
            int[][] map = new int[2][2];
            map[0][0] = 0;
            map[0][1] = 0;
            map[1][0] = 0;
            map[1][1] = 0;

            for (int j = 0; j < pairs.size(); j++) {
                map[Character.getNumericValue(pairs.get(j).first.getAlgorithm().charAt(i))]
                        [Character.getNumericValue(pairs.get(j).second.getAlgorithm().charAt(i))] += 1;
            }


            int setZero = map[1][0];
            int setOne = map[0][1];
            int leave = map[0][0] + map[1][1];

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

        //select output action
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

                    if (!scenario.get(i).getActions().equals(scenario.get(i - 1).getActions())) {
                        table[currentState].add(new Pair<OutputAction, OutputAction>(
                                new OutputAction(scenario.get(i - 1).getActions(), scenario.get(i - 1).getOutputEvent()),
                                new OutputAction(scenario.get(i).getActions(), scenario.get(i).getOutputEvent())));
                    }
                }
            }
        }

        MultiMaskEfsm labeled = new MultiMaskEfsm(instance);
        for (int state = 0; state < MultiMaskEfsmSkeleton.STATE_COUNT; state++) {
            OutputAction bestMask = getBestMask(table[state]);
            labeled.setActions(state, bestMask == null ? new OutputAction(EccUtils.getActions('x', outputVariablesCount), "") : bestMask);
        }
        return labeled;
    }

    private RawRunData getRawRunData(MultiMaskEfsm instance, VarsActionsScenario scenario) {
        int currentState = instance.getInitialState();
        List<String> outputs = new ArrayList<String>();
        int numberOfStateChanges = 0;

        String currentActions = scenario.getActions(0);
        currentActions = applyMask(currentActions, instance.getActions(currentState).getAlgorithm());
        int firstErrorPosition = -1;
        for (int i = 0; i < scenario.size(); i++) {
            int nextState = instance.getNewState(currentState, scenario.get(i).getInputEvent(), scenario.get(i).getVariableValues());
            if (nextState != -1) {
                numberOfStateChanges++;
                currentState = nextState;
                currentActions = applyMask(currentActions, instance.getActions(currentState).getAlgorithm());
            }
            if (firstErrorPosition == -1) {
                if (!currentActions.equals(scenario.get(i).getActions())) {
                    firstErrorPosition = i;
                }
                if (nextState != -1 && !instance.getActions(currentState).getOutputEvent().equals(scenario.get(i).getOutputEvent())) {
                    firstErrorPosition = i;
                }
            }
            outputs.add(currentActions + ((nextState != -1) ? instance.getActions(currentState).getOutputEvent() : ""));
        }

        return new RawRunData(outputs.toArray(new String[0]), numberOfStateChanges, firstErrorPosition);
    }

    private String getTrace(MultiMaskEfsm instance, VarsActionsScenario scenario) {
        int currentState = instance.getInitialState();
        StringBuilder result = new StringBuilder();

        String currentActions = "0000000";
//		currentActions = applyMask(currentActions, instance.getActions(currentState).getAlgorithm());
        for (int i = 0; i < scenario.size(); i++) {
            int nextState = instance.getNewState(currentState, scenario.get(i).getInputEvent(), scenario.get(i).getVariableValues());
            String outputEvent = "";
            if (nextState != -1) {
                currentState = nextState;
                currentActions = applyMask(currentActions, instance.getActions(currentState).getAlgorithm());
                outputEvent = instance.getActions(currentState).getOutputEvent();
            }
            result.append(new TraceElement(scenario.get(i).getInputEvent(), scenario.get(i).getVariableValues(),
                    outputEvent, currentActions).toString() + " ");
        }
        return result.toString().trim();
    }

    public RunData runScenario(MultiMaskEfsm instance, VarsActionsScenario scenario) {
        RawRunData rawRunData = getRawRunData(instance, scenario);
        String[] scenarioOutputs = scenario.getOutputs();

        double f1 = StringUtils.levenshteinDistance(scenarioOutputs, rawRunData.outputs) / Math.max(scenarioOutputs.length, rawRunData.outputs.length);
        return new RunData(Math.max(scenarioOutputs.length, rawRunData.outputs.length) == 0
                ? 1.0
                : f1, (double) rawRunData.numberOfStateChanges / (double) scenario.size(),
                rawRunData.firstErrorPosition == -1 ? 1.0 : (double) rawRunData.firstErrorPosition / (double) (scenario.size() - 1));
    }

    private String applyMask(String currentActions, String mask) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < currentActions.length(); i++) {
            result.append(mask.charAt(i) == 'x' ? currentActions.charAt(i) : mask.charAt(i));
        }
        return result.toString();
    }

    private RunData getF(MultiMaskEfsm labeledInstance, VarsActionsScenario[] s) {
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

        f /= (double) s.length;
        c /= (double) s.length;
        e /= (double) s.length;
        javafx.util.Pair<Double, Double> r = TLFitness.getFitness(labeledInstance, this);

        f = 0.45 * (0.9 * f + 0.1 * e) + 0.45 * r.getKey() + 0.1 * r.getValue();
        //f = 0.9 * f + 0.1 * e;
        return new RunData(f, c, e);
    }

    @Override
    public FitInstance<MultiMaskEfsmSkeleton> getFitInstance(MultiMaskEfsmSkeleton instance) {
        //first, try with short scenarios
        MultiMaskEfsm labeledInstance = label(instance, shortScenarios);
        RunData f = getF(labeledInstance, shortScenarios);

        //if the fitness value is large enough, try with medium scenarios
        if (f.fitness > startMediumPreciseFitnessCalculation) {
            labeledInstance = label(instance, mediumScenarios);
            f = getF(labeledInstance, mediumScenarios);

            //if the fitness value is large enough, try with full scenarios
            if (f.fitness > startPreciseFitnessCalculation) {
                labeledInstance = label(instance, scenarios);
                f = getF(labeledInstance, scenarios);
            }
        }

        if (f.fitness >= 1.0) {
            f.fitness = 1.1;
            return new FitInstance<MultiMaskEfsmSkeleton>(instance, f.fitness);
        }

        f.fitness += 0.0001 * f.numberOfStateChanges;

        return new FitInstance<MultiMaskEfsmSkeleton>(instance, f.fitness);
    }

    public List<String> getTraces(MultiMaskEfsm labeledInstance) {
        List<String> result = new ArrayList<String>();
        for (VarsActionsScenario scenario : scenarios) {
            result.add(getTrace(labeledInstance, scenario));
        }
        return result;
    }

    public FitInstance<MultiMaskEfsmSkeleton> getFitInstance(MultiMaskEfsm instance) {
//		RunData f = getF(instance, scenarios);

        //first, try with short scenarios
        RunData f = getF(instance, shortScenarios);

        //if the fitness value is large enough, try with medium scenarios
        if (f.fitness > startMediumPreciseFitnessCalculation) {
            f = getF(instance, mediumScenarios);

            //if the fitness value is large enough, try with full scenarios
            if (f.fitness > startPreciseFitnessCalculation) {
                f = getF(instance, scenarios);
            }
        }

        if (f.fitness >= 1.0) {
            f.fitness = 1.1;
            return new FitInstance<MultiMaskEfsmSkeleton>(instance.getSkeleton(), f.fitness);
        }

        f.fitness += 0.0001 * f.numberOfStateChanges;

        return new FitInstance<MultiMaskEfsmSkeleton>(instance.getSkeleton(), f.fitness);
    }

    public double getFitness(MultiMaskEfsm labeledInstance) {
        RunData f = getF(labeledInstance, scenarios);

        if (f.fitness >= 1.0) {
            f.fitness = 1.1;
            return 1.1;
        }

        return f.fitness;
    }

    public double getFitness(MultiMaskEfsm labeledInstance, VarsActionsScenario[] s) {
        RunData f = getF(labeledInstance, s);

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
    public double correctFitness(double fitness,
                                 MultiMaskEfsmSkeleton cachedInstance, MultiMaskEfsmSkeleton trueInstance) {
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
        private double fitness;
        private double numberOfStateChanges;
        private double firstErrorPosition;

        public RunData(double fitness, double numberOfStateChanges, double firstErrorPosition) {
            this.fitness = fitness;
            this.numberOfStateChanges = numberOfStateChanges;
            this.firstErrorPosition = firstErrorPosition;
        }

        public double getFitness() {
            return fitness;
        }
    }

    private class RawRunData {
        private String[] outputs;
        private double numberOfStateChanges;
        private double firstErrorPosition;

        public RawRunData(String[] outputs, double numberOfStateChanges, double firstErrorPosition) {
            this.outputs = outputs;
            this.numberOfStateChanges = numberOfStateChanges;
            this.firstErrorPosition = firstErrorPosition;
        }
    }

    private class TraceElement {
        private String inputEvent;
        private String inputVariables;
        private String outputEvent;
        private String outputVariables;

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
