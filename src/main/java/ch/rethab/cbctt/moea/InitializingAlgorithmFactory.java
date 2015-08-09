package ch.rethab.cbctt.moea;

import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.algorithm.ReferencePointNondominatedSortingPopulation;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.spi.AlgorithmFactory;
import org.moeaframework.util.TypedProperties;

import java.util.Properties;


/**
 * @author Reto Habluetzel, 2015
 */
public class InitializingAlgorithmFactory extends AlgorithmFactory {

    @Override
    public synchronized Algorithm getAlgorithm(String name, Properties properties, Problem problem) {
        TypedProperties typedProps = new TypedProperties(properties);
        if (name.equals("NSGAIII"))  {
            return newNSGAIII(typedProps, (TimetablingProblem) problem);
        } else {
            throw new IllegalArgumentException("Unhandled Algorithm: " + name);
        }
    }

    private Algorithm newNSGAIII(TypedProperties properties, TimetablingProblem problem) {
        int populationSize = (int)properties.getDouble("populationSize", 100.0D);

        ReferencePointNondominatedSortingPopulation population = null;
        int selection;
        if(properties.contains("divisionsOuter") && properties.contains("divisionsInner")) {
            selection = (int)properties.getDouble("divisionsOuter", 4.0D);
            int variation = (int)properties.getDouble("divisionsInner", 0.0D);
            population = new ReferencePointNondominatedSortingPopulation(problem.getNumberOfObjectives(), selection, variation);
        } else {
            selection = (int)properties.getDouble("divisions", 4.0D);
            population = new ReferencePointNondominatedSortingPopulation(problem.getNumberOfObjectives(), selection);
        }

        Initialization initialization = problem.getInitialization(populationSize);
        Variation variation = problem.getVariation();

        TournamentSelection selection1 = new TournamentSelection(2, new ParetoDominanceComparator());
        return new NSGAII(problem, population, null, selection1, variation, initialization);
    }
}
