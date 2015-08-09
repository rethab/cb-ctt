package ch.rethab.cbctt.formulation;

import ch.rethab.cbctt.formulation.constraint.Constraint;

/**
 *
 * Problem formulation. Each timetable may be solved with certain
 * objectives to be optimized. Depending on the specific formulation,
 * different objectives are optimized and each of them is weighted
 * differently.
 *
 * The weight has originally been added in order to convert these
 * problems to single-optimization problems.
 *
 * See 'Benchmarking Curriculum-Based Course Timetabling: Formulations,
        Data Formats, Instances, Validation, and Results' (De Cesco et al).
 *
 * @author Reto Habluetzel, 2015
 */
public abstract class Formulation {

    private final Constraint[] constraints;

    /** soft constraints have a multiplicative penalty factor */
    private final Objective[] objectives;

    protected Formulation(Constraint[] constraints, Objective[] objectives) {
        this.constraints = constraints;
        this.objectives = objectives;
    }

    public final int getNumberOfVariables() {
        return 1;
    }

    public final int getNumberOfConstraints() {
        return this.constraints.length;
    }

    public final int getNumberOfObjectives() {
        return this.objectives.length;
    }

    public Constraint[] getConstraints() {
        return constraints;
    }

    public Objective[] getObjectives() {
        return objectives;
    }

    public static class Objective {
        public final Constraint constraint;
        public final int penalty;
        public Objective(Constraint constraint, int penalty) {
            this.constraint = constraint;
            this.penalty = penalty;
        }
    }
}
