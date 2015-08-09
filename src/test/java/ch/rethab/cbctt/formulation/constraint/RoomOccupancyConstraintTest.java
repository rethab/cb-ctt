package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class RoomOccupancyConstraintTest {

    Course c1, c2, c3;

    Curriculum cur1 = new Curriculum("curr1");
    Curriculum cur2 = new Curriculum("curr2");

    List<Curriculum> curricula = Arrays.asList(cur1, cur2);

    Room r1 = new Room("r1", 40, 1);
    Room r2 = new Room("r2", 30, 1);
    Room r3 = new Room("r3", 14, 0);

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(5, 4);
    RoomConstraints roomConstraints = new RoomConstraints();

    Specification spec = new Specification("spec1", 5, 4, 3, 5, Arrays.asList(c1, c2, c3), Arrays.asList(r1, r2, r3), Arrays.asList(cur1, cur2), unavailabilityConstraints, roomConstraints);
    RoomOccupancyConstraint roomOccupancyConstraint = new RoomOccupancyConstraint(spec);

    @Before
    public void init() {
        cur1.setCourses(Arrays.asList(c1, c2));
        cur2.setCourses(Arrays.asList(c3));

        c1 = new Course("c1", cur1, "t1", 1, 1, 40, true);
        c2 = new Course("c2", cur1, "t2", 1, 1, 15, true);
        c3 = new Course("c3", cur2, "t3", 1, 1, 15, true);

    }

    @Test
    public void shouldFailWithTwoLecturesInSameRoomAtSamePeriod() {
        Timetable t = new Timetable(curricula, );
        t.addMeeting(new Meeting(c1, r1, 0, 1));
        t.addMeeting(new Meeting(c2, r2, 0, 1));
        t.addMeeting(new Meeting(c3, r1, 0, 1));
        assertTrue(roomOccupancyConstraint.violations(t) > 0);
    }

    @Test
    public void shouldSucceedWithDifferentRoomsPerPeriod() {
        Timetable t = new Timetable(curricula);
        t.addMeeting(new Meeting(c1, r1, 0, 1));
        t.addMeeting(new Meeting(c2, r2, 0, 1));
        t.addMeeting(new Meeting(c3, r3, 0, 1));
        assertEquals(0, roomOccupancyConstraint.violations(t));
    }

}