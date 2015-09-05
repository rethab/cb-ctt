package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import org.junit.Test;

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

    Course c1 = Course.Builder.id("c1").curriculum(curr1).teacher("t1").nlectures(5).nWorkingDays(5).nStudents(1).doubleLectures(false).build();
    Course c2 = Course.Builder.id("c2").curriculum(curr2).teacher("t2").nlectures(3).nWorkingDays(2).nStudents(1).doubleLectures(false).build();
    Course c3 = Course.Builder.id("c3").curriculum(curr3).teacher("t3").nlectures(2).nWorkingDays(1).nStudents(1).doubleLectures(false).build();

    Room r1 = new Room("r1", 1, 1);
    Room r2 = new Room("r2", 1, 1);
    Room r3 = new Room("r3", 1, 1);

    Specification spec = Specification.Builder.name("TestSpec").days(5).periodsPerDay(5)
            .minLectures(2).maxLectures(3)
            .curriculum(curr1).curriculum(curr2).curriculum(curr3)
            .course(c1).course(c2).course(c3)
            .room(r1).room(r2).room(r3)
            .roomConstraints(new RoomConstraints())
            .unavailabilityConstraints(new UnavailabilityConstraints(days, periodsPerDay)).build();

    MinWorkingDaysConstraint minWorkingDaysConstraint = new MinWorkingDaysConstraint(spec);

    @Test
    public void shouldFindViolationInDifferentRoom() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        // c1 min 5 days. actual 4 days --> 1 violation
        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r2, 0, 1);
        builder.addMeeting(c1, r1, 2, 0);
        builder.addMeeting(c1, r1, 3, 0);
        builder.addMeeting(c1, r1, 4, 0);

        // no violations
        builder.addMeeting(c2, r2, 1, 0);
        builder.addMeeting(c2, r2, 1, 1);
        builder.addMeeting(c2, r2, 2, 2);

        // no violations
        builder.addMeeting(c3, r3, 2, 0);
        builder.addMeeting(c3, r3, 3, 1);

        assertEquals(1, minWorkingDaysConstraint.violations(builder.build()));
    }

    @Test
    public void shouldSumUpMultipleViolationsForSameCourse() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        // c1 min 5 days. actual 1 day --> 4 violations
        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r2, 0, 1); // violation
        builder.addMeeting(c1, r1, 0, 2); // violation
        builder.addMeeting(c1, r2, 0, 3); // violation
        builder.addMeeting(c1, r3, 0, 4); // violation

        // no violations
        builder.addMeeting(c2, r2, 1, 0);
        builder.addMeeting(c2, r2, 1, 1);
        builder.addMeeting(c2, r2, 2, 2);

        // no violations
        builder.addMeeting(c3, r3, 2, 0);
        builder.addMeeting(c3, r3, 3, 1);

        assertEquals(4, minWorkingDaysConstraint.violations(builder.build()));
    }

    @Test
    public void shouldSumUpViolationsFromAllCourses() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        // c1 min 5 days. actual 1 day --> 4 violations
        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r1, 0, 1);
        builder.addMeeting(c1, r2, 0, 2);
        builder.addMeeting(c1, r1, 0, 3);
        builder.addMeeting(c1, r3, 0, 4);

        // c2 min 2 days. actual 1 day --> 1 violation
        builder.addMeeting(c2, r2, 1, 0);
        builder.addMeeting(c2, r2, 1, 1);
        builder.addMeeting(c2, r2, 1, 2);

        // c3 min 1 day. actual 1 day --> 0 violations
        builder.addMeeting(c3, r3, 2, 0);
        builder.addMeeting(c3, r3, 2, 1);

        assertEquals(5, minWorkingDaysConstraint.violations(builder.build()));
    }

    @Test
    public void shouldReturnZeroIfNoViolations() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        // c1 min 5 days
        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r1, 1, 0);
        builder.addMeeting(c1, r1, 2, 0);
        builder.addMeeting(c1, r1, 3, 0);
        builder.addMeeting(c1, r1, 4, 0);

        // c2 min 2 days
        builder.addMeeting(c2, r2, 0, 0);
        builder.addMeeting(c2, r2, 0, 1);
        builder.addMeeting(c2, r2, 1, 0);

        // c3 min 1 day
        builder.addMeeting(c3, r3, 0, 0);
        builder.addMeeting(c3, r3, 0, 1);

        assertEquals(0, minWorkingDaysConstraint.violations(builder.build()));
    }

}