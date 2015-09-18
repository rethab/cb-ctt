package ch.rethab.cbctt.meta;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.indicator.AdditiveEpsilonIndicator;
import org.moeaframework.core.indicator.GenerationalDistance;
import org.moeaframework.core.indicator.Hypervolume;
import org.moeaframework.core.indicator.NormalizedIndicator;

import java.util.Arrays;
import java.util.List;

/**
 * Evaluates a NondominatedPopulation of the meta algorithm.
 *
 * @author Reto Habluetzel, 2015
 */
public class MetaEvaluator {

    private final List<NormalizedIndicator> indicators;

    public MetaEvaluator(Problem p, NondominatedPopulation referenceSet) {
        this.indicators = Arrays.asList(
                new Hypervolume(p, referenceSet),
                new AdditiveEpsilonIndicator(p, referenceSet),
                new GenerationalDistance(p, referenceSet)
        );
    }

    public double evaluate(int idx, NondominatedPopulation result) {
        return this.indicators.get(idx).evaluate(result);
    }

    public int getNumberOfObjectives() {
        return this.indicators.size();
    }
}
