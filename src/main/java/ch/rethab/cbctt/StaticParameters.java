package ch.rethab.cbctt;

import ch.rethab.cbctt.moea.InitializationFactory;
import org.moeaframework.core.Problem;

/**
 * @author Reto Habluetzel, 2015
 */
public interface StaticParameters {

    int maxEvaluations();

    String algorithmName();

    InitializationFactory getInitializationFactory(Problem problem);
}
