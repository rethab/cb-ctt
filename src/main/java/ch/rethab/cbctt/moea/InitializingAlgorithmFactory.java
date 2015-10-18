package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.Logger;
import ch.rethab.cbctt.StaticParameters;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import org.moeaframework.algorithm.SPEA2;
import org.moeaframework.core.*;
import org.moeaframework.core.spi.AlgorithmFactory;
import org.moeaframework.util.TypedProperties;

import java.util.Properties;


/**
 * @author Reto Habluetzel, 2015
 */
public class InitializingAlgorithmFactory extends AlgorithmFactory {

    private final StaticParameters staticParameters;

    private final Variation variation;

    public InitializingAlgorithmFactory(StaticParameters staticParameters, Variation variation) {
        this.staticParameters = staticParameters;
        this.variation = variation;
    }

    @Override
    public synchronized Algorithm getAlgorithm(String name, Properties properties, Problem problem) {
        TypedProperties typedProps = new TypedProperties(properties);
        if (name.equals("SPEA2")) {
            return newSPEA2(typedProps, problem);
        } else {
            throw new IllegalArgumentException("Unhandled Algorithm: " + name);
        }
    }

    private Algorithm newSPEA2(TypedProperties properties, Problem problem) {
        int populationSize = properties.getInt("populationSize", -1);
        int numberOfOffspring =  properties.getInt("numberOfOffspring", -1);
        int k = properties.getInt("k", -1);
        Initialization initialization = staticParameters.getInitializationFactory(problem).create(populationSize);
        return new SPEA2WithLogging(problem, initialization, variation, numberOfOffspring, k);
    }
}

class SPEA2WithLogging extends SPEA2 {

    public SPEA2WithLogging(Problem problem, Initialization initialization, Variation variation, int numberOfOffspring, int k) {
        super(problem, initialization, variation, numberOfOffspring, k);
    }

    @Override
    protected void iterate() {
        super.iterate();

        printPopulation();
    }

    @Override
    protected void initialize() {
        super.initialize();

        // initialize also performs a step, so we need to intercept
        // this as well if want the individuals after all steps
        printPopulation();
    }

    private void printPopulation() {
        // the moea framework doesn't use the archive for the archive for the best individuals, but always he population
        for (Solution solution : population) {
            // 1 means we are in meta. cb-ctt is multi-objective
            if (solution.getNumberOfObjectives() == 1) {

                CbcttStaticParameters mockParams = new CbcttStaticParameters(0, null, null, null, null, new VariationFactory(null, null, null));
                ParametrizationPhenotype params = ParametrizationPhenotype.fromSolution(mockParams, solution);
                int indicator = (int) solution.getObjective(0);
                Logger.gibber(String.format("Parameters [%s] --> Objective: %d", params.toString(), indicator));
            }
        }
    }
}