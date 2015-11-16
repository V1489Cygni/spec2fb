package ru.ifmo.optimization.instance.fsm.task.languagelearning;

public class Example {
	private int[] string;
	private boolean accept;
	
	public Example(String string, boolean accept) {
		this.string = new int[string.length()];
		for (int i = 0; i < string.length(); i++) {
			this.string[i] = (string.charAt(i) == '1') ? 1 : 0;
		}
		this.accept = accept;
	}

	public int[] getString() {
		return string;
	}

	public boolean accept() {
		return accept;
	}
}
