package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.ea.Timetable;

import java.io.Serializable;

/**
 * Constraints (or 'cost components') are used to evaluate a timetable.
 *
 * Hard Constraints define the feasibility of a timetable.
 * Soft Constraints (or 'objectives') are the optimization goals.
 *
 * A constraint may either be hard or soft depending on the
 * {@link ch.rethab.cbctt.formulation.Formulation}
 *
 * @author Reto Habluetzel, 2015
 */
public interface Constraint extends Serializable {

    /**
     * Returns the name for diagnostics etc.
     */
    String name();

    /**
     * Count the number of constraint violations the specified timetable has
     */
    int violations(Timetable t);
}
