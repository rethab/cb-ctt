package ch.rethab.cbctt.validator.constraint;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class RoomAvailabilityTest {

    Course c1 = new Course("c1", "t1", 1, 1, 40, true);
    Course c2 = new Course("c2", "t2", 1, 1, 15, true);
    Course c3 = new Course("c3", "t3", 1, 1, 15, true);

    Curriculum cur1 = new Curriculum("curr1", Arrays.asList(c1, c2));
    Curriculum cur2 = new Curriculum("curr2", Arrays.asList(c3));

    Room r1 = new Room("r1", 40, 1);
    Room r2 = new Room("r2", 30, 1);
    Room r3 = new Room("r3", 14, 0);

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(5, 4);
    RoomConstraints roomConstraints = new RoomConstraints();

    Specification spec = new Specification("spec1", 5, 4, 3, 5, Arrays.asList(c1, c2, c3), Arrays.asList(r1, r2, r3), Arrays.asList(cur1, cur2), unavailabilityConstraints, roomConstraints);
    TeacherAvailabilityConstraint teacherAvailabilityConstraint = new TeacherAvailabilityConstraint(spec);

    @Before
    public void init() {
        unavailabilityConstraints.addUnavailability(c1, 0, 1);
    }

    @Test
    public void shouldFailWithTeacherNotAvailable() {
        Timetable t = new Timetable();
        // teacher not available
        t.addMeeting(new Meeting(c1, r1, 0, 1));
        t.addMeeting(new Meeting(c2, r2, 0, 1));
        t.addMeeting(new Meeting(c3, r1, 0, 1));
        assertFalse(teacherAvailabilityConstraint.satisfies(t));
    }

    @Test
    public void shouldSucceedWithTeacherAvailable() {
        Timetable t = new Timetable();
        t.addMeeting(new Meeting(c1, r1, 1, 1));
        t.addMeeting(new Meeting(c2, r2, 0, 1));
        t.addMeeting(new Meeting(c3, r3, 0, 1));
        assertTrue(teacherAvailabilityConstraint.satisfies(t));
    }

}