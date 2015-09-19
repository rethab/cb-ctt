package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.formulation.Formulation;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.AlgorithmFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Reto Habluetzel, 2015
 */
public final class MetaStaticParameters extends CbcttStaticParameters {

    public static final String META_ALGORITHM_NAME = "SPEA2";

    public final NondominatedPopulation referenceSet = new NondominatedPopulation(Arrays.asList(
            new Solution(new double[] {0})
    ));

    public MetaStaticParameters(List<Variation> crossover, List<Variation> mutation,
                                AlgorithmFactory algorithmFactory, Formulation formulation,
                                Evaluator evaluator) {
        super(crossover, mutation, algorithmFactory, formulation, evaluator);
    }
}
