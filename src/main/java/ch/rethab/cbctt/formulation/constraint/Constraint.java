package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.ea.Timetable;

/**
 * @author Reto Habluetzel, 2015
 */
public interface Constraint {

    int violations(Timetable t);
}
