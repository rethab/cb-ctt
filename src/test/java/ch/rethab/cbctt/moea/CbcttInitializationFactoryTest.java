package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.meta.MetaCurriculumBasedTimetabling;
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

                assertTrue(kUpperBound <= popSize);
                assertTrue(archiveUpperBound <= popSize);
                assertTrue(sectorSizeUpperBound <= popSize);
            }
        }

    }

}