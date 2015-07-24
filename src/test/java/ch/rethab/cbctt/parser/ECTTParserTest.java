package ch.rethab.cbctt.parser;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.domain.Room;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Reto Habluetzel, 2015
 */
public class ECTTParserTest {

    @Test
    public void shouldProduceFeasibleTimetablesForCompTests() throws IOException {
        for (int i = 1; i <= 21; i++) {
            String filename = String.format("comp%02d.ectt", i);
            InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            ECTTParser parser = new ECTTParser(br);
            parser.parse();
        }
    }

    @Test
    public void shouldSetTheAttributes() throws IOException {
        InputStream is = ECTTParserTest.class.getClassLoader().getResourceAsStream("toy.ectt");
        if (is == null) {
            throw new FileNotFoundException("toy.ectt");
        }
        ECTTParser parser = new ECTTParser(new BufferedReader(new InputStreamReader(is)));
        Specification specification = parser.parse();

        assertEquals("Toy", specification.getName());
        assertEquals(5, specification.getNumberOfDaysPerWeek());
        assertEquals(4, specification.getPeriodsPerDay());
        assertEquals(2, specification.getMinLectures());
        assertEquals(3, specification.getMaxLectures());

        Course sceCosc = new Course("SceCosC", "Ocra", 3, 3, 30, true);
        Course arcTec = new Course("ArcTec", "Indaco", 3, 2, 42, false);
        Course tecCos = new Course("TecCos", "Rosa", 5, 4, 40, true);
        Course geotec = new Course("Geotec", "Scarlatti", 5, 4, 18, true);
        List<Course> courses = Arrays.asList(sceCosc, arcTec, tecCos, geotec);
        assertEqualsList(courses, specification.getCourses());

        Room rA = new Room("rA", 32, 1);
        Room rB = new Room("rB", 50, 0);
        Room rC = new Room("rC", 40, 0);
        List<Room> rooms = Arrays.asList(rA, rB, rC);
        assertEqualsList(rooms, specification.getRooms());

        List<Curriculum> curricula = Arrays.asList(
            new Curriculum("Cur1", Arrays.asList(sceCosc, arcTec, tecCos)),
            new Curriculum("Cur2", Arrays.asList(tecCos, geotec))
        );
        assertEqualsList(curricula, specification.getCurricula());

        assertFalse(specification.getUnavailabilityConstraints().checkAvailability(tecCos, 2, 0));
        assertFalse(specification.getUnavailabilityConstraints().checkAvailability(tecCos, 2, 1));
        assertFalse(specification.getUnavailabilityConstraints().checkAvailability(tecCos, 3, 2));
        assertFalse(specification.getUnavailabilityConstraints().checkAvailability(tecCos, 3, 3));
        assertFalse(specification.getUnavailabilityConstraints().checkAvailability(arcTec, 4, 0));
        assertFalse(specification.getUnavailabilityConstraints().checkAvailability(arcTec, 4, 1));
        assertFalse(specification.getUnavailabilityConstraints().checkAvailability(arcTec, 4, 2));
        assertFalse(specification.getUnavailabilityConstraints().checkAvailability(arcTec, 4, 3));

        assertTrue(specification.getRoomConstraints().isUnsuitable(sceCosc, rA));
        assertTrue(specification.getRoomConstraints().isUnsuitable(geotec, rB));
        assertTrue(specification.getRoomConstraints().isUnsuitable(tecCos, rC));
    }

    private void assertEqualsList(List<?> expected, List<?> actual) {
        assertEquals("size is not equal", expected.size(), actual.size());
        for (Object o : expected) {
            assertTrue("Expected " + o + " in " + expected, actual.contains(o));
        }
    }

}
