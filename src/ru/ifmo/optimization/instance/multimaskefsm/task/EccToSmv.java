package ru.ifmo.optimization.instance.multimaskefsm.task;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsm;
import ru.ifmo.optimization.instance.multimaskefsm.TLFitness;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class EccToSmv {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Expected two arguments: input and output filenames.");
            System.exit(1);
        }
        OptimizationRunnerConfig config = new OptimizationRunnerConfig("experiment.properties");
        AbstractTaskFactory factory = config.getTaskFactory();
        factory.createTask();
        TLFitness.init(null);
        MultiMaskEfsm efsm = new MultiMaskEfsm(args[0]);
        String s = TLFitness.getSMV(efsm);
        Writer writer = new FileWriter(new File(args[1]));
        writer.write(s);
        writer.close();
    }
}
