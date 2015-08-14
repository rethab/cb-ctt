package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class ConflictsConstraintTest {

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(5, 4);
    RoomConstraints roomConstraints = new RoomConstraints();

    Curriculum cur1 = new Curriculum("curr1");
    Curriculum cur2 = new Curriculum("curr2");
    Set<String> curricula = new HashSet<>(Arrays.asList(cur1.getId(), cur2.getId()));

    Course c1 = Course.Builder.id("c1").curriculum(cur1).teacher("t1").nlectures(1).nWorkingDays(1).nStudents(40).doubleLectures(true).build();
    Course c2 = Course.Builder.id("c2").curriculum(cur1).teacher("t2").nlectures(1).nWorkingDays(1).nStudents(15).doubleLectures(true).build();
    Course c3 = Course.Builder.id("c3").curriculum(cur2).teacher("t3").nlectures(1).nWorkingDays(1).nStudents(15).doubleLectures(true).build();
    Course c4 = Course.Builder.id("c4").curriculum(cur2).teacher("t2").nlectures(1).nWorkingDays(1).nStudents(15).doubleLectures(true).build();

    Room r1 = new Room("r1", 40, 1);
    Room r2 = new Room("r2", 30, 1);
    Room r3 = new Room("r3", 14, 0);
    Set<String> rooms = new HashSet<>(Arrays.asList(r1.getId(), r2.getId(), r3.getId()));

    private int days = 5;
    private int periodsPerDay = 4;

    Specification spec = Specification.Builder.name("spec1")
            .days(days).periodsPerDay(periodsPerDay)
            .minLectures(3).maxLectures(5)
            .course(c1).course(c2).course(c3).course(c4)
            .room(r1).room(r2).room(r3)
            .curriculum(cur1).curriculum(cur2)
            .unavailabilityConstraints(unavailabilityConstraints)
            .roomConstraints(roomConstraints).build();

    ConflictsConstraint conflictsConstraint = new ConflictsConstraint(spec);

    @Before
    public void init() {
        cur1.setCourses(Arrays.asList(c1, c2));
        cur2.setCourses(Arrays.asList(c3, c4));
    }

    @Test
    public void shouldSuccessWithAllOk() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);
        t.addMeeting(new Meeting(c1, r3, 0, 1));
        t.addMeeting(new Meeting(c2, r1, 0, 2));
        t.addMeeting(new Meeting(c3, r2, 2, 1));
        t.addMeeting(new Meeting(c4, r2, 1, 1));
        assertEquals(0, conflictsConstraint.violations(t));

    }

}