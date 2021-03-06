package ru.ifmo.optimization.algorithm.muaco;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.ant.factory.AntFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.AntColony;
import ru.ifmo.optimization.algorithm.muaco.colony.factory.AntColonyFactory;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.algorithm.muaco.startnodesselector.StartNodesSelector;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceGenerator;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.TestsModelCheckingTask;
import ru.ifmo.optimization.instance.multimaskefsm.task.MultiMaskTaskWithTL;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class MuACO<Instance extends Constructable<Instance>,
        MutationType extends InstanceMutation<Instance>> extends AbstractOptimizationAlgorithm<Instance> implements Callable<InstanceMetaData<Instance>> {
    protected MuACOConfig<Instance, MutationType> config;
    protected int numberOfAnts;
    protected SearchGraph<Instance, MutationType> graph;
    protected Path bestPath = new Path();
    protected AntStats<Instance> stats;
    protected PheromoneUpdater<Instance, MutationType> pheromoneUpdater;
    protected int colonyIterationNumber = 0;
    protected boolean restartWithInitialSolution;
    protected Instance initialSolution;
    private int antStagnationParameter;
    private int colonyStagnationParameter;
    private int maxEvaluationsTillStagnation;
    private int outputPeriod;
    private double evaporationRate;
    private int maxNumberOfNodesInGraph;
    private StartNodesSelector<Instance, MutationType> startNodesSelector;
    private AntFactory<Instance, MutationType> antFactory;
    private AntColonyFactory<Instance, MutationType> antColonyFactory;
    private InstanceGenerator instanceGenerator;
    private boolean restartWithBest;
    private Instance globalBestSolution = null;
    private double globalBestFitness = Double.MIN_VALUE;
    private double globalBestSolutionGenerationTime = 0;
    

    public MuACO(
            MuACOConfig<Instance, MutationType> config,
            AbstractOptimizationTask<Instance> task,
            Instance initialSolution) {
        super(task);
        this.config = config;
        init();
        setInitialSolution(initialSolution);
    }

    public MuACO(
            MuACOConfig<Instance, MutationType> config,
            AbstractOptimizationTask<Instance> task) {
        super(task);
        this.config = config;
        init();
//		setInitialSolution(randomInstance());
    }

    public MuACO(
            MuACOConfig<Instance, MutationType> config,
            AbstractTaskFactory<Instance> taskFactory) {
        this(config, taskFactory.createTask());
    }

    public MuACO(
            MuACOConfig<Instance, MutationType> config,
            AbstractTaskFactory<Instance> taskFactory,
            Instance initialSolution) {
        this(config, taskFactory.createTask(), initialSolution);
    }

    private void init() {
        this.antStagnationParameter = config.getStagnationParameter();
        this.colonyStagnationParameter = config.getBigStagnationParameter();
        this.maxEvaluationsTillStagnation = config.getMaxEvaluationsTillStagnation();
        this.numberOfAnts = config.getNumberOfAnts();
        this.outputPeriod = config.getOutputPeriod();
        this.maxNumberOfNodesInGraph = config.getMaxNumberOfNodes();
        this.evaporationRate = config.getEvaporationRate();
        this.restartWithBest = Boolean.parseBoolean(config.getProperty("restart-with-best"));
        this.restartWithInitialSolution = Boolean.parseBoolean(config.getProperty("restart-with-initial-solution"));
        boolean writeThreadLogs = Boolean.parseBoolean(config.getProperty("write-thread-logs", "false"));

        if (restartWithBest && restartWithInitialSolution) {
            throw new RuntimeException("restart-with-best=true and restart-with-initial-solution=true, only one permitted");
        }
        instanceGenerator = config.getInstanceGenerator();

        stats = new AntStats<Instance>(0, Double.MIN_VALUE, null, 0, writeThreadLogs);
        startNodesSelector = config.getStartNodesSelector();
        antFactory = config.getAntFactory(task, stats);
        antColonyFactory = config.getAntColonyFactory();
        pheromoneUpdater = config.getPheromoneUpdater(task);

//		String initialSolutionFileName = config.getProperty("initial-solution");
//		if (initialSolutionFileName != null) {
//			MultiMaskEfsm initial = new MultiMaskEfsm(initialSolutionFileName);
//			setInitialSolution((Instance) initial.getSkeleton());
//		} else {
        setInitialSolution(randomInstance());
//		}
    }

    public Instance getInitialSolution() {
        return initialSolution;
    }

    public void setInitialSolution(Instance startSolution) {
        initialSolution = startSolution.copyInstance(startSolution);
        FitInstance<Instance> firstInstance = task.getFitInstance(initialSolution);
        System.out.println("Fitness of initial solution (" + firstInstance.getInstance().computeStringHash()
                + ") = " + firstInstance.getFitness());

        graph = config.getSearchGraph(pheromoneUpdater, config.getHeuristicDistance(), firstInstance);
        stats.setBest(graph.getNode(firstInstance.getInstance()), firstInstance.getInstance(), System.currentTimeMillis());
        globalBestSolution = initialSolution.copyInstance(initialSolution);
    }

    public int getNumberOfFitnessEvaluations() {
        return task.getNumberOfFitnessEvaluations();
    }

    public SearchGraph<Instance, MutationType> getSearchGraph() {
        return graph;
    }

    public AntFactory<Instance, MutationType> getAntFactory() {
        return antFactory;
    }

    public void setAntFactory(AntFactory<Instance, MutationType> antFactory) {
        this.antFactory = antFactory;
    }

    public AbstractOptimizationTask<Instance> getTask() {
        return task;
    }

    public AntStats<Instance> getAntStats() {
        return stats;
    }

    protected Instance randomInstance() {
        return (Instance) instanceGenerator.createInstance(task);
    }

    public int getAntStagnationParameter() {
        return antStagnationParameter;
    }

    public void setAntStagnationParameter(int antStagnationParameter) {
        this.antStagnationParameter = antStagnationParameter;
    }

    @Override
    public InstanceMetaData<Instance> runAlgorithm() {
        stats.initLog();
        InstanceMetaData<Instance> result = runAlgorithm(randomInstance());
        System.out.println("I finished, result.fitness = " + result.getFitness());
        return result;
    }

    @Override
    public InstanceMetaData<Instance> call() throws Exception {
        return runAlgorithm();
    }


    protected boolean runIteration() {
        stats.setColonyIterationNumber(colonyIterationNumber);
        List<Node<Instance>> startNodes = getStartNodes();

        startNodes = startNodesSelector.getStartNodes(graph, numberOfAnts, null, bestPath, null);
        long startRunningColony = System.currentTimeMillis();
        AntColony<Instance, MutationType> antColony = antColonyFactory.createAntColony(graph, startNodes, antFactory, stats, task, antStagnationParameter);
        List<Path> paths = antColony.run();
        System.out.println("    Running colony: " + (System.currentTimeMillis() - startRunningColony) / 1000.0 + " sec.");
        System.out.println("    Bundle hits = " + RunStats.GRAPH_BUNDLE_HITS);
        stats.addAntPathData(paths);

        if (paths.isEmpty()) {
            return false;
        }
        Path iterationBestPath = Collections.max(paths);

        if (antColony.hasFoundGlobalOptimum()) {
            return false;
        }

        if (iterationBestPath.getBestFitness() >= bestPath.getBestFitness()) {
            bestPath = new Path(iterationBestPath);
        }

        updateBestNode(stats.getBestInstance(), stats.getBestFitness());

        graph.updatePheromone(paths, iterationBestPath, bestPath, evaporationRate, config.useRisingPaths());

        writeStats(paths);

        if (doRestart()) {
            restart();
        }
        colonyIterationNumber++;

        if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
            return false;
        }
        return true;
    }

    @Override
    public InstanceMetaData<Instance> runAlgorithm(Instance startSolution) {

        while (!(stats.getBestFitness() > task.getDesiredFitness()) && !Thread.currentThread().isInterrupted()) {
            if (!runIteration()) {
                break;
            }
        }
        Instance bestInstance = stats.getBestFitness() > globalBestFitness
                ? graph.getNodeInstance((Node<Instance>) stats.getBestNode())
                : globalBestSolution;
        double bestNodeGenerationTime = stats.getBestFitness() > globalBestFitness
                ? stats.getBestNodeGenerationTime()
                : globalBestSolutionGenerationTime;
        InstanceMetaData<Instance> result = task.getInstanceMetaData(bestInstance);
        result.setNumberOfFitnessEvaluations(task.getNumberOfFitnessEvaluations());
        result.setHistory(stats.getBestFitnessHistory(), stats.getStepsHistory());
        result.setNodeVisitStats(graph.getNodeVisitStats());
        result.setInstanceGenerationTime(bestNodeGenerationTime);
        return result;
    }

    protected void updateBestNode(Instance instance, double fitnessValue) {
    }

    protected List<Node<Instance>> getStartNodes() {
        return startNodesSelector.getStartNodes(graph, numberOfAnts, null, bestPath, null);
    }

    protected boolean doRestart() {
        if (stats.getColonyIterationNumber() - stats.getLastBestFitnessOccurence() > colonyStagnationParameter) {
            System.out.println("Colony iteration stagnation reached");
            return true;
        }

        if (graph.getNumberOfNodes() > maxNumberOfNodesInGraph) {
            System.out.println("Reached max number of nodex in the construction graph");
            return true;
        }

        if (stats.getStepsHistory().size() > 0) {
            if (task.getNumberOfFitnessEvaluations() - stats.getStepsHistory().get(stats.getStepsHistory().size() - 1) > maxEvaluationsTillStagnation) {
                System.out.println("Stagnation by fitness evaluations reached");
                return true;
            }
        }

        return false;
    }

    protected void restart() {
        if (stats.getBestNode().getFitness() > globalBestFitness) {
            globalBestSolution = graph.getNodeInstance((Node<Instance>) stats.getBestNode());
            globalBestFitness = stats.getBestFitness();
            globalBestSolutionGenerationTime = stats.getBestNodeGenerationTime();
            
            
        }

        long startRestart = System.currentTimeMillis();
        System.out.println("Restarting...");
        bestPath.clear();
        graph.clear();
        task.reset();

        System.gc();
        FitInstance<Instance> md = getInitialSolutionForRestart();
        graph = config.getSearchGraph(pheromoneUpdater, config.getHeuristicDistance(), md);
        stats.setBest(graph.getNode(md.getInstance()), md.getInstance(), System.currentTimeMillis());

        stats.setLastBestFitnessColonyIterationNumber(stats.getColonyIterationNumber());
        System.out.println("Restart: " + (System.currentTimeMillis() - startRestart) / 1000.0 + " sec.");
    }

    protected FitInstance<Instance> getInitialSolutionForRestart() {
        if (restartWithBest && !restartWithInitialSolution) {
            System.out.println("Restarting with best...");
            return task.getFitInstance(stats.getBestInstance());
        } else if (restartWithInitialSolution && !restartWithBest) {
            System.out.println("Restarting with initial solution...");
            return task.getFitInstance(initialSolution);
        }
        return task.getFitInstance(randomInstance());
    }

    private void writeStats(List<Path> paths) {
        double currentBestFitness = Collections.max(paths,
                new Comparator<Path>() {
                    @Override
                    public int compare(Path arg0, Path arg1) {
                        return Double.compare(arg0.getBestFitness(), arg1.getBestFitness());
                    }
                }).getBestFitness();

        double meanFitness = paths.get(0).getBestFitness();
        if (paths.size() > 1) {
            for (int i = 1; i < paths.size(); i++) {
                meanFitness += paths.get(i).getBestFitness();
            }
        }
        meanFitness /= (double) paths.size();

        if (stats.getColonyIterationNumber() % outputPeriod == 0) {
            System.out.println(stats.getColonyIterationNumber()
                    + " : currentFitness = " + currentBestFitness
                    + "; meanFitness = " + meanFitness
                    + "; best = " + stats.getBestFitness());
        }
        if (task instanceof MultiMaskTaskWithTL) {
            ((MultiMaskTaskWithTL) task).printStats();
        }
    }

    private Collection<Node<Instance>> getBestSolutions(int maxNumberOfSolutions) {
        TestsModelCheckingTask t = (TestsModelCheckingTask) task;

        PriorityQueue<Node<Instance>> nodes = new PriorityQueue<Node<Instance>>(
                maxNumberOfSolutions,
                new Comparator<Node<Instance>>() {
                    @Override
                    public int compare(Node<Instance> o1, Node<Instance> o2) {
                        return Double.compare(o1.getFitness(), o2.getFitness());
                    }
                }
        );
        nodes.add(stats.getBestNode());

        for (Node<Instance> node : graph.getNodes()) {
            if (nodes.size() < maxNumberOfSolutions) {
                nodes.add(node);
            } else {
                if (node.getFitness() > nodes.peek().getFitness()) {
                    nodes.poll();
                    nodes.add(node);
                }
            }
        }

        return nodes;
    }

    public List<double[]> getPoints(int maxNumberOfSolutions) {
        TestsModelCheckingTask t = (TestsModelCheckingTask) task;
        Collection<Node<Instance>> nodes = getBestSolutions(maxNumberOfSolutions);
        List<double[]> result = new ArrayList<double[]>();
        for (Node<Instance> node : nodes) {
            FSM fsm = (FSM) graph.getNodeInstance(node);
            result.add(t.getTestFitness(fsm));
        }
        return result;
    }

    public void printSortedBestSolutions(int maxNumberOfSolutions, String filename) {
        TestsModelCheckingTask t = (TestsModelCheckingTask) task;
        Collection<Node<Instance>> nodes = getBestSolutions(maxNumberOfSolutions);

        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Node<Instance> node : nodes) {
            FSM fsm = (FSM) graph.getNodeInstance(node);
            double[] ff = t.getTestFitness(fsm);
            for (int j = 0; j < ff.length; j++) {
                out.print(ff[j]);
                if (j < ff.length - 1) {
                    out.print(" ");
                }
            }
            out.println();
        }
        out.close();
    }

    public Instance getBestSolution() {
        return stats.getBestInstance();
    }
}