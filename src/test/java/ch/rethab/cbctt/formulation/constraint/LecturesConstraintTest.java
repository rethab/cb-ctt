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
    Set<String> curriculua = new HashSet<>(Arrays.asList(curr1.getId()));

    Course c1 = new Course("c1", "t1", 1, 1, 40, true);
    Course c2 = new Course("c2", "t2", 2, 2, 15, true);

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
    public void shouldFailWithLecturesOfSameCourseAtSamePeriod() {
        Timetable t = new Timetable(curriculua, rooms, days, periodsPerDay);
        t.addMeeting(new Meeting(c1, r3, 0, 1));

        // two lectures of same course at same period
        t.addMeeting(new Meeting(c2, r1, 0, 1));
        t.addMeeting(new Meeting(c2, r2, 0, 1));
        assertEquals(1, lecturesConstraint.violations(t));
    }

    @Test
    public void shouldSucceedWithLecturesAtDifferentPeriod() {
        Timetable t = new Timetable(curriculua, rooms, days, periodsPerDay);
        t.addMeeting(new Meeting(c1, r3, 0, 1));

        t.addMeeting(new Meeting(c2, r1, 0, 1));
        t.addMeeting(new Meeting(c2, r2, 1, 1));
        assertEquals(0, lecturesConstraint.violations(t));
    }

    @Test
    public void shouldFailSinceNotAllLecturesScheduled() {
        Timetable t = new Timetable(curriculua, rooms, days, periodsPerDay);
        t.addMeeting(new Meeting(c1, r3, 0, 1));

        // course 2 consists of 2 lectures
        t.addMeeting(new Meeting(c2, r1, 0, 1));
        assertEquals(1, lecturesConstraint.violations(t));
    }


}