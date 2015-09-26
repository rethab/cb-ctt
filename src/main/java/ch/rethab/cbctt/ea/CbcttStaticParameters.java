package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.StaticParameters;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.moea.InitializationFactory;
import ch.rethab.cbctt.moea.VariationFactory;
import org.moeaframework.core.Problem;

/**
 * @author Reto Habluetzel, 2015
 */
public class CbcttStaticParameters implements StaticParameters {

    private static final String ALGO_NAME = "SPEA2";

    public final Formulation formulation;

    public final Evaluator evaluator;

    private final InitializationFactory initializationFactory;

    private final VariationFactory variationFactory;

    public CbcttStaticParameters(Formulation formulation, Evaluator evaluator,
                                 InitializationFactory initializationFactory, VariationFactory variationFactory) {
        this.formulation = formulation;
        this.evaluator = evaluator;
        this.initializationFactory = initializationFactory;
        this.variationFactory = variationFactory;
    }

    public VariationFactory getVariationFactory() {
        return variationFactory;
    }

    @Override
    public String algorithmName() {
        return ALGO_NAME;
    }

    @Override
    public InitializationFactory getInitializationFactory(Problem problem) {
        return initializationFactory;
    }
}
