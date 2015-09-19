package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.StaticParameters;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.meta.MetaEvaluator;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.AlgorithmFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Reto Habluetzel, 2015
 */
public class CbcttStaticParameters extends StaticParameters {

    public static final String ALGORITHM_NAME = "SPEA2";

    public final AlgorithmFactory algorithmFactory;

    public final Formulation formulation;

    public final Evaluator evaluator;

    public CbcttStaticParameters(List<Variation> crossover, List<Variation> mutation,
                                 AlgorithmFactory algorithmFactory, Formulation formulation,
                                 Evaluator evaluator) {
        super(crossover, mutation);
        this.algorithmFactory = algorithmFactory;
        this.formulation = formulation;
        this.evaluator = evaluator;
    }
}
