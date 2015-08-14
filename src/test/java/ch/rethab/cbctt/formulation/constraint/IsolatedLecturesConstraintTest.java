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
public class IsolatedLecturesConstraintTest {

    int days = 3;
    int periodsPerDay = 4;

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(days, periodsPerDay);
    RoomConstraints roomConstraints = new RoomConstraints();

    Curriculum curr1 = new Curriculum("curr1");
    Curriculum curr2 = new Curriculum("curr2");
    Set<String> curricula = new HashSet<>(Arrays.asList(new String[]{curr1.getId(), curr2.getId()}));

    Course c1 = Course.Builder.id("c1").curriculum(curr1).teacher("t1").nlectures(2).nWorkingDays(1).nStudents(3).doubleLectures(false).build();
    Course c2 = Course.Builder.id("c2").curriculum(curr1).teacher("t2").nlectures(2).nWorkingDays(1).nStudents(3).doubleLectures(false).build();
    Course c3 = Course.Builder.id("c3").curriculum(curr2).teacher("t3").nlectures(2).nWorkingDays(1).nStudents(3).doubleLectures(false).build();

    Room r1 = new Room("r1", 1, 1);
    Set<String> rooms = new HashSet<>(Arrays.asList(new String[]{r1.getId()}));

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
    public void shouldNotAccumulateOverMultipleDaysForSameCurrcilum() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        // c1 and c2 belong to same curriculum
        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c2, r1, 1, 0));

        assertEquals(0, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldNotAccumulateOverMultipleDaysForSameCourse() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r1, 1, 0));

        assertEquals(0, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldNotAccumulateIfDirectlyAfterEachOtherForCourse() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r1, 0, 1));

        assertEquals(0, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldNotCountSingleLecture() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);
        t.addMeeting(new Meeting(c1, r1, 0, 0));
        assertEquals(0, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldNotCountSingleLectureForDifferentCurricula() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);
        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c3, r1, 0, 3));
        assertEquals(0, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldCountLastAndFirstPeriodOfDay() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r1, 0, periodsPerDay-1));

        assertEquals(2, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldNotAccumulateIfDirectlyAfterEachOtherForCurriculum() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c2, r1, 0, 1));

        assertEquals(0, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldAccumulateForCourseWithinDay() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r1, 0, 2));

        assertEquals(2, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldAccumulateForCurriculumWithinDay() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c2, r1, 0, 2));

        assertEquals(2, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldAccumulateForCourseWithinDayWithOtherAdjacent() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r1, 0, 1));
        t.addMeeting(new Meeting(c1, r1, 0, 3));

        assertEquals(1, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldAccumulateForCurriculumWithinDayWithOtherAdjacent() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r1, 0, 1));
        t.addMeeting(new Meeting(c2, r1, 0, 3));

        assertEquals(1, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldNotAccumulateTwoGroupsWithinDay() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        // this is mostly a question of definition. The two
        // groups are not adjacent, however, the individual
        // lectures are

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r1, 0, 1));
        t.addMeeting(new Meeting(c2, r1, 0, 2));
        t.addMeeting(new Meeting(c2, r1, 0, 3));

        assertEquals(0, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldNotAccumulateWithSpreadOverDays() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, spec.getPeriodsPerDay()-1));
        t.addMeeting(new Meeting(c1, r1, 1, 0));
        t.addMeeting(new Meeting(c1, r1, 1, 1));

        assertEquals(0, isolatedLecturesConstraint.violations(t));
    }


    @Test
    public void shouldSumUpDifferentViolations() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r1, 0, 2));

        t.addMeeting(new Meeting(c1, r1, 1, 0));
        t.addMeeting(new Meeting(c1, r1, 1, 2));

        t.addMeeting(new Meeting(c2, r1, 2, 0));
        t.addMeeting(new Meeting(c1, r1, 2, 2));

        assertEquals(6, isolatedLecturesConstraint.violations(t));

    }

}