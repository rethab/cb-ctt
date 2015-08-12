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

    Course c1 = new Course("c1", "curr1", "t1", 2, 1, 3, false);
    Course c2 = new Course("c2", "curr1", "t2", 2, 1, 3, false);
    Course c3 = new Course("c3", "curr2", "t3", 2, 1, 3, false);

    Curriculum curr1 = new Curriculum("curr1");
    Curriculum curr2 = new Curriculum("curr2");
    Set<String> curricula = new HashSet<>(Arrays.asList(new String[]{curr1.getId(), curr2.getId()}));

    Room r1 = new Room("r1", 1, 1);
    Set<String> rooms = new HashSet<>(Arrays.asList(new String[]{r1.getId()}));

    Specification spec = new Specification("spec1", days, periodsPerDay, 3, 5, Arrays.asList(c1, c2, c3),
            Arrays.asList(r1), Arrays.asList(curr1, curr2), unavailabilityConstraints, roomConstraints);

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