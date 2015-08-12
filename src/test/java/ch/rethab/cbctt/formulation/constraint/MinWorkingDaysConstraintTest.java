package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class MinWorkingDaysConstraintTest {

    int days = 5;
    int periodsPerDay = 5;

    Curriculum curr1 = new Curriculum("curr1");
    Curriculum curr2 = new Curriculum("curr2");
    Curriculum curr3 = new Curriculum("curr3");
    Set<String> curricula = new HashSet<>(Arrays.asList(curr1.getId(), curr2.getId(), curr3.getId()));

    Course c1 = new Course("c1", curr1.getId(), "t1", 5, 5, 1, false);
    Course c2 = new Course("c2", curr2.getId(), "t2", 3, 2, 1, false);
    Course c3 = new Course("c3", curr3.getId(), "t3", 2, 1, 1, false);

    Room r1 = new Room("r1", 1, 1);
    Room r2 = new Room("r2", 1, 1);
    Room r3 = new Room("r3", 1, 1);
    Set<String> rooms = new HashSet<>(Arrays.asList(r1.getId(), r2.getId(), r3.getId()));

    MinWorkingDaysConstraint minWorkingDaysConstraint = new MinWorkingDaysConstraint();

    @Test
    public void shouldFindViolationInDifferentRoom() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        // c1 min 5 days. actual 4 days --> 1 violation
        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r2, 0, 1));
        t.addMeeting(new Meeting(c1, r1, 2, 0));
        t.addMeeting(new Meeting(c1, r1, 3, 0));
        t.addMeeting(new Meeting(c1, r1, 4, 0));

        assertEquals(1, minWorkingDaysConstraint.violations(t));
    }

    @Test
    public void shouldSumUpMultipleViolationsForSameCourse() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        // c1 min 5 days. actual 1 day --> 4 violations
        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r2, 0, 1)); // violation
        t.addMeeting(new Meeting(c1, r1, 0, 2)); // violation
        t.addMeeting(new Meeting(c1, r2, 0, 3)); // violation
        t.addMeeting(new Meeting(c1, r3, 0, 4)); // violation

        assertEquals(4, minWorkingDaysConstraint.violations(t));
    }

    @Test
    public void shouldSumUpViolationsFromAllCourses() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        // c1 min 5 days. actual 1 day --> 4 violations
        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r1, 0, 1));
        t.addMeeting(new Meeting(c1, r2, 0, 2));
        t.addMeeting(new Meeting(c1, r1, 0, 3));
        t.addMeeting(new Meeting(c1, r3, 0, 4));

        // c2 min 2 days. actual 1 day --> 1 violation
        t.addMeeting(new Meeting(c2, r2, 1, 0));
        t.addMeeting(new Meeting(c2, r2, 1, 1));
        t.addMeeting(new Meeting(c2, r2, 1, 2));

        // c3 min 1 day. actual 1 day --> 0 violations
        t.addMeeting(new Meeting(c3, r3, 2, 0));
        t.addMeeting(new Meeting(c3, r3, 2, 1));

        assertEquals(5, minWorkingDaysConstraint.violations(t));
    }

    @Test
    public void shouldReturnZeroIfNoViolations() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        // c1 min 5 days
        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r1, 1, 0));
        t.addMeeting(new Meeting(c1, r1, 2, 0));
        t.addMeeting(new Meeting(c1, r1, 3, 0));
        t.addMeeting(new Meeting(c1, r1, 4, 0));

        // c2 min 2 days
        t.addMeeting(new Meeting(c2, r2, 0, 0));
        t.addMeeting(new Meeting(c2, r2, 0, 1));
        t.addMeeting(new Meeting(c2, r2, 1, 0));

        // c3 min 1 day
        t.addMeeting(new Meeting(c3, r3, 0, 0));
        t.addMeeting(new Meeting(c3, r3, 0, 1));

        assertEquals(0, minWorkingDaysConstraint.violations(t));
    }

}