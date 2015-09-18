package ch.rethab.cbctt;

import org.moeaframework.core.Variation;

import java.util.Collections;
import java.util.List;

/**
 * @author Reto Habluetzel, 2015
 */
public class StaticParameters {

    public static final int MAX_EVALUATIONS = 2000;

    public final List<Variation> crossover;

    public final List<Variation> mutation;

    public StaticParameters(List<Variation> crossover, List<Variation> mutation) {
        this.crossover = Collections.unmodifiableList(crossover);
        this.mutation = Collections.unmodifiableList(mutation);
    }
}
