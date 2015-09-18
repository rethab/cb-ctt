package ch.rethab.cbctt;

import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.meta.MetaEvaluator;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.AlgorithmFactory;

import java.util.List;

/**
 * @author Reto Habluetzel, 2015
 */
public final class MetaStaticParameters extends CbcttStaticParameters{

    public final MetaEvaluator metaEvaluator;

    public MetaStaticParameters(List<Variation> crossover, List<Variation> mutation,
                                AlgorithmFactory algorithmFactory, Formulation formulation,
                                Evaluator evaluator, MetaEvaluator metaEvaluator) {
        super(crossover, mutation, algorithmFactory, formulation, evaluator);
        this.metaEvaluator = metaEvaluator;
    }
}
