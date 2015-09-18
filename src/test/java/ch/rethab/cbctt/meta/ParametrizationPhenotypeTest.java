package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.StaticParameters;
import org.junit.Test;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;

import java.util.Collections;
import java.util.List;

/**
 * @author Reto Habluetzel, 2015
 */
public class ParametrizationPhenotypeTest {

    Variation op1 = new DummyVariation();
    Variation op2 = new DummyVariation();

    @Test
    public void shouldNotBlowUp() {
        StaticParameters operators = new StaticParameters(Collections.singletonList(op1), Collections.singletonList(op2));
        List<Variable> variables = ParametrizationPhenotype.newVariables(operators);
        ParametrizationPhenotype.decode(operators, variables);
    }

}

class DummyVariation implements Variation {

    @Override
    public int getArity() {
        return 0;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        return new Solution[0];
    }
}
