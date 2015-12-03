package ru.ifmo.optimization.instance.multimaskefsm.task;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsm;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
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
        config.getTaskFactory();
        MultiMaskTaskWithTL t = new MultiMaskTaskWithTL(new AbstractTaskConfig(config.getTaskConfigFileName()));
        MultiMaskEfsm efsm = new MultiMaskEfsm(args[0]);
        String s = t.getSMV(efsm);
        Writer writer = new FileWriter(new File(args[1]));
        writer.write(s);
        writer.close();
    }
}
