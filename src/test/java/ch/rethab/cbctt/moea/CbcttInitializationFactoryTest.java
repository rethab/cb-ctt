package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.ea.op.*;
import ch.rethab.cbctt.meta.MetaCurriculumBasedTimetabling;
import static org.hamcrest.Matchers.*;
import ch.rethab.cbctt.meta.MetaStaticParameters;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import org.junit.Test;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class CbcttInitializationFactoryTest {

    @Test
    public void shouldNotExceedPopulationSizeWithArchiveSizeAndK() {
        VariationFactory variationFactory = new VariationFactory(null, null, null);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(null, null, null, variationFactory);
        MetaStaticParameters metaStaticParameters = new MetaStaticParameters(cbcttStaticParameters);
        MetaCurriculumBasedTimetabling problem = new MetaCurriculumBasedTimetabling(metaStaticParameters, null);
        InitializationFactory initializationFactory = new CbcttInitializationFactory(problem);

        for (int i = 0; i < 100; i++) {
            Initialization initialization = initializationFactory.create(1000);
            for (Solution solution : initialization.initialize()) {
                int popSize = (int) ((RealVariable) solution.getVariable(ParametrizationPhenotype.POPULATION_SIZE_IDX)).getValue();
                int archiveUpperBound = (int) ((RealVariable) solution.getVariable(ParametrizationPhenotype.ARCHIVE_SIZE_IDX)).getUpperBound();
                int kUpperBound = (int) ((RealVariable) solution.getVariable(ParametrizationPhenotype.K_IDX)).getUpperBound();
                int sectorSizeUpperBound = (int) ((RealVariable) solution.getVariable(ParametrizationPhenotype.SECTOR_SIZE_IDX)).getUpperBound();

                assertThat(kUpperBound, allOf(is(lessThanOrEqualTo(popSize)),
                        is(lessThanOrEqualTo(ParametrizationPhenotype.K_MEANS_UPPER_BOUND))));
                assertThat(archiveUpperBound, is(lessThanOrEqualTo(popSize)));
                assertThat(sectorSizeUpperBound, is(lessThanOrEqualTo(popSize)));
            }
        }

    }

    @Test
    public void shouldProduceValueWithinBounds() {
        VariationFactory variationFactory = new VariationFactory(null, null, null);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(null, null, null, variationFactory);
        MetaStaticParameters metaStaticParameters = new MetaStaticParameters(cbcttStaticParameters);
        MetaCurriculumBasedTimetabling problem = new MetaCurriculumBasedTimetabling(metaStaticParameters, null);
        InitializationFactory initializationFactory = new CbcttInitializationFactory(problem);

        // we also want to make sure that all operators are found once
        boolean mutationFound = false;
        boolean courseX = false;
        boolean currX = false;
        boolean secX = false;

        for (int i = 0; i < 200; i++) {
            Initialization initialization = initializationFactory.create(100);
            for (Solution solution : initialization.initialize()) {
                ParametrizationPhenotype params = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, solution);

                assertThat(params.getK(), allOf(is(lessThanOrEqualTo(params.getPopulationSize())),
                    is(greaterThanOrEqualTo(ParametrizationPhenotype.K_MEANS_LOWER_BOUND))));

                assertThat(params.getPopulationSize(), allOf(is(lessThanOrEqualTo(ParametrizationPhenotype.POPULATION_UPPER_BOUND)),
                    is(greaterThanOrEqualTo(ParametrizationPhenotype.POPULATION_LOWER_BOUND))));

                assertThat(params.getOffspringSize(), allOf(is(lessThanOrEqualTo(params.getPopulationSize())),
                    is(greaterThanOrEqualTo(ParametrizationPhenotype.ARCHIVE_SIZE_LOWER_BOUND))));

                if (!params.getOperators().isEmpty()) {
                    assertThat(params.getOperators().size(), is(lessThanOrEqualTo(4)));
                    for (CbcttVariation cbcttVariation : params.getOperators()) {
                        if (cbcttVariation instanceof CourseBasedCrossover) {
                            courseX = true;
                        } else if (cbcttVariation instanceof CurriculumBasedCrossover) {
                            currX = true;
                        } else if (cbcttVariation instanceof SectorBasedCrossover) {
                            secX = true;
                            SectorBasedCrossover sectorBasedCrossover = (SectorBasedCrossover) cbcttVariation;
                            assertThat(sectorBasedCrossover.getSectorSize(), allOf(is(lessThanOrEqualTo(params.getPopulationSize())),
                                    is(greaterThanOrEqualTo(ParametrizationPhenotype.SECTOR_SIZE_LOWER_BOUND))));
                        } else if (cbcttVariation instanceof CourseBasedMutation) {
                            mutationFound = true;
                            CourseBasedMutation mutation = (CourseBasedMutation) cbcttVariation;
                            assertThat(mutation.getProbability(), allOf(is(lessThanOrEqualTo(ParametrizationPhenotype.PROBABILITY_UPPER_BOUND)),
                                    is(greaterThanOrEqualTo(ParametrizationPhenotype.PROBABILITY_LOWER_BOUND))));
                        } else {
                            fail("Unknown Crossover");
                        }
                    }

                }
            }
        }

        assertThat(mutationFound, is(true));
        assertThat(courseX, is(true));
        assertThat(currX, is(true));
        assertThat(secX, is(true));

    }

    @Test
    public void shouldAlwaysUseSomeOperator() {
        /* if no operator is used, then SPEA2 may fail to generate enough
         * distinct offsprings (in fact, the offsprings will not differ
         * from the parents - how would they). Therefore, SPEA2 will be
         * stuck in the iterate method
        */

        VariationFactory variationFactory = new VariationFactory(null, null, null);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(null, null, null, variationFactory);
        MetaStaticParameters metaStaticParameters = new MetaStaticParameters(cbcttStaticParameters);
        MetaCurriculumBasedTimetabling problem = new MetaCurriculumBasedTimetabling(metaStaticParameters, null);
        InitializationFactory initializationFactory = new CbcttInitializationFactory(problem);

        for (int i = 0; i < 200; i++) {
            Initialization initialization = initializationFactory.create(100);
            for (Solution solution : initialization.initialize()) {
                ParametrizationPhenotype params = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, solution);
                assertThat(params.getOperators(), is(not(empty())));
            }
        }

    }

    @Test
    public void shouldProduceSolutionWithoutOperatorOnce() {
        VariationFactory variationFactory = new VariationFactory(null, null, null);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(null, null, null, variationFactory);
        MetaStaticParameters metaStaticParameters = new MetaStaticParameters(cbcttStaticParameters);
        MetaCurriculumBasedTimetabling problem = new MetaCurriculumBasedTimetabling(metaStaticParameters, null);
        InitializationFactory initializationFactory = new CbcttInitializationFactory(problem);

        boolean missingMutator = false;
        boolean missingCourseX = false;
        boolean missingCurrX = false;
        boolean missingSectorX = false;
        for (int i = 0; i < 200; i++) {
            Initialization initialization = initializationFactory.create(100);
            for (Solution solution : initialization.initialize()) {
                ParametrizationPhenotype params = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, solution);
                boolean foundMutator = false;
                boolean foundCourseX = false;
                boolean foundCurrX = false;
                boolean foundSectorX = false;
                for (CbcttVariation op : params.getOperators()) {
                    if (op instanceof CourseBasedCrossover) { foundCourseX = true; }
                    if (op instanceof CurriculumBasedCrossover) { foundCurrX = true; }
                    if (op instanceof SectorBasedCrossover) { foundSectorX = true; }
                    if (op instanceof CourseBasedMutation) { foundMutator = true; }
                }

                if (!foundMutator) { missingMutator = true; }
                if (!foundCourseX) { missingCourseX = true; }
                if (!foundCurrX)   { missingCurrX = true; }
                if (!foundSectorX) { missingSectorX = true; }

            }
        }

        assertTrue(missingMutator);
        assertTrue(missingCourseX);
        assertTrue(missingCurrX);
        assertTrue(missingSectorX);
    }

}