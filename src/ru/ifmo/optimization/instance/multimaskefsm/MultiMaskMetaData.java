package ru.ifmo.optimization.instance.multimaskefsm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceMetaData;

public class MultiMaskMetaData extends InstanceMetaData<MultiMaskEfsmSkeleton> {

	private MultiMaskEfsm efsm;
	
	public MultiMaskMetaData(FitInstance<MultiMaskEfsmSkeleton> fitInstance, MultiMaskEfsm efsm) {
		super(fitInstance);
		this.efsm = efsm;

		efsm.setSkeleton(fitInstance.getInstance());
	}
	
	@Override
	public void printProblemSpecificData(String dirname) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(dirname + "/efsm.gv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		out.println(efsm.toGraphvizString());
		
		out.close();
	}
	
	public String print() {
		return efsm.toGraphvizString();
	}
	
	public MultiMaskEfsm getEfsm() {
		return efsm;
	}
}
