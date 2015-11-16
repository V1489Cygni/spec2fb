package ru.ifmo.optimization.instance.fsm;

public class CanonicalInstanceData<T> {
	private T data;
	private int[] newId;
	
	public CanonicalInstanceData(T t, int[] newId) {
		this.data = t;
		this.newId = newId;
	}
	
	public T getData() {
		return data;
	}
	
	public int[] getNewId() {
		return newId;
	}
}
