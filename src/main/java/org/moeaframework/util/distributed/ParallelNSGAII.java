package org.moeaframework.util.distributed;

import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.core.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class ParallelNSGAII extends NSGAII {

    private final ExecutorService executorService;

    private final Selection selection;

    private final Variation variation;

    /**
     * Constructs the NSGA-II algorithm with the specified components.
     *
     * @param problem        the problem being solved
     * @param population     the population used to store solutions
     * @param archive        the archive used to store the result; can be {@code null}
     * @param selection      the selection operator
     * @param variation      the variation operator
     * @param initialization the initialization method
     */
    public ParallelNSGAII(Problem problem, NondominatedSortingPopulation population,
                          EpsilonBoxDominanceArchive archive, Selection selection, Variation variation,
                          Initialization initialization, ExecutorService executorService) {
        super(problem, population, archive, selection, variation, initialization);
        this.executorService = executorService;
        this.selection = selection;
        this.variation = variation;
    }

    @Override
    public void iterate() {
        NondominatedSortingPopulation population = getPopulation();
        EpsilonBoxDominanceArchive archive = getArchive();
        Population offspring = new Population();
        int populationSize = population.size();

        int offspringSize = 0;
        List<Future<Solution[]>> futures = new LinkedList<>();
        while (offspringSize < populationSize) {
            Solution[] parents = selection.select(variation.getArity(),
                    population);

            Future<Solution[]> future = executorService.submit(() -> variation.evolve(parents));
            futures.add(future);

            // assuming the arity is also the number of kids
            offspringSize += variation.getArity();
        }

        for (Future<Solution[]> future : futures) {
            try {
                Solution[] solutions = future.get(10, TimeUnit.SECONDS);
                offspring.addAll(solutions);
            } catch (InterruptedException e) {
                System.err.println("Interrupted during parallel variation");
            } catch (ExecutionException e) {
                System.err.println("ExecutionException during parallel variation: " + e.getMessage());
            } catch (TimeoutException e) {
                System.err.println("Timeout while retrieving result of parallel variation: " + e.getMessage());
            }
        }

        evaluateAll(offspring);

        if (archive != null) {
            archive.addAll(offspring);
        }

        population.addAll(offspring);
        population.truncate(populationSize);
    }
}
