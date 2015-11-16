package ru.ifmo.optimization.application;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import ru.ifmo.optimization.instance.multimaskefsm.TLFitness;
import ru.ifmo.optimization.runner.OptimizationRunner;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;

import java.io.FileNotFoundException;

/**
 * @author Daniil Chivilikhin
 */
public class Application {

    @Option(name = "--seed", aliases = {"-s"}, usage = "random seed", metaVar = "<seed>", required = false)
    private int seed;

    public static void main(String[] args) {
        new Application().run(args);
    }

    private void launch(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            System.out.println("MUACO");
            System.out.println("Author: Daniil Chivilikhin (chivdan@rain.ifmo.ru)\n");
            System.out.print("Usage: ");
            parser.printSingleLineUsage(System.out);
            System.out.println();
            parser.printUsage(System.out);
            return;
        }
        OptimizationRunnerConfig config = new OptimizationRunnerConfig("experiment.properties");
        try {
            TLFitness.init(config.getSpecFileName());
        } catch (FileNotFoundException e) {
            System.err.println("Error while reading specification: " + e.getMessage());
            System.exit(1);
        }
        OptimizationRunner runner = new OptimizationRunner(config, seed);
        runner.run();
    }

    private void run(String[] args) {
        launch(args);
    }
}
