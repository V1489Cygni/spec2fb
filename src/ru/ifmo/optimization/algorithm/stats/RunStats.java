package ru.ifmo.optimization.algorithm.stats;

import java.util.concurrent.atomic.AtomicInteger;

public class RunStats {
	public static int N_CACHE_HITS = 0;
	public static int N_CANONICAL_CACHE_HITS = 0;
	public static int N_SAVED_EVALS_LAZY = 0;
	public static long N_CANONICAL_DIST = 0;
	public static double ERROR = 0;
	public static AtomicInteger GRAPH_BUNDLE_HITS = new AtomicInteger(0);
	

	public static void reset() {		
		N_CACHE_HITS = 0;
		N_CANONICAL_CACHE_HITS = 0;
		N_SAVED_EVALS_LAZY = 0;
		N_CANONICAL_DIST = 0;
		ERROR = 0;
		GRAPH_BUNDLE_HITS.set(0);
	}
}
