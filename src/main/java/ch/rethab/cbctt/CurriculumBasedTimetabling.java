package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.ea.initializer.Initializer;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.constraint.Constraint;
import ch.rethab.cbctt.moea.InitializerAdapter;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.moea.TimetablingProblem;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Solution;

/**
 * @author Reto Habluetzel, 2015
 */
public class CurriculumBasedTimetabling implements TimetablingProblem {

    private final Specification spec;

    private final Initializer initializer;

    private final Formulation formulation;

    private final SolutionConverter solutionConverter;

    public CurriculumBasedTimetabling(Specification spec, Initializer initializer, Formulation formulation, SolutionConverter solutionConverter) {
        this.spec = spec;
        this.initializer = initializer;
        this.formulation = formulation;
        this.solutionConverter = solutionConverter;
    }

    @Override
    public Initialization getInitialization(int populationSize) {
        return new InitializerAdapter(this.formulation, this.initializer, spec, populationSize);
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
        Timetable t = solutionConverter.fromSolution(solution);

        for (int i = 0; i < this.formulation.getConstraints().length; i++) {
            double constraint = - this.formulation.getConstraints()[i].violations(t);
            solution.setConstraint(i, constraint);
        }

        for (int i = 0; i < this.formulation.getObjectives().length; i++) {
            Constraint c = this.formulation.getObjectives()[i].constraint;
            int penalty  = this.formulation.getObjectives()[i].penalty;

            solution.setObjective(i, c.violations(t) * penalty);
        }
    }

    @Override
    public Solution newSolution() {
        throw new IllegalStateException("I believe this is not used in NSGA-III");
    }

    @Override
    public void close() {

    }
}
