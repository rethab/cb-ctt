package ch.rethab.cbctt.validator;

import ch.rethab.cbctt.ea.Timetable;

/**
 * @author Reto Habluetzel, 2015
 */
public interface Validator {

    /**
     * Feasible means all hard constraints are satisfied
     */
    boolean isFeasible(Timetable t);
}
