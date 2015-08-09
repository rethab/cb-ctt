package ch.rethab.cbctt.ea.op;

import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * @author Reto Habluetzel, 2015
 */
public class DummyVariation implements Variation {

    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public Solution[] evolve(Solution[] solutions) {
        System.out.println("Evolving");
        return solutions;
    }
}
