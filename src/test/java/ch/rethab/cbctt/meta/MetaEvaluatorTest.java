package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.ea.CurriculumBasedTimetabling;
import ch.rethab.cbctt.formulation.UD1Formulation;
import org.junit.Test;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import java.util.Arrays;

/**
 * @author Reto Habluetzel, 2015
 */
public class MetaEvaluatorTest {

    CurriculumBasedTimetabling problem = new CurriculumBasedTimetabling(new UD1Formulation(null), null);

    // these are solution from the timetable - ie. soft constraints
    NondominatedPopulation referenceSet = new NondominatedPopulation(Arrays.asList(
        new Solution(new double[]{0, 1000, 0}),
        new Solution(new double[]{0, 0, 1000}),
        new Solution(new double[]{1000, 0, 0})
    ));

    @Test
    public void shouldFoo() {
        // this is not an actual test. its just to try things
        MetaEvaluator me = new MetaEvaluator(problem, referenceSet);

        NondominatedPopulation nd = new NondominatedPopulation(Arrays.asList(
            new Solution(new double[]{100, 700, 500}),
            new Solution(new double[]{300, 200, 800}),
            new Solution(new double[]{500, 100, 400})
        ));

        double hypervolume = me.evaluate(0, nd);
        double addivieEpsilon = me.evaluate(1, nd);
        double generationalDistance = me.evaluate(2, nd);

        // System.out.printf("Hypervolume=%2.2f, AdditiveEpsilon=%2.2f, GenerationalDistance=%2.2f\n",
        //                   hypervolume, addivieEpsilon, generationalDistance);
    }

}