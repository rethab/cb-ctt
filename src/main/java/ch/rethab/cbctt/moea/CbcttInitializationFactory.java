package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This custom initializer is required, since our parameters are dependent on each other.
 * Both the 'numberOfOffspring' and 'k' must not exceed the population size, which is
 * why we need to set their upper bounds dynamically.
 *
 */
public class CbcttInitializationFactory implements InitializationFactory {

    private final Problem problem;

    public CbcttInitializationFactory(Problem problem) {
        this.problem = problem;
    }

    @Override
    public Initialization create(int populationSize) {
        return new CbcttInitializer(problem, populationSize);
    }

}

class CbcttInitializer extends RandomInitialization {

    /**
     * As we store the population size of the underlying problem, this
     * class must only be used once in order to make sure we don't use old
     * values, since this thing was not build with concurrency in mind.
     */
    private final AtomicBoolean reuseProtection = new AtomicBoolean(false);

    /**
     * This is the size of vhe cb ctt population (ie. the underlying
     * problem). the other population size, that is passed in to
     * the constructor, is actually the population size of the
     * outer problem.
     *
     * We need this to restrict the archive size upper bound.
     */
    private int cbcttPopulationSize = -1;

    /**
     * We need this to restrict the k upper bound.
     */
    private int cbcttArchiveSize = -1;

    public CbcttInitializer(Problem problem, int populationSize) {
        super(problem, populationSize);
    }

    private Variable indexInitialize(int i, Variable v) {
        // at least right now we have only real variables

        switch (i) {
            case ParametrizationPhenotype.POPULATION_SIZE_IDX:
                initialize(v);
                /* assuming that the population size is first initialized,
                 * we store the actual value, in order to later reset
                 * the upper bound of the archive size. */
                cbcttPopulationSize = (int) ((RealVariable) v).getValue();
                break;
            case ParametrizationPhenotype.ARCHIVE_SIZE_IDX:
                if (cbcttPopulationSize == -1)
                    throw new IllegalStateException("population size should have been initialized before");
                // create a new variable so we can set the upper bound
                v = EncodingUtils.newInt((int) ((RealVariable) v).getLowerBound(), cbcttPopulationSize);
                initialize(v);
                cbcttArchiveSize = (int) ((RealVariable) v).getValue();
                break;
            case ParametrizationPhenotype.K_IDX:
                if (cbcttArchiveSize == -1)
                    throw new IllegalStateException("archive size should have been initialized before");
                // k must be at most the population size, but also not exceed the k upper bound
                int kUpperBound = Math.min(cbcttArchiveSize, ParametrizationPhenotype.K_MEANS_UPPER_BOUND);
                v = EncodingUtils.newInt((int) ((RealVariable) v).getLowerBound(), kUpperBound);
                initialize(v);
                break;
            case ParametrizationPhenotype.SECTOR_SIZE_IDX:
                if (cbcttPopulationSize == -1)
                    throw new IllegalStateException("populatin size should have been initialized before");
                // create a new variable so we can set the upper bound
                v = EncodingUtils.newInt((int) ((RealVariable) v).getLowerBound(), cbcttPopulationSize);
                initialize(v);
                break;
            default:
                initialize(v);
                break;
        }

            return v;
    }

    @Override
    public Solution[] initialize() {

        if (reuseProtection.getAndSet(true)) {
            throw new IllegalStateException("You must not reuse this");
        }

        Solution[] initialPopulation = new Solution[populationSize];

        for (int i = 0; i < populationSize; i++) {
            Solution solution = problem.newSolution();

            for (int j = 0; j < solution.getNumberOfVariables(); j++) {
                // we overwrite the variable
                Variable variable = solution.getVariable(j);
                variable = indexInitialize(j, variable);
                solution.setVariable(j, variable);
            }

            initialPopulation[i] = solution;
        }

        return initialPopulation;
    }
}
