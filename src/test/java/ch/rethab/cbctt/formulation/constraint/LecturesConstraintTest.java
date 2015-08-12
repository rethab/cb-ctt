package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class LecturesConstraintTest {

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(5, 4);
    RoomConstraints roomConstraints = new RoomConstraints();

    Curriculum curr1 = new Curriculum("curr1");
    Curriculum curr2 = new Curriculum("curr2");
    Set<String> curriculua = new HashSet<>(Arrays.asList(curr1.getId(), curr2.getId()));

    Course c1 = new Course("c1", curr1.getId(), "t1", 1, 1, 40, true);
    Course c2 = new Course("c2", curr1.getId(), "t2", 2, 2, 15, true);
    Course c3 = new Course("c3", curr2.getId(), "t3", 2, 2, 15, true);

    Room r1 = new Room("r1", 40, 1);
    Room r2 = new Room("r2", 30, 1);
    Room r3 = new Room("r3", 14, 0);
    Set<String> rooms = new HashSet<>(Arrays.asList(r1.getId(), r2.getId(), r3.getId()));

    private int days = 5;
    private int periodsPerDay = 4;

    Specification spec = new Specification("spec1", days, periodsPerDay, 3, 5, Arrays.asList(c1, c2), Arrays.asList(r1, r2, r3), Arrays.asList(), unavailabilityConstraints, roomConstraints);
    LecturesConstraint lecturesConstraint = new LecturesConstraint(spec);

    @Before
    public void init() {
        curr1.setCourses(Arrays.asList(c1, c2));
    }

    @Test
    public void shouldAddTestWithMoreThanOneViolation() {
        fail("shouldAddTestWithMoreThanOneViolation");
    }

    @Test
    public void shouldSucceedWithLecturesAtDifferentPeriod() {
        Timetable t = new Timetable(curriculua, rooms, days, periodsPerDay);
        // c1 consists of one lecture
        t.addMeeting(new Meeting(c1, r3, 0, 1));

        // c3 consists of two lectures
        t.addMeeting(new Meeting(c3, r1, 0, 1));
        t.addMeeting(new Meeting(c3, r2, 1, 1));
        assertEquals(0, lecturesConstraint.violations(t));
    }

    @Test
    public void shouldCountMissingLecturesOfSameCourse() {
        Timetable t = new Timetable(curriculua, rooms, days, periodsPerDay);
        // course 1 consists of 1 lecture
        t.addMeeting(new Meeting(c1, r1, 0, 0));
        // course 2 consists of 2 lectures
        t.addMeeting(new Meeting(c2, r1, 0, 1));
        assertEquals(1, lecturesConstraint.violations(t));
    }

    @Test
    public void shouldCountMissingLecturesOfDifferentCourse() {
        Timetable t = new Timetable(curriculua, rooms, days, periodsPerDay);
        // course 1 consists of 1 lecture: missing entirely
        // course 2 consists of 2 lectures
        t.addMeeting(new Meeting(c2, r1, 0, 1));
        assertEquals(2, lecturesConstraint.violations(t));
    }


}