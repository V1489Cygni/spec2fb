package ru.ifmo.util;

import structures.Node;

public class NodePair extends Pair<Node, Node>{

	public NodePair(Node first, Node second) {
		super(first, second);
	}
	
	public NodePair getComplimentaryPair() {
		return new NodePair(second, first);
	}

	@Override
	public int hashCode() {
		return (first.getNumber() + "-" + second.getNumber()).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		NodePair other = (NodePair)o;
		return first.equals(other.first) && second.equals(other.second);
	}
	
	@Override
	public String toString() {
		return first.toString() + "," + second.toString();
	}
}
