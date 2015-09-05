package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

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

    Course c1 = Course.Builder.id("c1").curriculum(cur1).teacher("t1").nlectures(1).nWorkingDays(1).nStudents(40).doubleLectures(true).build();
    Course c2 = Course.Builder.id("c2").curriculum(cur2).teacher("t2").nlectures(1).nWorkingDays(1).nStudents(15).doubleLectures(true).build();
    Course c3 = Course.Builder.id("c3").curriculum(cur3).teacher("t3").nlectures(1).nWorkingDays(1).nStudents(15).doubleLectures(true).build();

    Room r1 = new Room("r1", 40, 1);
    Room r2 = new Room("r2", 30, 1);
    Room r3 = new Room("r3", 14, 0);

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(days, periodsPerDay);
    RoomConstraints roomConstraints = new RoomConstraints();

    Specification spec = Specification.Builder.name("spec1")
                                              .days(days)
                                              .periodsPerDay(periodsPerDay)
                                              .minLectures(3)
                                              .maxLectures(5)
                                              .course(c1).course(c2).course(c3)
                                              .room(r1).room(r2).room(r3)
                                              .curriculum(cur1).curriculum(cur2).curriculum(cur3)
                                              .unavailabilityConstraints(unavailabilityConstraints)
                                              .roomConstraints(roomConstraints).build();

    RoomOccupancyConstraint roomOccupancyConstraint = new RoomOccupancyConstraint(spec);

    @Before
    public void init() {
        cur1.setCourses(Arrays.asList(c1, c2));
        cur2.setCourses(Arrays.asList(c3));
    }

    @Test
    public void shouldSucceedWithDifferentRoomsPerPeriod() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);
        builder.addMeeting(c1, r1, 0, 1);
        builder.addMeeting(c2, r2, 0, 1);
        builder.addMeeting(c3, r3, 0, 1);
        assertEquals(0, roomOccupancyConstraint.violations(builder.build()));
    }

}