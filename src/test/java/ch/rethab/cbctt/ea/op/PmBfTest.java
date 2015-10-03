package ch.rethab.cbctt.ea.op;

import static org.hamcrest.Matchers.*;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.meta.MetaCurriculumBasedTimetabling;
import ch.rethab.cbctt.meta.MetaStaticParameters;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import ch.rethab.cbctt.moea.CbcttInitializationFactory;
import ch.rethab.cbctt.moea.VariationFactory;
import org.junit.Test;
import org.moeaframework.core.Solution;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class PmBfTest {

    @Test
    public void shouldNeverProduceOutOfBoundsValues() {
        VariationFactory variationFactory = new VariationFactory(null, null, null);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(0, null, null, null, null, variationFactory);
        MetaStaticParameters metaStaticParameters = new MetaStaticParameters(cbcttStaticParameters);
        MetaCurriculumBasedTimetabling problem = new MetaCurriculumBasedTimetabling(metaStaticParameters);
        CbcttInitializationFactory cbcttInitializationFactory = new CbcttInitializationFactory(problem);
        PmBf pbm = new PmBf(1, 20, 1);

        for (int i = 0; i < 1000; i++) {
            Solution[] parent = cbcttInitializationFactory.create(1).initialize();
            Solution[] offspring = pbm.evolve(parent);

            ParametrizationPhenotype child = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, offspring[0]);

            assertThat(child.getOperators(), is(not(empty())));

            assertThat(child.getPopulationSize(),
                    allOf(is(lessThanOrEqualTo(ParametrizationPhenotype.POPULATION_UPPER_BOUND)),
                            is(greaterThanOrEqualTo(ParametrizationPhenotype.POPULATION_LOWER_BOUND))));
            assertThat(child.getOffspringSize(),
                    allOf(is(lessThanOrEqualTo(child.getPopulationSize())),
                            is(greaterThanOrEqualTo(ParametrizationPhenotype.ARCHIVE_SIZE_LOWER_BOUND))));
            assertThat(child.getK(),
                    allOf(is(lessThan(child.getPopulationSize())),
                            is(greaterThanOrEqualTo(ParametrizationPhenotype.K_MEANS_LOWER_BOUND))));

        }
    }
}