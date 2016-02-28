package ru.ifmo.optimization.algorithm.genetic.operator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;

public class Selection<Instance extends Constructable<Instance>> {
    
    public List<FitInstance<Instance>> apply(List<FitInstance<Instance>> population, int fixedPartSize) {
        final int n = population.size();
        final double[] weight = new double[n];
        weight[0] = population.get(0).getFitness();
        
        for (int i = 1; i < n; i++) {
            weight[i] = weight[i - 1] + population.get(i).getFitness();
        }
        
        List<FitInstance<Instance>> selected = new ArrayList<FitInstance<Instance>>();
        
        for (int i = 0; i < fixedPartSize; i++) {
        	selected.add(population.get(population.size() - i - 1));
        }
        
        while (selected.size() < population.size()) {
            double p = weight[n - 1] * ThreadLocalRandom.current().nextDouble();
            int i = 0;
            while (p > weight[i]) {
                i++;
            }
            selected.add(population.get(i));
        }
        
        return selected;
    }
}
