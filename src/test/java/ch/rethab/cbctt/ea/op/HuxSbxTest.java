package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.meta.MetaCurriculumBasedTimetabling;
import ch.rethab.cbctt.meta.MetaStaticParameters;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import ch.rethab.cbctt.moea.CbcttInitializationFactory;
import ch.rethab.cbctt.moea.VariationFactory;
import org.junit.Test;
import org.moeaframework.core.Solution;
import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class HuxSbxTest {

    @Test
    public void shouldNeverProduceOutOfBoundsValues() {
        // todo write why this is necessary if this turns out to be the reason
        VariationFactory variationFactory = new VariationFactory(null, null, null);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(null, null, null, variationFactory);
        MetaStaticParameters metaStaticParameters = new MetaStaticParameters(cbcttStaticParameters);
        MetaCurriculumBasedTimetabling problem = new MetaCurriculumBasedTimetabling(metaStaticParameters, null);
        CbcttInitializationFactory cbcttInitializationFactory = new CbcttInitializationFactory(problem);
        HuxSbx hsx = new HuxSbx(1, 1, 15);
        for (int i = 0; i < 1000; i++) {
            Solution[] parents = cbcttInitializationFactory.create(2).initialize();
            Solution[] offspring = hsx.evolve(parents);

            ParametrizationPhenotype parent1 = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, parents[0]);
            ParametrizationPhenotype parent2 = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, parents[1]);

            ParametrizationPhenotype param1 = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, offspring[0]);
            ParametrizationPhenotype param2 = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, offspring[1]);

            if (parent1.getOffspringSize() != param1.getOffspringSize() && parent2.getOffspringSize() != param1.getOffspringSize()) {
                assertThat(param1.getPopulationSize(), is(lessThanOrEqualTo(ParametrizationPhenotype.POPULATION_UPPER_BOUND)));
                assertThat(param1.getOffspringSize(), is(lessThanOrEqualTo(param1.getPopulationSize())));
                assertThat(param1.getK(), is(lessThan(param1.getPopulationSize())));
            }

            if (parent1.getOffspringSize() != param2.getOffspringSize() && parent2.getOffspringSize() != param2.getOffspringSize()) {
                assertThat(param2.getPopulationSize(), is(lessThanOrEqualTo(ParametrizationPhenotype.POPULATION_UPPER_BOUND)));
                assertThat(param2.getOffspringSize(), is(lessThanOrEqualTo(param2.getPopulationSize())));
                assertThat(param2.getK(), is(lessThan(param2.getPopulationSize())));
            }
        }
    }

}