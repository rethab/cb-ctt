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
        VariationFactory variationFactory = new VariationFactory(null, null, null);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(0, null, null, null, null, variationFactory);
        MetaStaticParameters metaStaticParameters = new MetaStaticParameters(cbcttStaticParameters);
        MetaCurriculumBasedTimetabling problem = new MetaCurriculumBasedTimetabling(metaStaticParameters);
        CbcttInitializationFactory cbcttInitializationFactory = new CbcttInitializationFactory(problem);
        HuxSbx hsx = new HuxSbx(1, 1, 15);

        // make sure sometimes we do run the checks in the if conditions
        int happenCounter = 0;
        int happenCounter2 = 0;
        for (int i = 0; i < 1000; i++) {
            Solution[] parents = cbcttInitializationFactory.create(2).initialize();
            Solution[] offspring = hsx.evolve(parents);

            ParametrizationPhenotype parent1 = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, parents[0]);
            ParametrizationPhenotype parent2 = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, parents[1]);

            ParametrizationPhenotype param1 = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, offspring[0]);
            ParametrizationPhenotype param2 = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, offspring[1]);

            assertThat(param1.getOperators(), is(not(empty())));
            assertThat(param2.getOperators(), is(not(empty())));

            // make sure we only compare when something actually was mutated
            if (parent1.getOffspringSize() != param1.getOffspringSize() && parent2.getOffspringSize() != param1.getOffspringSize()) {
                assertThat(param1.getPopulationSize(), is(lessThanOrEqualTo(ParametrizationPhenotype.POPULATION_UPPER_BOUND)));
                assertThat(param1.getPopulationSize(), is(greaterThanOrEqualTo(ParametrizationPhenotype.POPULATION_LOWER_BOUND)));
                assertThat(param1.getOffspringSize(), is(lessThanOrEqualTo(param1.getPopulationSize())));
                assertThat(param1.getOffspringSize(), is(greaterThanOrEqualTo(ParametrizationPhenotype.ARCHIVE_SIZE_LOWER_BOUND)));
                assertThat(param1.getK(), is(lessThan(param1.getPopulationSize())));
                assertThat(param1.getK(), is(greaterThanOrEqualTo(ParametrizationPhenotype.K_MEANS_LOWER_BOUND)));
                happenCounter++;
            }

            if (parent1.getOffspringSize() != param2.getOffspringSize() && parent2.getOffspringSize() != param2.getOffspringSize()) {
                assertThat(param2.getPopulationSize(), is(lessThanOrEqualTo(ParametrizationPhenotype.POPULATION_UPPER_BOUND)));
                assertThat(param2.getPopulationSize(), is(greaterThanOrEqualTo(ParametrizationPhenotype.POPULATION_LOWER_BOUND)));
                assertThat(param2.getOffspringSize(), is(lessThanOrEqualTo(param2.getPopulationSize())));
                assertThat(param2.getOffspringSize(), is(greaterThanOrEqualTo(ParametrizationPhenotype.ARCHIVE_SIZE_LOWER_BOUND)));
                assertThat(param2.getK(), is(lessThan(param2.getPopulationSize())));
                assertThat(param2.getK(), is(greaterThanOrEqualTo(ParametrizationPhenotype.K_MEANS_LOWER_BOUND)));
                happenCounter2++;
            }
        }

        // meaning every now and then something has happened.
        assertThat(happenCounter, is(greaterThan(100)));
        assertThat(happenCounter2, is(greaterThan(100)));
    }

}