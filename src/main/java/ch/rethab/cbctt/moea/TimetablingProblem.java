package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.formulation.Formulation;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;

/**
 * Extension of the standard MOEA problem for udinese-style timetabling.
 *
 *  - MOEA doesn't provide a nice way to hook into the initialization
 *    mechanism. Our {@link InitializingAlgorithmFactory} provides
 *    for that.
 *
 *  - Each problem is associated with a problem formulation.
 *
 * @author Reto Habluetzel, 2015
 */
public interface TimetablingProblem extends Problem {

    Initialization getInitialization(int populationSize);

    Formulation getFormulation();

    Variation getVariation();

}
