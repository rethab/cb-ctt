package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.Room;
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

    private IsolatedLecturesConstraint isolatedLecturesConstraint = new IsolatedLecturesConstraint();

    private Curriculum curr1 = new Curriculum("curr1");
    private Curriculum curr2 = new Curriculum("curr2");
    private Set<String> curricula = new HashSet<>(Arrays.asList(new String[]{curr1.getId(), curr2.getId()}));

    private Course c1 = new Course("c1", "curr1", "t1", 2, 1, 3, false);
    private Course c2 = new Course("c2", "curr1", "t2", 2, 1, 3, false);

    private Course c3 = new Course("c3", "curr2", "t3", 2, 1, 3, false);

    private Room r1 = new Room("r1", 1, 1);
    private Set<String> rooms = new HashSet<>(Arrays.asList(new String[]{r1.getId()}));

    private int days = 3;

    private int periodsPerDay = 4;

    @Before
    public void init() {
        curr1.setCourses(Arrays.asList(c1, c2));
        curr2.setCourses(Arrays.asList(c3));
    }

    @Test
    public void shouldFail() {
        fail("implement me");
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

        assertEquals(1, isolatedLecturesConstraint.violations(t));
    }

    @Test
    public void shouldAccumulateForCurriculumWithinDay() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c2, r1, 0, 2));

        assertEquals(1, isolatedLecturesConstraint.violations(t));
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
    public void shouldSumUpDifferentViolations() {
        Timetable t = new Timetable(curricula, rooms, days, periodsPerDay);

        t.addMeeting(new Meeting(c1, r1, 0, 0));
        t.addMeeting(new Meeting(c1, r1, 0, 2));

        t.addMeeting(new Meeting(c1, r1, 1, 0));
        t.addMeeting(new Meeting(c1, r1, 1, 2));

        t.addMeeting(new Meeting(c2, r1, 2, 0));
        t.addMeeting(new Meeting(c1, r1, 2, 2));

        assertEquals(1, isolatedLecturesConstraint.violations(t));

    }

}