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
public class RoomCapacityConstraintTest {

    int days = 5;
    int periodsPerDay = 5;

    Curriculum curr1 = new Curriculum("curr1");
    Curriculum curr2 = new Curriculum("curr2");
    Set<String> curricula = new HashSet<>(Arrays.asList(curr1.getId(), curr2.getId()));

    Course c3 = new Course("c3", curr1.getId(), "t1", 5, 5, 3, false);
    Course c7 = new Course("c7", curr1.getId(), "t2", 3, 2, 7, false);
    Course c11 = new Course("c11", curr2.getId(), "t3", 2, 1, 11, false);

    Room r2 = new Room("r2", 2, 1);
    Room r7 = new Room("r7", 7, 1);
    Room r12 = new Room("r12", 12, 1);
    Set<String> rooms = new HashSet<>(Arrays.asList(r2.getId(), r7.getId(), r12.getId()));

    RoomCapacityConstraint roomCapacityConstraint = new RoomCapacityConstraint();

    @Test
    public void shouldSumUpViolations() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c7, r2, 0, 0)); // violation
        t.addMeeting(new Meeting(c7, r7, 0, 1));
        t.addMeeting(new Meeting(c7, r12, 0, 2));
        t.addMeeting(new Meeting(c7, r2, 1, 1)); // violation
        t.addMeeting(new Meeting(c7, r2, 2, 1)); // violation

        assertEquals(3, roomCapacityConstraint.violations(t));
    }

    @Test
    public void shouldSumUpViolationsFromDifferentCurricula() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c3, r2, 0, 0)); // violation
        t.addMeeting(new Meeting(c7, r7, 0, 1));
        t.addMeeting(new Meeting(c7, r2, 1, 1)); // violation
        t.addMeeting(new Meeting(c11, r7, 1, 1)); // violation
        t.addMeeting(new Meeting(c11, r12, 1, 2));

        assertEquals(3, roomCapacityConstraint.violations(t));
    }

    @Test
    public void shouldCountZeroIfAllFitIn() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c3, r7, 0, 0));
        t.addMeeting(new Meeting(c7, r7, 0, 1));
        t.addMeeting(new Meeting(c7, r12, 1, 1));
        t.addMeeting(new Meeting(c11, r12, 0, 1));
        t.addMeeting(new Meeting(c11, r12, 1, 2));

        assertEquals(0, roomCapacityConstraint.violations(t));
    }

}