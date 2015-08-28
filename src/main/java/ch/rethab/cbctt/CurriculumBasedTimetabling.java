package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.ea.initializer.Initializer;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.ea.printer.Printer;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.constraint.Constraint;
import ch.rethab.cbctt.moea.InitializerAdapter;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.moea.TimetablingProblem;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * @author Reto Habluetzel, 2015
 */
public class CurriculumBasedTimetabling implements TimetablingProblem {

    private final Specification spec;

    private final Initializer initializer;

    private final Formulation formulation;

    private final Variation variation;

    private final Evaluator evaluator;

    public CurriculumBasedTimetabling(Specification spec, Initializer initializer, Formulation formulation,
                                      Variation variation, Evaluator evaluator) {
        this.spec = spec;
        this.initializer = initializer;
        this.formulation = formulation;
        this.variation = variation;
        this.evaluator = evaluator;
    }

    @Override
    public Initialization getInitialization(int populationSize) {
        return new InitializerAdapter(this.formulation, this.initializer, spec, populationSize);
    }

    @Override
    public Variation getVariation() {
        return variation;
    }

    public Formulation getFormulation() {
        return this.formulation;
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
