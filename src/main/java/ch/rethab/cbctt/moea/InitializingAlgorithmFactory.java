package ch.rethab.cbctt.moea;

import org.moeaframework.algorithm.ReferencePointNondominatedSortingPopulation;
import org.moeaframework.core.Algorithm;
import org.moeaframework.algorithm.SPEA2;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.spi.AlgorithmFactory;
import org.moeaframework.util.TypedProperties;
import org.moeaframework.util.distributed.ParallelNSGAII;

import java.util.Properties;
import java.util.concurrent.ExecutorService;


/**
 * @author Reto Habluetzel, 2015
 */
public class InitializingAlgorithmFactory extends AlgorithmFactory {

    private final InitializationFactory initializationFactory;
    private final Variation variation;
    private final ExecutorService executorService;

    public InitializingAlgorithmFactory(InitializationFactory initializationFactory, Variation variation,
                                        ExecutorService executorService) {
        this.initializationFactory = initializationFactory;
        this.variation = variation;
        this.executorService = executorService;
    }

    @Override
    public synchronized Algorithm getAlgorithm(String name, Properties properties, Problem problem) {
        TypedProperties typedProps = new TypedProperties(properties);
        if (name.equals("SPEA2")) {
            return newSPEA2(typedProps, problem);
        } else if (name.equals("NSGAIII"))  {
            return newNSGAIII(typedProps, problem);
        } else {
            throw new IllegalArgumentException("Unhandled Algorithm: " + name);
        }
    }

    private Algorithm newNSGAIII(TypedProperties properties, Problem problem) {
        int populationSize = (int)properties.getDouble("populationSize", 100.0D);

        ReferencePointNondominatedSortingPopulation population;
        int selection;
        if(properties.contains("divisionsOuter") && properties.contains("divisionsInner")) {
            selection = (int)properties.getDouble("divisionsOuter", 4.0D);
            int variation = (int)properties.getDouble("divisionsInner", 0.0D);
            population = new ReferencePointNondominatedSortingPopulation(problem.getNumberOfObjectives(), selection, variation);
        } else {
            selection = (int)properties.getDouble("divisions", 4.0D);
            population = new ReferencePointNondominatedSortingPopulation(problem.getNumberOfObjectives(), selection);
        }

        Initialization initialization = initializationFactory.create(populationSize);

        TournamentSelection selection1 = new TournamentSelection(2, new ParetoDominanceComparator());
        return new ParallelNSGAII(problem, population, null, selection1, variation, initialization, executorService);
    }

    private Algorithm newSPEA2(TypedProperties properties, Problem problem) {
        int populationSize = properties.getInt("populationSize", 100);
        int numberOfOffspring =  properties.getInt("numberOfOffspring", populationSize);
        int k = properties.getInt("kNearestNeighbour", 1);

        Initialization initialization = initializationFactory.create(populationSize);
        return new SPEA2(problem, initialization, variation, numberOfOffspring, k);
    }
}
