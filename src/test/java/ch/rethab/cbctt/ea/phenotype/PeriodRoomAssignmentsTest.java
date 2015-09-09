package ch.rethab.cbctt.ea.phenotype;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.parser.ECTTParser;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class PeriodRoomAssignmentsTest {

    int day = 1;
    int period = 1;

    RoomConstraints roomConstraints = new RoomConstraints();

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(day, period);

    Room r1 = new Room("r1", 0, 1);
    Room r2 = new Room("r2", 1, 1);
    Room r3 = new Room("r3", 2, 1);
    Room r4 = new Room("r4", 3, 1);

    Curriculum cur1 = new Curriculum("curr1");
    Curriculum cur2 = new Curriculum("curr2");
    Curriculum cur3 = new Curriculum("curr3");
    Curriculum cur4 = new Curriculum("curr4");

    Course c1 = Course.Builder.id("c1")
            .curriculum(cur1)
            .teacher("t1").doubleLectures(false).nWorkingDays(2)
            .nStudents(4)
            .build();

    Course c2 = Course.Builder.id("c2")
            .curriculum(cur2)
            .teacher("t2").doubleLectures(false).nWorkingDays(2)
            .nStudents(4)
            .build();

    Course c3 = Course.Builder.id("c3")
            .curriculum(cur3)
            .teacher("t3").doubleLectures(false).nWorkingDays(2)
            .nStudents(4)
            .build();

    Course c4 = Course.Builder.id("c4")
            .curriculum(cur4)
            .teacher("t4").doubleLectures(false).nWorkingDays(2)
            .nStudents(4)
            .build();

    Specification spec = Specification.Builder.name("spec")
            .days(day).periodsPerDay(period)
            .room(r1).room(r2).room(r3).room(r4)
            .course(c1).course(c2).course(c3).course(c4)
            .curriculum(cur1).curriculum(cur2).curriculum(cur3).curriculum(cur4)
            .minLectures(1).maxLectures(2)
            .roomConstraints(roomConstraints)
            .unavailabilityConstraints(unavailabilityConstraints)
            .build();

    @Test
    public void shouldUpdateRoomConstrainednessDuringAssignment() {
        PeriodRoomAssignments pra = new PeriodRoomAssignments(spec);
        /* Idea: (c=constraint, f=free, o=occupied by other, r=rooms)
         *
         *  r = 1 2 3 4
         * c1 = c c c f --> 1
         * c2 = c c f f --> 2
         * c3 = f f c c --> 2
         * c4 = c f f f --> 3
         *
         * --> c4 is easiest to assignRooms in the beginning, but
         *     after c1 and c2 have been scheduled, c4 will be
         *     harder to assignRooms than c3.
         *     this means c4 must get a better room than c3.
         */

        roomConstraints.addRoomConstraint(c1, r1);
        roomConstraints.addRoomConstraint(c1, r2);
        roomConstraints.addRoomConstraint(c1, r3);
        roomConstraints.addRoomConstraint(c2, r1);
        roomConstraints.addRoomConstraint(c2, r2);
        roomConstraints.addRoomConstraint(c3, r3);
        roomConstraints.addRoomConstraint(c3, r4);
        roomConstraints.addRoomConstraint(c4, r1);

        // all courses have too many students. since r1 is the smallest,
        // whichever course is assigned to that, was scheduled last
        assertTrue(pra.add(c1));
        assertTrue(pra.add(c2));
        assertTrue(pra.add(c3));
        assertTrue(pra.add(c4));

        List<PeriodRoomAssignments.CourseWithRoom> courses = pra.assignRooms();
        assertEquals(4, courses.size());
        Optional<PeriodRoomAssignments.CourseWithRoom> c1wr = courses.stream().filter(cwr -> cwr.course.equals(c1)).findFirst();
        Optional<PeriodRoomAssignments.CourseWithRoom> c2wr = courses.stream().filter(cwr -> cwr.course.equals(c2)).findFirst();
        Optional<PeriodRoomAssignments.CourseWithRoom> c3wr = courses.stream().filter(cwr -> cwr.course.equals(c3)).findFirst();
        Optional<PeriodRoomAssignments.CourseWithRoom> c4wr = courses.stream().filter(cwr -> cwr.course.equals(c4)).findFirst();
        assertEquals(r4, c1wr.get().room);
        assertEquals(r3, c2wr.get().room);
        assertEquals(r2, c4wr.get().room);
        assertEquals(r1, c3wr.get().room);
    }

    @Test
    public void shouldBeAbleToRemoveAndReAdd() {
        PeriodRoomAssignments pra = new PeriodRoomAssignments(spec);
        assertTrue(pra.add(c1));
        pra.remove(c1);
        assertTrue(pra.add(c1));
        assertEquals(c1, pra.assignRooms().stream().map(cwr -> cwr.course).findFirst().get());
    }

    @Test
    public void shouldNotBeAbletoAddCourseWithRoomConstraint() {
        PeriodRoomAssignments pra = new PeriodRoomAssignments(spec);
        roomConstraints.addRoomConstraint(c1, r1);
        roomConstraints.addRoomConstraint(c1, r2);
        roomConstraints.addRoomConstraint(c1, r3);
        roomConstraints.addRoomConstraint(c1, r4);
        assertFalse(pra.add(c1));
        assertTrue(pra.assignRooms().isEmpty());
    }

    @Test
    public void shouldNotBeAbleToAddSecondCourseWithRoomConstraint() {
        PeriodRoomAssignments pra = new PeriodRoomAssignments(spec);
        roomConstraints.addRoomConstraint(c1, r1);
        roomConstraints.addRoomConstraint(c2, r1);
        roomConstraints.addRoomConstraint(c1, r2);
        roomConstraints.addRoomConstraint(c2, r2);
        roomConstraints.addRoomConstraint(c1, r3);
        roomConstraints.addRoomConstraint(c2, r3);
        assertTrue(pra.add(c1)); // will be assigned to r4
        assertFalse(pra.add(c2)); // no more are free now

        List<PeriodRoomAssignments.CourseWithRoom> cwr = pra.assignRooms();
        assertEquals(1, cwr.size());
        assertEquals(c1, cwr.get(0).course);
        assertEquals(r4, cwr.get(0).room);
    }

    @Test
    public void shouldNotBeAbleToAddTooManyCourses() {
        PeriodRoomAssignments pra = new PeriodRoomAssignments(spec);
        assertTrue(pra.add(c1));
        assertTrue(pra.add(c2));
        assertTrue(pra.add(c3));
        assertTrue(pra.add(c4));

        Course c5 = Course.Builder.id("c5").curriculum(cur1).doubleLectures(false)
            .nlectures(1).nStudents(3).nWorkingDays(3).teacher("t5").build();
        assertFalse(pra.add(c5)); // we have only four rooms
    }

    @Test
    public void shouldAssignAllCourses() {
        PeriodRoomAssignments pra = new PeriodRoomAssignments(spec);
        assertTrue(pra.add(c1));
        assertTrue(pra.add(c2));
        assertTrue(pra.add(c3));
        assertTrue(pra.add(c4));

        List<PeriodRoomAssignments.CourseWithRoom> cwr = pra.assignRooms();
        Set<Room> rooms = cwr.stream().map(_cwr -> _cwr.room).collect(Collectors.toSet());
        for (Room room : spec.getRooms()) {
            assertTrue(rooms.contains(room));
        }
    }

    @Test
    public void shouldNotAssignToConstraints() {
        PeriodRoomAssignments pra = new PeriodRoomAssignments(spec);

        // c1 can only be assigned to r4
        roomConstraints.addRoomConstraint(c1, r1);
        roomConstraints.addRoomConstraint(c1, r2);
        roomConstraints.addRoomConstraint(c1, r3);
        // c2 can only be assigned to r3
        roomConstraints.addRoomConstraint(c2, r1);
        roomConstraints.addRoomConstraint(c2, r2);
        roomConstraints.addRoomConstraint(c2, r4);
        // c3 can only be assigned to r2
        roomConstraints.addRoomConstraint(c3, r1);
        roomConstraints.addRoomConstraint(c3, r3);
        roomConstraints.addRoomConstraint(c3, r4);
        // c4 can only be assigned to r1
        roomConstraints.addRoomConstraint(c4, r2);
        roomConstraints.addRoomConstraint(c4, r3);
        roomConstraints.addRoomConstraint(c4, r4);

        assertTrue(pra.add(c1));
        assertTrue(pra.add(c2));
        assertTrue(pra.add(c3));
        assertTrue(pra.add(c4));

        List<PeriodRoomAssignments.CourseWithRoom> cwr = pra.assignRooms();
        assertEquals(r4, cwr.stream().filter(_cwr -> _cwr.course.equals(c1)).findFirst().get().room);
        assertEquals(r3, cwr.stream().filter(_cwr -> _cwr.course.equals(c2)).findFirst().get().room);
        assertEquals(r2, cwr.stream().filter(_cwr -> _cwr.course.equals(c3)).findFirst().get().room);
        assertEquals(r1, cwr.stream().filter(_cwr -> _cwr.course.equals(c4)).findFirst().get().room);
    }

    @Test
    public void shouldBeAbleToAddAndRemove() {
        PeriodRoomAssignments pra = new PeriodRoomAssignments(spec);
        assertTrue(pra.add(c1));
        assertTrue(pra.add(c2));
        assertTrue(pra.add(c3));
        assertTrue(pra.add(c4));
        pra.remove(c1);
        List<Course> courses = pra.assignRooms().stream().map(cwr -> cwr.course).collect(Collectors.toList());
        assertEquals(3, courses.size());
        assertTrue(courses.contains(c2));
        assertTrue(courses.contains(c3));
        assertTrue(courses.contains(c4));

        pra = new PeriodRoomAssignments(spec);
        pra.add(c1); pra.add(c2); pra.add(c3); pra.add(c4);
        pra.remove(c2);
        courses = pra.assignRooms().stream().map(cwr -> cwr.course).collect(Collectors.toList());
        assertEquals(3, courses.size());
        assertTrue(courses.contains(c1));
        assertTrue(courses.contains(c3));
        assertTrue(courses.contains(c4));

        pra = new PeriodRoomAssignments(spec);
        pra.add(c1); pra.add(c2); pra.add(c3); pra.add(c4);
        pra.remove(c3);
        courses = pra.assignRooms().stream().map(cwr -> cwr.course).collect(Collectors.toList());
        assertEquals(3, courses.size());
        assertTrue(courses.contains(c1));
        assertTrue(courses.contains(c2));
        assertTrue(courses.contains(c4));

        pra = new PeriodRoomAssignments(spec);
        pra.add(c1); pra.add(c2); pra.add(c3); pra.add(c4);
        pra.remove(c4);
        courses = pra.assignRooms().stream().map(cwr -> cwr.course).collect(Collectors.toList());
        assertEquals(3, courses.size());
        assertTrue(courses.contains(c1));
        assertTrue(courses.contains(c2));
        assertTrue(courses.contains(c3));
    }

    @Test
    public void shouldBeAbleToConstructTimetableRegressionTest() throws IOException {
        /*         rB    rC    rE     rF   rG    rS     violations
         * c0002 = -125   null  66     45   55    45    5
         * c0066 = -186  -86    null  -16  -6    -16    5
         * c0059 = -193  -93    null  -23  -13   -23    5
         * c0061 = -194  -94    null  -24   null -24    4
         * c0031 = -189  -89    2     -19  -9     null  5
         * c0058 = -198  -98   -7     -28  -18    null  5
         */

        InputStream is = getClass().getClassLoader().getResourceAsStream("comp01.ectt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ECTTParser parser = new ECTTParser(br);
        Specification spec = parser.parse();

        Course c0002 = spec.getCourses().stream().filter(c -> c.getId().equals("c0002")).findFirst().get();
        Course c0066 = spec.getCourses().stream().filter(c -> c.getId().equals("c0066")).findFirst().get();
        Course c0059 = spec.getCourses().stream().filter(c -> c.getId().equals("c0059")).findFirst().get();
        Course c0061 = spec.getCourses().stream().filter(c -> c.getId().equals("c0061")).findFirst().get();
        Course c0031 = spec.getCourses().stream().filter(c -> c.getId().equals("c0031")).findFirst().get();
        Course c0058 = spec.getCourses().stream().filter(c -> c.getId().equals("c0058")).findFirst().get();

        PeriodRoomAssignments pra = new PeriodRoomAssignments(spec);
        assertTrue(pra.add(c0002));
        assertTrue(pra.add(c0066));
        assertTrue(pra.add(c0059));
        assertTrue(pra.add(c0061));
        assertTrue(pra.add(c0031));
        assertTrue(pra.add(c0058));

        assertEquals(6, pra.assignRooms().size());
    }

}