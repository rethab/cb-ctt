package ch.rethab.cbctt.validator;

import ch.rethab.cbctt.ea.Timetable;

/**
 * @author Reto Habluetzel, 2015
 */
public interface Validator {

    boolean satisfiesHardConstraints(Timetable t);
}
