package ch.rethab.cbctt.parser;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.domain.Specification;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

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


        Room rA = new Room("rA", 32, 1);
        Room rB = new Room("rB", 50, 0);
        Room rC = new Room("rC", 40, 0);
        List<Room> rooms = Arrays.asList(rA, rB, rC);
        assertEqualsList(rooms, specification.getRooms());

        Curriculum cur1 = new Curriculum("Cur1");
        Curriculum cur2 = new Curriculum("Cur2");

        // courses
        Course sceCosc = Course.Builder.id("SceCosC").curriculum(cur1).teacher("Ocra").nlectures(3).nWorkingDays(3).nStudents(30).doubleLectures(true).build();
        Course arcTec = Course.Builder.id("ArcTec").curriculum(cur1).teacher("Indaco").nlectures(3).nWorkingDays(2).nStudents(42).doubleLectures(false).build();
        Course tecCos = Course.Builder.id("TecCos").curriculum(cur1).curriculum(cur2).teacher("Rosa").nlectures(5).nWorkingDays(4).nStudents(40).doubleLectures(true).build();
        Course geotec = Course.Builder.id("Geotec").curriculum(cur2).teacher("Scarlatti").nlectures(5).nWorkingDays(4).nStudents(18).doubleLectures(true).build();
        List<Course> courses = Arrays.asList(sceCosc, arcTec, tecCos, geotec);
        assertEqualsList(courses, specification.getCourses());

        // curricula
        cur1.setCourses(Arrays.asList(sceCosc, arcTec, tecCos));
        cur2.setCourses(Arrays.asList(tecCos, geotec));
        List<Curriculum> curricula = Arrays.asList(cur1, cur2);
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
