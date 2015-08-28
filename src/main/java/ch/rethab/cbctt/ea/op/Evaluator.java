package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.constraint.Constraint;
import ch.rethab.cbctt.moea.SolutionConverter;
import org.moeaframework.core.Solution;

public class Evaluator {

    private final Formulation formulation;

    private final SolutionConverter solutionConverter;

    public Evaluator(Formulation formulation, SolutionConverter solutionConverter) {
        this.formulation = formulation;
        this.solutionConverter = solutionConverter;
    }

    public void evaluate(Solution s) {
        Timetable t = solutionConverter.fromSolution(s);

        for (int i = 0; i < this.formulation.getConstraints().length; i++) {
            double constraint = - this.formulation.getConstraints()[i].violations(t);
            s.setConstraint(i, constraint);
        }

        for (int i = 0; i < this.formulation.getObjectives().length; i++) {
            Constraint c = this.formulation.getObjectives()[i].constraint;
            int penalty  = this.formulation.getObjectives()[i].penalty;

            int objective = c.violations(t) * penalty;
            s.setObjective(i, objective);
        }
    }
}
