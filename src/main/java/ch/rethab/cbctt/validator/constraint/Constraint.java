package ch.rethab.cbctt.validator.constraint;

import ch.rethab.cbctt.ea.Timetable;

/**
 * @author Reto Habluetzel, 2015
 */
public interface Constraint {

    boolean satisfies(Timetable t);
}
