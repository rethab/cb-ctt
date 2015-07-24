package ch.rethab.cbctt.parser.initializer;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.ea.initializer.Initializer;
import ch.rethab.cbctt.ea.initializer.TeacherGreedyInitializer;
import ch.rethab.cbctt.parser.ECTTParser;
import ch.rethab.cbctt.validator.UD1Validator;
import ch.rethab.cbctt.validator.Validator;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class TeacherGreedyTest {

    Initializer teacherGreedyInitializer = new TeacherGreedyInitializer();

    @Test
    public void shouldProduceValidToyTimetable() {
        Specification toySpec = testData();
        Validator v = new UD1Validator(toySpec);

        for (Timetable t : teacherGreedyInitializer.initialize(toySpec, 20)) {
            System.out.println("Toy Attempt");
            assertTrue(v.isFeasible(t));
        }
    }

    @Test
    public void shouldProduceFeasibleTimetablesForCompTests() throws IOException {
        for (int i = 1; i <= 21; i++) {
            String filename = String.format("comp%02d.ectt", i);
            System.out.println(filename);
            InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            ECTTParser parser = new ECTTParser(br);
            Specification spec = parser.parse();
            Validator v = new UD1Validator(spec);
            Timetable t = teacherGreedyInitializer.initialize(spec, 1).get(0);
            assertTrue(v.isFeasible(t));
        }
    }

    @Test
    public void shouldProduceCorrectAmount() {
        assertEquals(1, teacherGreedyInitializer.initialize(testData(), 1).size());
        assertEquals(10, teacherGreedyInitializer.initialize(testData(), 10).size());
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
