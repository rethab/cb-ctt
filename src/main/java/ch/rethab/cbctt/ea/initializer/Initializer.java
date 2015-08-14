package ch.rethab.cbctt.ea.initializer;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Timetable;

import java.util.List;

/**
 * @author Reto Habluetzel, 2015
 */
public interface Initializer {

    /**
     * Creates a specified number of timetables using the given specification
     */
    List<Timetable> initialize(Specification specification, int size);

}
