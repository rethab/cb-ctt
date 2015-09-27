package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.moea.VariationFactory;
import org.junit.Test;
import org.moeaframework.core.Variable;

import java.util.List;

/**
 * @author Reto Habluetzel, 2015
 */
public class ParametrizationPhenotypeTest {

    @Test
    public void shouldNotBlowUp() {
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(0, null, null, null, null, new VariationFactory(null, null, null));
        List<Variable> variables = ParametrizationPhenotype.newVariables(cbcttStaticParameters);
        ParametrizationPhenotype.decode(cbcttStaticParameters, variables);
    }

}
