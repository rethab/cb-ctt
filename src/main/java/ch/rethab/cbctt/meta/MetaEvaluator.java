package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.Logger;
import org.moeaframework.core.Indicator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.indicator.AdditiveEpsilonIndicator;

/**
 * Evaluates a NondominatedPopulation of the meta algorithm.
 *
 * @author Reto Habluetzel, 2015
 */
public class MetaEvaluator {

    private final Problem problem;

    private final NondominatedPopulation referenceSet;

    public MetaEvaluator(Problem problem, NondominatedPopulation referenceSet) {
        this.problem = problem;
        this.referenceSet = referenceSet;
    }

    public double evaluate(NondominatedPopulation pop) {
        Logger.trace("ENTRY (" + pop.size() + ")");

        Indicator indicator = new AdditiveEpsilonIndicator(problem, referenceSet);
        double result = indicator.evaluate(pop);

        Logger.info("EXIT (" + result + ")");
        return result;
    }
}
