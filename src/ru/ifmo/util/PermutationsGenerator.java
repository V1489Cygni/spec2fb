package ru.ifmo.util;

import java.util.ArrayList;

public class PermutationsGenerator<T> {
    private T[] source;
    private int variationLength;
 
    public PermutationsGenerator(T[] source, int variationLength) {
        this.source = source;
        this.variationLength = variationLength;
    }
 
    public ArrayList<ArrayList<T>> getVariations() {
        int srcLength = source.length;
        int permutations = (int) Math.pow(srcLength, variationLength);
 
        ArrayList<ArrayList<T>> table = new ArrayList<ArrayList<T>>();

        for (int i = 0; i < permutations; i++) {
        	ArrayList<T> newList = new ArrayList<T>();
        	for (int j = 0; j < variationLength; j++) {
        		newList.add(null);
        	}
        	table.add(newList);
        }
        
        for (int i = 0; i < variationLength; i++) {
            int t2 = (int) Math.pow(srcLength, i);
            for (int p1 = 0; p1 < permutations;) {
                for (int al = 0; al < srcLength; al++) {
                    for (int p2 = 0; p2 < t2; p2++) {
                    	table.get(p1).set(i, source[al]);
                        p1++;
                    }
                }
            }
        }
        return table;
    }
}
