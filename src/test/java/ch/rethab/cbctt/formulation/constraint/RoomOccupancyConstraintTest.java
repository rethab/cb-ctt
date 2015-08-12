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
public class RoomOccupancyConstraintTest {

    int days = 5;
    int periodsPerDay = 4;

    Curriculum cur1 = new Curriculum("curr1");
    Curriculum cur2 = new Curriculum("curr2");
    Curriculum cur3 = new Curriculum("curr3");
    Set<String> curricula = new HashSet(Arrays.asList(cur1.getId(), cur2.getId(), cur3.getId()));

    Course c1 = new Course("c1", cur1.getId(), "t1", 1, 1, 40, true);
    Course c2 = new Course("c2", cur2.getId(), "t2", 1, 1, 15, true);
    Course c3 = new Course("c3", cur3.getId(), "t3", 1, 1, 15, true);

    Room r1 = new Room("r1", 40, 1);
    Room r2 = new Room("r2", 30, 1);
    Room r3 = new Room("r3", 14, 0);
    Set<String> rooms = new HashSet<>(Arrays.asList(r1.getId(), r2.getId(), r3.getId()));

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(days, periodsPerDay);
    RoomConstraints roomConstraints = new RoomConstraints();

    Specification spec = new Specification("spec1", days, periodsPerDay, 3, 5, Arrays.asList(c1, c2, c3), Arrays.asList(r1, r2, r3), Arrays.asList(cur1, cur2, cur3), unavailabilityConstraints, roomConstraints);
    RoomOccupancyConstraint roomOccupancyConstraint = new RoomOccupancyConstraint(spec);

    @Before
    public void init() {
        cur1.setCourses(Arrays.asList(c1, c2));
        cur2.setCourses(Arrays.asList(c3));
    }

    @Test
    public void shouldSucceedWithDifferentRoomsPerPeriod() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);
        t.addMeeting(new Meeting(c1, r1, 0, 1));
        t.addMeeting(new Meeting(c2, r2, 0, 1));
        t.addMeeting(new Meeting(c3, r3, 0, 1));
        assertEquals(0, roomOccupancyConstraint.violations(t));
    }

}