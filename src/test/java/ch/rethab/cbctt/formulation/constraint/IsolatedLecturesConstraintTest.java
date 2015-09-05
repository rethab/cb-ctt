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
public class IsolatedLecturesConstraintTest {

    int days = 3;
    int periodsPerDay = 4;

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(days, periodsPerDay);
    RoomConstraints roomConstraints = new RoomConstraints();

    Curriculum curr1 = new Curriculum("curr1");
    Curriculum curr2 = new Curriculum("curr2");

    Course c1 = Course.Builder.id("c1").curriculum(curr1).teacher("t1").nlectures(2).nWorkingDays(1).nStudents(3).doubleLectures(false).build();
    Course c2 = Course.Builder.id("c2").curriculum(curr1).teacher("t2").nlectures(2).nWorkingDays(1).nStudents(3).doubleLectures(false).build();
    Course c3 = Course.Builder.id("c3").curriculum(curr2).teacher("t3").nlectures(2).nWorkingDays(1).nStudents(3).doubleLectures(false).build();

    Room r1 = new Room("r1", 1, 1);

    Specification spec = Specification.Builder.name("spec1")
            .days(days).periodsPerDay(periodsPerDay)
            .minLectures(3).maxLectures(5)
            .course(c1).course(c2).course(c3)
            .room(r1)
            .curriculum(curr1).curriculum(curr2)
            .unavailabilityConstraints(unavailabilityConstraints)
            .roomConstraints(roomConstraints).build();

    IsolatedLecturesConstraint isolatedLecturesConstraint = new IsolatedLecturesConstraint(spec);

    @Before
    public void init() {
        curr1.setCourses(Arrays.asList(c1, c2));
        curr2.setCourses(Arrays.asList(c3));
    }

    @Test
    public void shouldNotAccumulateOverMultipleDaysForSameCurriculum() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        // c1 and c2 belong to same curriculum
        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c2, r1, 1, 0);

        assertEquals(0, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldNotAccumulateOverMultipleDaysForSameCourse() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r1, 1, 0);

        assertEquals(0, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldNotAccumulateIfDirectlyAfterEachOtherForCourse() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r1, 0, 1);

        assertEquals(0, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldNotCountSingleLecture() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);
        builder.addMeeting(c1, r1, 0, 0);
        assertEquals(0, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldNotCountSingleLectureForDifferentCurricula() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);
        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c3, r1, 0, 3);
        assertEquals(0, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldCountLastAndFirstPeriodOfDay() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r1, 0, periodsPerDay-1);

        assertEquals(2, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldNotAccumulateIfDirectlyAfterEachOtherForCurriculum() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c2, r1, 0, 1);

        assertEquals(0, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldAccumulateForCourseWithinDay() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r1, 0, 2);

        assertEquals(2, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldAccumulateForCurriculumWithinDay() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c2, r1, 0, 2);

        assertEquals(2, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldAccumulateForCourseWithinDayWithOtherAdjacent() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r1, 0, 1);
        builder.addMeeting(c1, r1, 0, 3);

        assertEquals(1, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldAccumulateForCurriculumWithinDayWithOtherAdjacent() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r1, 0, 1);
        builder.addMeeting(c2, r1, 0, 3);

        assertEquals(1, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldNotAccumulateTwoGroupsWithinDay() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        // this is mostly a question of definition. The two
        // groups are not adjacent, however, the individual
        // lectures are

        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r1, 0, 1);
        builder.addMeeting(c2, r1, 0, 2);
        builder.addMeeting(c2, r1, 0, 3);

        assertEquals(0, isolatedLecturesConstraint.violations(builder.build()));
    }

    @Test
    public void shouldNotAccumulateWithSpreadOverDays() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c1, r1, 0, spec.getPeriodsPerDay()-1);
        builder.addMeeting(c1, r1, 1, 0);
        builder.addMeeting(c1, r1, 1, 1);

        assertEquals(0, isolatedLecturesConstraint.violations(builder.build()));
    }


    @Test
    public void shouldSumUpDifferentViolations() {
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);

        builder.addMeeting(c1, r1, 0, 0);
        builder.addMeeting(c1, r1, 0, 2);

        builder.addMeeting(c1, r1, 1, 0);
        builder.addMeeting(c1, r1, 1, 2);

        builder.addMeeting(c2, r1, 2, 0);
        builder.addMeeting(c1, r1, 2, 2);

        assertEquals(6, isolatedLecturesConstraint.violations(builder.build()));

    }

}