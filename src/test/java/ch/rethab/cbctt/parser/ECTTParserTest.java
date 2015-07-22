package ch.rethab.cbctt.parser;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.ECTT;
import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.domain.constraint.RoomConstraint;
import ch.rethab.cbctt.domain.constraint.UnavailabilityConstraint;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Reto Habluetzel, 2015
 */
public class ECTTParserTest {

    @Test
    public void shouldSetTheAttributes() throws IOException {
        InputStream is = ECTTParserTest.class.getClassLoader().getResourceAsStream("toy.ectt");
        if (is == null) {
            throw new FileNotFoundException("toy.ectt");
        }
        ECTTParser parser = new ECTTParser(new BufferedReader(new InputStreamReader(is)));
        ECTT ectt = parser.parse();

        assertEquals("Toy", ectt.getName());
        assertEquals(5, ectt.getNumberOfDaysPerWeek());
        assertEquals(4, ectt.getPeriodsPerDay());
        assertEquals(2, ectt.getMinLectures());
        assertEquals(3, ectt.getMaxLectures());

        Course sceCosc = new Course("SceCosC", "Ocra", 3, 3, 30, true);
        Course arcTec = new Course("ArcTec", "Indaco", 3, 2, 42, false);
        Course tecCos = new Course("TecCos", "Rosa", 5, 4, 40, true);
        Course geotec = new Course("Geotec", "Scarlatti", 5, 4, 18, true);
        List<Course> courses = Arrays.asList(sceCosc, arcTec, tecCos, geotec);
        assertEqualsList(courses, ectt.getCourses());

        Room rA = new Room("rA", 32, 1);
        Room rB = new Room("rB", 50, 0);
        Room rC = new Room("rC", 40, 0);
        List<Room> rooms = Arrays.asList(rA, rB, rC);
        assertEqualsList(rooms, ectt.getRooms());

        List<Curriculum> curricula = Arrays.asList(
            new Curriculum("Cur1", Arrays.asList(sceCosc, arcTec, tecCos)),
            new Curriculum("Cur2", Arrays.asList(tecCos, geotec))
        );
        assertEqualsList(curricula, ectt.getCurricula());

        List<UnavailabilityConstraint> unavailabilityConstraints = Arrays.asList(
            new UnavailabilityConstraint(tecCos, 2, 0),
            new UnavailabilityConstraint(tecCos, 2, 1),
            new UnavailabilityConstraint(tecCos, 3, 2),
            new UnavailabilityConstraint(tecCos, 3, 3),
            new UnavailabilityConstraint(arcTec, 4, 0),
            new UnavailabilityConstraint(arcTec, 4, 1),
            new UnavailabilityConstraint(arcTec, 4, 2),
            new UnavailabilityConstraint(arcTec, 4, 3)
        );
        assertEqualsList(unavailabilityConstraints, ectt.getUnavailabilityConstraints());

        List<RoomConstraint> roomConstraints = Arrays.asList(
            new RoomConstraint(sceCosc, rA),
            new RoomConstraint(geotec, rB),
            new RoomConstraint(tecCos, rC)
        );
        assertEqualsList(roomConstraints, ectt.getRoomConstraints());
    }

    private void assertEqualsList(List<?> expected, List<?> actual) {
        assertEquals("size is not equal", expected.size(), actual.size());
        for (Object o : expected) {
            assertTrue("Expected " + o + " in " + expected, actual.contains(o));
        }
    }

}
