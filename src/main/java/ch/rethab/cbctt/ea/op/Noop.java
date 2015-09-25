package ch.rethab.cbctt.ea.op;

import org.moeaframework.core.Solution;

/**
 * the moea framework always wants a variation, so here it goes
 *
 * @author Reto Habluetzel, 2015
 */
public class Noop implements CbcttVariation {

    @Override
    public String name() {
        return "Noop";
    }

    @Override
    public int getArity() {
        return 0;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        return new Solution[0];
    }
}
