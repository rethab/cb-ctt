package ch.rethab.cbctt;

import ch.rethab.cbctt.moea.InitializationFactory;
import org.moeaframework.core.Problem;
import org.moeaframework.util.progress.ProgressListener;

/**
 * @author Reto Habluetzel, 2015
 */
public interface StaticParameters {

    String algorithmName();

    InitializationFactory getInitializationFactory(Problem problem);

    ProgressListener getProgressListener();
}
