package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.StaticParameters;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.moea.CbcttInitializationFactory;
import ch.rethab.cbctt.moea.InitializationFactory;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import java.util.Arrays;

/**
 * @author Reto Habluetzel, 2015
 */
public final class MetaStaticParameters implements StaticParameters {

    /* set of best possible non-dominating solutions. while this may not
     * really be the lower bound in each dimension, it is our optimization
      * 'direction' */
    public final NondominatedPopulation referenceSet = new NondominatedPopulation(Arrays.asList(
            new Solution(new double[]{0, 0, 1}),
            new Solution(new double[]{0, 1, 0}),
            new Solution(new double[]{1, 0, 0})
    ));

    private final CbcttStaticParameters cbcttStaticParameters;

    public MetaStaticParameters(CbcttStaticParameters cbcttStaticParameters) {
        this.cbcttStaticParameters = cbcttStaticParameters;
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
