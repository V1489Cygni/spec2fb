package ru.ifmo.optimization.runner.config;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.algorithm.genetic.FsmGeneticAlgorithm;
import ru.ifmo.optimization.algorithm.genetic.GeneticAlgorithm;
import ru.ifmo.optimization.algorithm.genetic.config.GeneticAlgorithmConfig;
import ru.ifmo.optimization.algorithm.muaco.MuACO;
import ru.ifmo.optimization.algorithm.muaco.MultiStartMuACO;
import ru.ifmo.optimization.algorithm.muaco.SmallGraphMuACO;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.algorithm.muaco.parallel.*;
import ru.ifmo.optimization.instance.fsm.task.factory.FsmTaskFactory;
import ru.ifmo.optimization.instance.multimaskefsm.task.MultiMaskTaskFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Daniil Chivilikhin
 */
public class OptimizationRunnerConfig {
    private Properties properties;

    public OptimizationRunnerConfig(String configFileName) {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(configFileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AbstractTaskFactory getTaskFactory() {
        switch (getInstanceType()) {
            case FSM:
                return new FsmTaskFactory(new AbstractTaskConfig(getTaskConfigFileName()));
            case FBDK_ECC:
                return new MultiMaskTaskFactory(new AbstractTaskConfig(getTaskConfigFileName()));
            default:
                return null;
        }
    }

    public String getSpecFileName() {
        return properties.getProperty("spec-filename");
    }

    public InstanceType getInstanceType() {
        return InstanceType.valueOf(properties.getProperty("instance-type"));
    }

    public String solutionDirName() {
        return properties.getProperty("solution-dir-name");
    }

    public int numberOfExperiments() {
        return Integer.parseInt(properties.getProperty("number-of-experiments"));
    }

    public String acoConfigFileName() {
        return properties.getProperty("aco-config-file-name");
    }

    public String getTaskConfigFileName() {
        return properties.getProperty("task-config-file-name");
    }

    public int getMaxEvalutions() {
        return Integer.parseInt(properties.getProperty("max-evaluations", "-1"));
    }

    public double getMaxRunTime() {
        return Double.parseDouble(properties.getProperty("max-run-time", "-1"));
    }

    public AbstractOptimizationAlgorithm getOptimizationAlgorithm(AbstractTaskFactory taskFactory) {

        AlgorithmType algorithmType = AlgorithmType.valueOf(properties.getProperty("algorithm-type"));
        MuACOConfig muacoConfig = new MuACOConfig("muaco.properties");
        switch (algorithmType) {
            case MUACO:
                return new MuACO(muacoConfig, taskFactory);
            case MULTI_START_MUACO:
                return new MultiStartMuACO(muacoConfig, taskFactory);
            case SMALL_GRAPH_MUACO:
                return new SmallGraphMuACO(muacoConfig, taskFactory);
            case PARALLEL_MUACO:
                return new ParallelMuACO(muacoConfig, taskFactory);
            case VARIABLE_WEIGHTS_PARALLEL_MUACO:
                return new VariableWeightsParallelMuACO(muacoConfig, taskFactory);
            case VARIABLE_NSTATES_PARALLEL_MUACO:
                return new VariableNumberOfStatesParallelMuACO(muacoConfig, taskFactory);
            case SHARED_BEST_PARALLEL_MUACO:
                return new SharedBestParallelMuACO(muacoConfig, taskFactory);
            case CROSSOVER_AND_SHARED_BEST_PARALLEL_MUACO:
                return new CrossoverAndSharedBestParallelMuACO(muacoConfig, taskFactory);
            case GENETIC:
                return new GeneticAlgorithm(new GeneticAlgorithmConfig("genetic.properties"), taskFactory);
            case FSM_GENETIC:
                return new FsmGeneticAlgorithm(new GeneticAlgorithmConfig("genetic.properties"), taskFactory);
            case PARALLEL_MUACO_GA:
                return new ParallelMuAcoGeneticAlgorithm(taskFactory);
            case CROSSOVER_AND_SHARED_BEST_PARALLEL_VARIABLE_WEIGHTS_MUACO:
                return new CrossoverAndSharedBestParallelMuACOWithVariableWeights(muacoConfig, taskFactory);
            case CROSSOVER_AND_VARIABLE_PARAMETERS_PARALLEL_MUACO:
                return new CrossoverAndVariableParametersParallelMuACO(muacoConfig, taskFactory);
            case RANDOM_CLUSTERING_PARALLEL_MUACO:
                return new RandomClusteringParallelMuACO(muacoConfig, taskFactory);
            case KMEANS_CLUSTERING_PARALLEL_MUACO:
                return new KmeansClusteringParallelMuACO(muacoConfig, taskFactory);
            case NO_INTERRUPTION_KMEANS_CLUSTERING_PARALLEL_MUACO:
                return new NoInterruptionKmeansClusteringParallelMuACO(muacoConfig, taskFactory);
            case FREQUENT_RESTART_PARALLEL_MUACO:
                return new FrequentRestartParallelMuACO(muacoConfig, taskFactory);
        }
        return null;
    }

    private static enum AlgorithmType {
        MUACO,
        MULTI_START_MUACO,
        SMALL_GRAPH_MUACO,
        PARALLEL_MUACO,
        SHARED_GET_PARALLEL_MUACO,
        VARIABLE_WEIGHTS_PARALLEL_MUACO,
        VARIABLE_NSTATES_PARALLEL_MUACO,
        SHARED_BEST_PARALLEL_MUACO,
        COMMON_START_SHARED_BEST_PARALLEL_MUACO,
        CROSSOVER_AND_SHARED_BEST_PARALLEL_MUACO,
        CROSSOVER_AND_SHARED_BEST_PARALLEL_VARIABLE_WEIGHTS_MUACO,
        CROSSOVER_AND_VARIABLE_PARAMETERS_PARALLEL_MUACO,
        RANDOM_CLUSTERING_PARALLEL_MUACO,
        KMEANS_CLUSTERING_PARALLEL_MUACO,
        NO_INTERRUPTION_KMEANS_CLUSTERING_PARALLEL_MUACO,
        FREQUENT_RESTART_PARALLEL_MUACO,
        GENETIC,
        ADAPTIVE_LAMBDA_ES,
        FSM_GENETIC,
        PARALLEL_MUACO_GA
    }


    public static enum InstanceType {
        FSM,
        FBDK_ECC
    }
}
