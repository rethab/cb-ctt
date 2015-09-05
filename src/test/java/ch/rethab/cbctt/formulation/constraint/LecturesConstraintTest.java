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
public class LecturesConstraintTest {

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(5, 4);
    RoomConstraints roomConstraints = new RoomConstraints();

    Curriculum curr1 = new Curriculum("curr1");
    Curriculum curr2 = new Curriculum("curr2");

    Course c1 = Course.Builder.id("c1").curriculum(curr1).teacher("t1").nlectures(1).nWorkingDays(1).nStudents(40).doubleLectures(true).build();
    Course c2 = Course.Builder.id("c2").curriculum(curr1).teacher("t2").nlectures(2).nWorkingDays(2).nStudents(15).doubleLectures(true).build();
    Course c3 = Course.Builder.id("c3").curriculum(curr2).teacher("t3").nlectures(2).nWorkingDays(2).nStudents(15).doubleLectures(true).build();
    Course c4 = Course.Builder.id("c4").curriculum(curr1).curriculum(curr2).teacher("t4").nlectures(2).nWorkingDays(2).nStudents(15).doubleLectures(true).build();

    Room r1 = new Room("r1", 40, 1);
    Room r2 = new Room("r2", 30, 1);
    Room r3 = new Room("r3", 14, 0);

    private int days = 5;
    private int periodsPerDay = 4;

    Specification spec = Specification.Builder.name("spec1").days(days).periodsPerDay(periodsPerDay)
            .minLectures(3).maxLectures(5)
            .course(c1).course(c2).course(c3).course(c4)
            .room(r1).room(r2).room(r3)
            .curriculum(curr1).curriculum(curr2)
            .unavailabilityConstraints(unavailabilityConstraints)
            .roomConstraints(roomConstraints).build();

    LecturesConstraint lecturesConstraint = new LecturesConstraint(spec);

    @Before
    public void init() {
        curr1.setCourses(Arrays.asList(c1, c2));
    }

    @Test
    public void shouldCountViolationOfMultiCurriculumCurseOnlyOnce() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);
        // c1 consists of one lecture
        builder.addMeeting(c1, r3, 0, 1);

        // course 2 consists of 2 lectures: missing entirely
        // c3 consists of two lectures
        builder.addMeeting(c3, r1, 0, 1);
        builder.addMeeting(c3, r2, 1, 1);
        // c4 consists of two lectures and belongs to both curricula
        builder.addMeeting(c4, r3, 1, 2);
        assertEquals(3, lecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldSucceedWithLecturesAtDifferentPeriod() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);
        // c1 consists of one lecture
        builder.addMeeting(c1, r3, 0, 1);

        // course 2 consists of 2 lectures
        builder.addMeeting(c2, r1, 2, 2);
        builder.addMeeting(c2, r1, 2, 3);

        // c3 consists of two lectures
        builder.addMeeting(c3, r1, 0, 1);
        builder.addMeeting(c3, r2, 1, 1);

        // c4 consists of two lectures
        builder.addMeeting(c4, r1, 2, 0);
        builder.addMeeting(c4, r2, 2, 1);
        assertEquals(0, lecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldCountMissingLecturesOfSameCourse() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);
        // c1 consists of 1 lecture
        builder.addMeeting(c1, r1, 0, 0);
        // c2 consists of 2 lectures
        builder.addMeeting(c2, r1, 0, 1);
        // c3 consists of 2 lectures: missing entirely
        // c4 consists of two lectures
        builder.addMeeting(c4, r1, 2, 0);
        builder.addMeeting(c4, r2, 2, 1);
        assertEquals(3, lecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldCountMissingLecturesOfDifferentCourse() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);
        // c1 consists of 1 lecture: missing entirely
        // c2 consists of 2 lectures
        builder.addMeeting(c2, r1, 0, 1);
        // c3 consists of 2 lectures: missing entirely
        // c4 consists of 2 lectures
        builder.addMeeting(c4, r1, 2, 0);
        assertEquals(5, lecturesConstraint.violations(builder.build()));
    }

}