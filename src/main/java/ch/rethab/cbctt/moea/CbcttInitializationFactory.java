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
     * This is the size of the cb ctt population (ie. the unverlying
     * problem). the other population size, that is passed in to
     * the constructor, is actually the population size of the
     * outer problem.
     */
    private int cbcttPopulationSize = -1;

    public CbcttInitializer(Problem problem, int populationSize) {
        super(problem, populationSize);
    }

    private Variable indexInitialize(int i, Variable variable) {
        /* assuming that the population size is first initialized,
         * we store the actual value, in order to later reset
         * the upper bound of the archive size as well as k. */
        if (i == ParametrizationPhenotype.POPULATION_SIZE_IDX) {
            initialize(variable);
            cbcttPopulationSize = (int) ((RealVariable)variable).getValue();
            return variable;
        } else if (i == ParametrizationPhenotype.SECTOR_SIZE_IDX || i == ParametrizationPhenotype.ARCHIVE_SIZE_IDX || i == ParametrizationPhenotype.K_IDX) {
            if (cbcttPopulationSize == -1) throw new IllegalStateException("Something is fishy here");
            RealVariable rv = (RealVariable) variable;
            // create a new variable so we can set the upper bound
            RealVariable copy = EncodingUtils.newInt((int) rv.getLowerBound(), cbcttPopulationSize);
            initialize(copy);
            return copy;
        } else {
            initialize(variable);
            return variable;
        }
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
