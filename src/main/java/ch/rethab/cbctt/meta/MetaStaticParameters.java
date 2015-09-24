package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.StaticParameters;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.moea.CbcttInitializationFactory;
import ch.rethab.cbctt.moea.InitializationFactory;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import java.util.Collections;

/**
 * @author Reto Habluetzel, 2015
 */
public final class MetaStaticParameters implements StaticParameters {

    public final NondominatedPopulation referenceSet = new NondominatedPopulation(Collections.singletonList(
            new Solution(new double[]{0})
    ));

    private final CbcttStaticParameters cbcttStaticParameters;

    public MetaStaticParameters(CbcttStaticParameters cbcttStaticParameters) {
        this.cbcttStaticParameters = cbcttStaticParameters;
    }

    @Override
    public int maxEvaluations() {
        return 1000;
    }

    @Override
    public String algorithmName() {
        return "SPEA2";
    }

    public InitializationFactory getInitializationFactory(Problem problem) {
        return new CbcttInitializationFactory(problem);
    }

    public CbcttStaticParameters getCbcttStaticParameters() {
        return cbcttStaticParameters;
    }
}
