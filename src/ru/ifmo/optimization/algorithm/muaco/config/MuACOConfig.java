package ru.ifmo.optimization.algorithm.muaco.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.ant.config.AntConfig;
import ru.ifmo.optimization.algorithm.muaco.ant.current.CurrentAntConfig;
import ru.ifmo.optimization.algorithm.muaco.ant.current.CurrentAntFactory;
import ru.ifmo.optimization.algorithm.muaco.ant.factory.AntFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.consecutive.ConsecutiveAntColonyFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.factory.AntColonyFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.stepbystep.StepByStepAntColonyFactory;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.fsm.canonical.ConstructionGraphWithActiveCanonicalCache;
import ru.ifmo.optimization.algorithm.muaco.graph.fsm.canonical.PassiveCanonicalConstructionGraph;
import ru.ifmo.optimization.algorithm.muaco.heuristicdist.DestinationFitnessHeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.heuristicdist.FitnessDifferenceHeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.heuristicdist.HeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.heuristicdist.NoneHeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.pathselector.AbstractPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pathselector.AntColonySystemPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pathselector.HeuristicAntPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pathselector.config.PathSelectorConfig;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.current.GlobalElitistMinBoundPheromoneUpdater;
import ru.ifmo.optimization.algorithm.muaco.startnodesselector.BestNodeStartNodesSelector;
import ru.ifmo.optimization.algorithm.muaco.startnodesselector.BestPathStartNodesSelector;
import ru.ifmo.optimization.algorithm.muaco.startnodesselector.GlobalRouletteStartNodesSelector;
import ru.ifmo.optimization.algorithm.muaco.startnodesselector.RandomStartNodesSelector;
import ru.ifmo.optimization.algorithm.muaco.startnodesselector.RootStartNodeSelector;
import ru.ifmo.optimization.algorithm.muaco.startnodesselector.StartNodesSelector;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceGenerator;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.CanonicalFSMGenerator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.fsm.crossover.AbstractCrossover;
import ru.ifmo.optimization.instance.fsm.crossover.MixedCrossover;
import ru.ifmo.optimization.instance.fsm.crossover.SimpleCrossover;
import ru.ifmo.optimization.instance.fsm.crossover.TestBasedCrossover;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeFinalStateMutator;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeOutputActionMutator;
import ru.ifmo.optimization.instance.fsm.mutator.LucasReynoldsMutator;
import ru.ifmo.optimization.instance.fsm.mutator.efsm.ChangeFinalStateMutatorWithVerification;
import ru.ifmo.optimization.instance.fsm.mutator.efsm.EFSMAddOrDeleteTransitionMutator;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.multimaskefsm.RandomMultiMaskEfsmGenerator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.ChangeMeaningfulPredicatesMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.ChangeNumberOfActionsMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.CounterExampleMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.DestinationStateMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.FBDKAddDeleteTransitionMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.MaskMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.OldCounterExampleMutator;
import ru.ifmo.optimization.instance.multimaskefsm.mutator.SetFixedActionIdMutator;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig.InstanceType;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class MuACOConfig<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> {

    private InstanceType instanceType;
    private Properties properties = new Properties();

    public MuACOConfig(String propertiesFileName) {
        try {
            properties.load(new FileInputStream(new File(propertiesFileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public double getEvaporationRate() {
        return Double.parseDouble(properties.getProperty("evaporation-rate"));
    }

    public int getStagnationParameter() {
        return Integer.parseInt(properties.getProperty("stagnation-parameter"));
    }

    public int getBigStagnationParameter() {
        return Integer.parseInt(properties.getProperty("big-stagnation-parameter"));
    }

    public int getMaxEvaluationsTillStagnation() {
        return Integer.parseInt(properties.getProperty("max-evals-till-stagnation"));
    }

    public int getOutputPeriod() {
        return Integer.parseInt(properties.getProperty("output-period"));
    }

    public int getNumberOfMutationsPerStep() {
        return Integer.parseInt(properties.getProperty("number-of-mutations-per-step"));
    }

    public int getNumberOfAnts() {
        return Integer.parseInt(properties.getProperty("number-of-ants"));
    }

    public double getNewMutationProbability() {
        return Double.parseDouble(properties.getProperty("new-mutation-probability"));
    }

    public int getMaxNumberOfNodes() {
        return Integer.parseInt(properties.getProperty("max-number-of-nodes"));
    }

    public double getMultipleMutationProbability() {
        return Double.parseDouble(properties.getProperty("multiple-mutation-probability"));
    }

    public boolean useRisingPaths() {
        return Boolean.parseBoolean(properties.getProperty("use-rising-paths", "0"));
    }

    public int getNumberOfLazyAnts() {
        return Integer.parseInt(properties.getProperty("number-of-lazy-ants", "0"));
    }

    public int getMaxCanonicalCacheSize() {
        return Integer.parseInt(properties.getProperty("max-canonical-cache-size"));
    }

    public int getNumberOfThreads() {
        return Integer.parseInt(properties.getProperty("number-of-threads", "1"));
    }

    public String getInitialSolutionsDir() {
        return properties.getProperty("initial-solutions-dir");
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

    public List<Mutator> getFsmMutators(AbstractOptimizationTask<Instance> t) {
        List<Mutator> mutators = new ArrayList<Mutator>();
        for (MutatorType type : getMutatorTypes()) {
            switch (type) {
                case CHANGE_ACTIONS: {
                    AbstractAutomatonTask task = (AbstractAutomatonTask) t;
                    mutators.add(new ChangeOutputActionMutator(task.getActions(), task.getConstraints()));
                    break;
                }
                case CHANGE_DEST:
                    mutators.add(new ChangeFinalStateMutator());
                    break;

                case VERIFICATION_CHANGE_DEST: {
                    AbstractAutomatonTask task = (AbstractAutomatonTask) t;
                    double ordinaryTransitionMutationProbability = 1.0 / ((double) task.getDesiredNumberOfStates() * task.getEvents().size());
                    double multiplier = Double.parseDouble(getProperty("mutation-multiplier", "10"));
                    double counterexampleTransitionMutationProbability = multiplier * ordinaryTransitionMutationProbability;
                    mutators.add(new ChangeFinalStateMutatorWithVerification(ordinaryTransitionMutationProbability, counterexampleTransitionMutationProbability));
                    break;
                }

                case EFSM_ADD_DELETE_TRANSITIONS: {
                    AbstractAutomatonTask task = (AbstractAutomatonTask) t;
                    double addDeleteTransitionProbability = Double.parseDouble(properties.getProperty("add-delete-transition-probability"));
                    mutators.add(new EFSMAddOrDeleteTransitionMutator(task.getEvents(), addDeleteTransitionProbability));
                    break;
                }
                case LUCAS_REYNOLDS: {
                    AbstractAutomatonTask task = (AbstractAutomatonTask) t;
                    mutators.add(new LucasReynoldsMutator(task.getActions(), task.getConstraints(), task));
                    break;
                }
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

    public AbstractPathSelector<Instance, MutationType> getPathSelector(AbstractOptimizationTask<Instance> task, AntStats antStats) {
        PathSelectorType type;
        try {
            type = PathSelectorType.valueOf(properties.getProperty("path-selector"));
        } catch (Exception e) {
            return null;
        }

        switch (type) {
            case HEURISTIC:
                return new HeuristicAntPathSelector(task, getFsmMutators(task),
                        new PathSelectorConfig("heuristic-path-selector.properties"), antStats);
            case ACO_SYSTEM:
                return new AntColonySystemPathSelector(task, getFsmMutators(task),
                        new PathSelectorConfig("acs-path-selector.properties"), antStats);
        }
        return null;
    }

    public StartNodesSelector<Instance, MutationType> getStartNodesSelector() {
        StartNodesSelectorType type;
        try {
            type = StartNodesSelectorType.valueOf(properties.getProperty("start-nodes-selector"));
        } catch (Exception e) {
            return null;
        }

        switch (type) {
            case BEST_PATH:
                return new BestPathStartNodesSelector();
            case GLOBAL_ROULETTE:
                return new GlobalRouletteStartNodesSelector();
            case RANDOM:
                return new RandomStartNodesSelector();
            case ROOT:
                return new RootStartNodeSelector();
            case BEST:
                return new BestNodeStartNodesSelector();
        }

        return null;
    }

    public AntFactory<Instance, MutationType> getAntFactory(AbstractOptimizationTask<Instance> task, AntStats antStats) {
        return getAntFactory(task, antStats, getPathSelector(task, antStats));
    }

    public AntFactory<Instance, MutationType> getAntFactory(AbstractOptimizationTask<Instance> task, AntStats antStats, AbstractPathSelector<Instance, MutationType> pathSelector) {
        AntType type;
        try {
            type = AntType.valueOf(properties.getProperty("ant-type"));
        } catch (Exception e) {
            return null;
        }

        AntConfig antConfig;
        switch (type) {
            case CURRENT:
                antConfig = new CurrentAntConfig(pathSelector, null);
                return new CurrentAntFactory<Instance, MutationType>(antConfig, task, getPheromoneUpdater(task));
        }

        return null;
    }

    public AntFactory<Instance, MutationType> getAntFactory(AbstractOptimizationTask<Instance> task, AntStats antStats, AntConfig antConfig) {
        AntType type;
        try {
            type = AntType.valueOf(properties.getProperty("ant-type"));
        } catch (Exception e) {
            return null;
        }

        switch (type) {
            case CURRENT:
                return new CurrentAntFactory<Instance, MutationType>(antConfig, task, getPheromoneUpdater(task));
        }

        return null;
    }

    public AntColonyFactory getAntColonyFactory() {
        AntColonyFactoryType type;
        try {
            type = AntColonyFactoryType.valueOf(properties.getProperty("ant-colony-type"));
        } catch (Exception e) {
            return null;
        }

        switch (type) {
            case CONSECUTIVE:
                return new ConsecutiveAntColonyFactory();
            case STEP_BY_STEP:
                return new StepByStepAntColonyFactory();
        }

        return null;
    }

    public PheromoneUpdater<Instance, MutationType> getPheromoneUpdater(AbstractOptimizationTask<Instance> task) {
        PheromoneUpdaterType type;
        try {
            type = PheromoneUpdaterType.valueOf(properties.getProperty("pheromone-updater"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        switch (type) {
            case GLOBAL_ELITIST_MIN_BOUND:
                return new GlobalElitistMinBoundPheromoneUpdater<Instance, MutationType>();
        }

        return null;
    }

    public HeuristicDistance<Instance> getHeuristicDistance() {
        HeuristicDistanceType type;
        try {
            type = HeuristicDistanceType.valueOf(properties.getProperty("heuristic-distance"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        switch (type) {
            case ABS_DIFF:
                return new FitnessDifferenceHeuristicDistance<Instance>();
            case DEST_FITNESS:
                return new DestinationFitnessHeuristicDistance<Instance>();
            case NONE:
                return new NoneHeuristicDistance<Instance>();
        }

        return null;
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

    public SearchGraph<Instance, MutationType> getSearchGraph(PheromoneUpdater<Instance, MutationType> pheromoneUpdater,
                                                              HeuristicDistance<Instance> heuristicDistance, FitInstance<Instance> metaData) {
        ConstructionGraphType type;
        try {
            type = ConstructionGraphType.valueOf(properties.getProperty("construction-graph-type"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        switch (type) {
            case PLAIN:
                return new SearchGraph<Instance, MutationType>(pheromoneUpdater, heuristicDistance, metaData);
            case ACTIVE_CANONICAL_CACHE:
                return (SearchGraph<Instance, MutationType>) new ConstructionGraphWithActiveCanonicalCache(
                        (PheromoneUpdater<FSM, FsmMutation>) pheromoneUpdater,
                        (HeuristicDistance<FSM>) heuristicDistance,
                        (FitInstance<FSM>) metaData, getMaxCanonicalCacheSize());
            case PASSIVE_CANONICAL_CACHE:
                return (SearchGraph<Instance, MutationType>) new PassiveCanonicalConstructionGraph(
                        (PheromoneUpdater<FSM, FsmMutation>) pheromoneUpdater,
                        (HeuristicDistance<FSM>) heuristicDistance,
                        (FitInstance<FSM>) metaData, getMaxCanonicalCacheSize());
        }
        return null;
    }

    public AbstractCrossover getCrossover() {
        CrossoverType type;
        try {
            type = CrossoverType.valueOf(properties.getProperty("crossover-type"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        switch (type) {
            case SIMPLE:
                return new SimpleCrossover();
            case TEST_BASED:
                return new TestBasedCrossover();
            case MIXED:
                return new MixedCrossover();
        }
        return null;
    }

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

    private static enum PathSelectorType {
        HEURISTIC,
        PASSIVE_CANONICAL_HEURISTIC,
        ACTIVE_CANONICAL_HEURISTIC,
        LAZY_HEURISTIC,
        ACO_SYSTEM,
        ADAPTIVE,
        FIRST_ASCENT,
        BOUNDED_FIRST_ASCENT,
        LAZY_PASSIVE_CANONICAL_HEURISTIC,
        MULTIOBJECTIVE,
        SHARED_GET
    }

    private static enum StartNodesSelectorType {
        BEST_PATH,
        RANDOM,
        GLOBAL_ROULETTE,
        ROOT,
        BEST,
        MULTIOBJECTIVE,
        MULTI_RANDOM,
        MULTI_BEST_FIRST
    }

    private static enum AntType {
        CURRENT,
        ACO_SYSTEM,
        CAUTIOUS,
        PARALLEL
    }


    private static enum PheromoneUpdaterType {
        GLOBAL_ELITIST_MIN_BOUND,
        MAX_MIN,
        ACO_SYSTEM,
        RANK
    }

    private static enum AntColonyFactoryType {
        CONSECUTIVE,
        STEP_BY_STEP,
        PARALLEL
    }

    private static enum HeuristicDistanceType {
        NONE,
        ABS_DIFF,
        DEST_FITNESS
    }

    private static enum InstanceGeneratorType {
        PLAIN,
        CANONICAL,
        FBDK_ECC
    }

    private static enum ConstructionGraphType {
        PLAIN,
        ACTIVE_CANONICAL_CACHE,
        PASSIVE_CANONICAL_CACHE
    }

    private static enum CrossoverType {
        SIMPLE,
        TEST_BASED,
        MIXED
    }
}
