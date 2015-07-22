package ch.rethab.cbctt.ea.initializer;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Timetable;

import java.util.List;

/**
 * @author Reto Habluetzel, 2015
 */
public interface Initializer {

    List<Timetable> initialize(Specification specification, int size);

}
