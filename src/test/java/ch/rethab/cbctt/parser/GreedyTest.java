package ch.rethab.cbctt.parser;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.ea.initializer.GreedyInitializer;
import ch.rethab.cbctt.validator.UD1Validator;
import ch.rethab.cbctt.validator.Validator;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Reto Habluetzel, 2015
 */
public class GreedyTest {

    GreedyInitializer greedy = new GreedyInitializer();

    @Test
    public void shouldProduceValidTimetable() {
        Specification toySpec = testData();
        Validator v = new UD1Validator(toySpec);

        for (Timetable t : greedy.initialize(toySpec, 20)) {
            assertTrue(v.satisfiesHardConstraints(t));
        }
    }

    @Test
    public void shouldProduceCorrectAmount() {
        assertEquals(1,  greedy.initialize(testData(),  1).size());
        assertEquals(10, greedy.initialize(testData(), 10).size());
    }

    private Specification testData() {
        Course sceCosc = new Course("SceCosC", "Ocra", 3, 3, 30, true);
        Course arcTec = new Course("ArcTec", "Indaco", 3, 2, 42, false);
        Course tecCos = new Course("TecCos", "Rosa", 5, 4, 40, true);
        Course geotec = new Course("Geotec", "Scarlatti", 5, 4, 18, true);
        List<Course> courses = Arrays.asList(sceCosc, arcTec, tecCos, geotec);

        Room rA = new Room("rA", 32, 1);
        Room rB = new Room("rB", 50, 0);
        Room rC = new Room("rC", 40, 0);
        List<Room> rooms = Arrays.asList(rA, rB, rC);

        List<Curriculum> curricula = Arrays.asList(
                new Curriculum("Cur1", Arrays.asList(sceCosc, arcTec, tecCos)),
                new Curriculum("Cur2", Arrays.asList(tecCos, geotec))
        );

        UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(5, 4);
        unavailabilityConstraints.addUnavailability(tecCos, 2, 0);
        unavailabilityConstraints.addUnavailability(tecCos, 2, 1);
        unavailabilityConstraints.addUnavailability(tecCos, 3, 2);
        unavailabilityConstraints.addUnavailability(tecCos, 3, 3);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 0);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 1);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 2);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 3);

        RoomConstraints roomConstraints = new RoomConstraints();
        roomConstraints.addRoomConstraint(sceCosc, rA);
        roomConstraints.addRoomConstraint(geotec, rB);
        roomConstraints.addRoomConstraint(tecCos, rC);

        return new Specification("Toy", 5, 4, 2, 3, courses, rooms, curricula, unavailabilityConstraints, roomConstraints);
    }

}
