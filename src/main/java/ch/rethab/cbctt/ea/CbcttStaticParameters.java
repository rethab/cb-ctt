package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.Logger;
import ch.rethab.cbctt.StaticParameters;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.moea.InitializationFactory;
import ch.rethab.cbctt.moea.LoggingProgressListener;
import ch.rethab.cbctt.moea.VariationFactory;
import org.moeaframework.core.Problem;
import org.moeaframework.util.progress.ProgressListener;

/**
 * @author Reto Habluetzel, 2015
 */
public class CbcttStaticParameters implements StaticParameters {

    private static final String ALGO_NAME = "SPEA2";

    private final int numberOfGenerations;

    private final Logger.Level level;

    public final Formulation formulation;

    public final Evaluator evaluator;

    private final InitializationFactory initializationFactory;

    private final VariationFactory variationFactory;

    public CbcttStaticParameters(int numberOfGenerations, Logger.Level level, Formulation formulation, Evaluator evaluator,
                                 InitializationFactory initializationFactory, VariationFactory variationFactory) {
        this.numberOfGenerations = numberOfGenerations;
        this.level = level;
        this.formulation = formulation;
        this.evaluator = evaluator;
        this.initializationFactory = initializationFactory;
        this.variationFactory = variationFactory;
    }

    @Override
    public ProgressListener getProgressListener() {
        return new LoggingProgressListener(level);
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

    public int getNumberOfGenerations() {
        return numberOfGenerations;
    }
}
