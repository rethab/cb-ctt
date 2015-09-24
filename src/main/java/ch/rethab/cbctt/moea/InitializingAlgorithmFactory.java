package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.StaticParameters;
import org.moeaframework.algorithm.SPEA2;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
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
        return new SPEA2(problem, initialization, variation, numberOfOffspring, k);
    }
}
