package ch.rethab.cbctt;

import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.formulation.Formulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import java.io.Serializable;

/**
 * @author Reto Habluetzel, 2015
 */
public class CurriculumBasedTimetabling implements Problem, Serializable {

    private final Formulation formulation;

    private final Evaluator evaluator;

    public CurriculumBasedTimetabling(Formulation formulation, Evaluator evaluator) {
        this.formulation = formulation;
        this.evaluator = evaluator;
    }

    @Override
    public String getName() {
        return this.getClass().getName() + "#" + this.formulation.getClass().getName();
    }

    @Override
    public int getNumberOfVariables() {
        return this.formulation.getNumberOfVariables();
    }

    @Override
    public int getNumberOfObjectives() {
        return this.formulation.getObjectives().length;
    }

    @Override
    public int getNumberOfConstraints() {
        return this.formulation.getConstraints().length;
    }

    @Override
    public void evaluate(Solution solution) {
        evaluator.evaluate(solution);
    }

    @Override
    public Solution newSolution() {
        throw new UnsupportedOperationException("I don't think this is required here");
    }

    @Override
    public void close() {

    }
}
