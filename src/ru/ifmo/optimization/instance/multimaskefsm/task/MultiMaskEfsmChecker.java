package ru.ifmo.optimization.instance.multimaskefsm.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import ru.ifmo.optimization.instance.multimaskefsm.MultiMaskEfsm;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class MultiMaskEfsmChecker {
	public static void main(String[] args) {
		MultiMaskTask task = new MultiMaskTask(new AbstractTaskConfig("gen-scenarios.properties"));
				
		MultiMaskTask labelTask = new MultiMaskTask(new AbstractTaskConfig("new-fbdk.properties"));
		MultiMaskEfsm efsm = labelTask.label(new MultiMaskEfsm("ecc.gv").getSkeleton());
		
		efsm.getSkeleton().clearUsedTransitions();
		
		List<String> traces = task.getTraces(efsm);
		
		System.out.println("used transitions ratio = " + efsm.getSkeleton().getUsedTransitionsCount() + "/" + efsm.getSkeleton().getDefinedTransitionsCount());
		
		System.out.println("used transitions ratio = " + 
		(double)efsm.getSkeleton().getUsedTransitionsCount() / efsm.getSkeleton().getDefinedTransitionsCount());
		
		PrintWriter out = null;
		
		try{
			out = new PrintWriter(new File("generated-scenarios"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		out.println(traces.size());
		for (String trace : traces) {
			out.println(trace);
		}
		out.close();
		
		MultiMaskTask checkTask = new MultiMaskTask(new AbstractTaskConfig("check-fbdk.properties"));
		MultiMaskEfsm generatedEfsm = new MultiMaskEfsm("simplified-efsm.gv");
		System.out.println(checkTask.getFitInstance(generatedEfsm));
//		System.out.println(checkTask.getFitInstance(efsm));
	}
}
