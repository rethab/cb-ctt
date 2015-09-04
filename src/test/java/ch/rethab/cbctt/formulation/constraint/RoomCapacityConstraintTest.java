package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
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

    Course c3 = Course.Builder.id("c3").curriculum(curr1).teacher("t1").nlectures(5).nWorkingDays(5).nStudents(3).doubleLectures(false).build();
    Course c7 = Course.Builder.id("c7").curriculum(curr1).teacher("t2").nlectures(3).nWorkingDays(2).nStudents(7).doubleLectures(false).build();
    Course c11 = Course.Builder.id("c11").curriculum(curr2).teacher("t3").nlectures(2).nWorkingDays(1).nStudents(11).doubleLectures(false).build();

    Room r2 = new Room("r2", 2, 1);
    Room r7 = new Room("r7", 7, 1);
    Room r12 = new Room("r12", 12, 1);
    Set<String> rooms = new HashSet<>(Arrays.asList(r2.getId(), r7.getId(), r12.getId()));

    RoomCapacityConstraint roomCapacityConstraint = new RoomCapacityConstraint();

    Specification spec = Specification.Builder.name("spec")
            .curriculum(curr1).curriculum(curr2)
            .course(c3).course(c7).course(c11)
            .room(r2).room(r7).room(r12)
            .days(days).periodsPerDay(periodsPerDay)
            .build();

    @Test
    public void shouldSumUpViolations() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c7, r2, 0, 0); // 5 violations
        builder.addMeeting(c7, r7, 0, 1);
        builder.addMeeting(c7, r12, 0, 2);
        builder.addMeeting(c7, r2, 1, 1); // 5 violations
        builder.addMeeting(c7, r2, 2, 1); // 5 violation

        assertEquals(15, roomCapacityConstraint.violations(builder.build()));
    }

    @Test
    public void shouldSumUpViolationsFromDifferentCurricula() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c3, r2, 0, 0); // 1 violation
        builder.addMeeting(c7, r7, 0, 1);
        builder.addMeeting(c7, r2, 1, 1); // 5 violation
        builder.addMeeting(c11, r7, 1, 1); // 4 violation
        builder.addMeeting(c11, r12, 1, 2);

        assertEquals(10, roomCapacityConstraint.violations(builder.build()));
    }

    @Test
    public void shouldCountZeroIfAllFitIn() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c3, r7, 0, 0);
        builder.addMeeting(c7, r7, 0, 1);
        builder.addMeeting(c7, r12, 1, 1);
        builder.addMeeting(c11, r12, 0, 1);
        builder.addMeeting(c11, r12, 1, 2);

        assertEquals(0, roomCapacityConstraint.violations(builder.build()));
    }

}