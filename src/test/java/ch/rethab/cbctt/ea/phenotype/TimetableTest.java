package ch.rethab.cbctt.ea.phenotype;

import ch.rethab.cbctt.domain.*;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class TimetableTest {

    int days = 5;
    int periodsPerDay = 8;

    Curriculum cur1 = new Curriculum("curr1");
    Curriculum cur2 = new Curriculum("curr2");

    Course c1 = Course.Builder.id("c1").curriculum(cur1).teacher("t1").build();
    Course c2 = Course.Builder.id("c2").curriculum(cur1).teacher("t2").build();
    Course c3 = Course.Builder.id("c3").curriculum(cur2).teacher("t3").build();
    Course c4 = Course.Builder.id("c4").curriculum(cur1).curriculum(cur2).teacher("t4").build();

    Room r1 = new Room("r1", 1, 1);
    Room r2 = new Room("r2", 1, 1);

    Specification spec = Specification.Builder.name("spec")
            .course(c1).course(c2).course(c3).course(c4)
            .room(r1).room(r2)
            .curriculum(cur1).curriculum(cur2)
            .days(days).periodsPerDay(periodsPerDay)
            .minLectures(1).maxLectures(8)
            .unavailabilityConstraints(new UnavailabilityConstraints(days, periodsPerDay))
            .roomConstraints(new RoomConstraints())
            .build();

    @Test
    public void shouldReturnSameMeetingForAllCoursesIfMultipleCurricula() {
        Timetable tt = new Timetable(spec);
        tt.addMeeting(new Meeting(c4, 0, 0));
        Set<Meeting> meetingsByCourse = tt.getMeetingsByCourse(c4);
        assertEquals(1, meetingsByCourse.size());
        assertEquals(c4, meetingsByCourse.iterator().next().getCourse());
    }

    @Test(expected = Timetable.InfeasibilityException.class)
    public void shouldNotAllowTwoMeetingsAtTheSameTime() {
        Timetable tt = new Timetable(spec);

        tt.addMeeting(new Meeting(c1, 0, 0));
        tt.addMeeting(new Meeting(c2, 0, 0));
    }

    @Test
    public void shouldSetRoomToFreeWhenRemovingMeeting() {
        Timetable t = new Timetable(spec);
        Meeting m1 = new Meeting(c1, 0, 1);
        Meeting m2 = new Meeting(c2, 0, 1);
        t.addMeeting(m1);
        t.removeMeeting(m1);
        t.addMeeting(m2);
    }

    @Test(expected = Timetable.InfeasibilityException.class)
    public void shouldNotAllowToScheduleTwoCoursesOfSameCurriculumAtSameTime() {
        Timetable tt = new Timetable(spec);

        tt.addMeeting(new Meeting(c1, 0, 0));
        tt.addMeeting(new Meeting(c2, 0, 0));
    }

    @Test
    public void shouldAllowTwoCursesOfDifferentCurriculaAtSameTime() {
        Timetable tt = new Timetable(spec);

        tt.addMeeting(new Meeting(c1, 0, 0));
        tt.addMeeting(new Meeting(c3, 0, 0));
    }

    @Test (expected = Timetable.InfeasibilityException.class)
    public void shouldSetCourseOfMultileCurriculaOnAllCurriculaTimetables() {
        Timetable tt = new Timetable(spec);

        tt.addMeeting(new Meeting(c4, 0, 0)); // belongs to cur1 + cur2
        tt.addMeeting(new Meeting(c1, 0, 0));
    }

    @Test(expected = Timetable.InfeasibilityException.class)
    public void shouldFailWithLecturesOfSameCurriculumAtSamePeriod() {
        Timetable t = new Timetable(spec);
        t.addMeeting(new Meeting(c1, 0, 0));
        t.addMeeting(new Meeting(c2, 0, 0));
    }

    @Test(expected = Timetable.InfeasibilityException.class)
    public void shouldFailWithLecturesOfSameCourseAtSamePeriod() {
        Timetable t = new Timetable(spec);
        t.addMeeting(new Meeting(c1, 0, 0));

        // two lectures of same course at same period
        t.addMeeting(new Meeting(c2, 0, 1));
        t.addMeeting(new Meeting(c2, 0, 1));
    }

    @Test(expected = Timetable.InfeasibilityException.class)
    public void shouldFailWithTwoLecturesInSameRoomAtSamePeriod() {
        Timetable t = new Timetable(spec);
        t.addMeeting(new Meeting(c1, 0, 1));
        t.addMeeting(new Meeting(c2, 0, 1));
        t.addMeeting(new Meeting(c3, 0, 1));
    }

    @Test
    public void shouldAllowACourseToBeInTwoCurricula() {
        Timetable t = new Timetable(spec);
        CurriculumTimetable c1tt = t.getCurriculumTimetables().get(cur1.getId());
        CurriculumTimetable c2tt = t.getCurriculumTimetables().get(cur2.getId());

        t.addMeeting(new Meeting(c4, 0, 1)); // c4 belongs to both cur1 + cur2

        Meeting m1 = c1tt.get(0, 1);
        Meeting m2 = c2tt.get(0, 1);
        assertEquals(m1.getCourse(), m2.getCourse());
    }

    @Test
    public void shouldAddAndRemoveAndReplace() {
        Timetable t = new Timetable(spec);
        Meeting m1 = new Meeting(c4, 0, 1);
        Meeting m2 = new Meeting(c3, 0, 1);

        // should be there
        t.addMeeting(m1);
        assertEquals(m1, t.getMeeting(m1.getCourse(), 0, 1));
        assertEquals(1, t.getMeetings().size());

        // m1 should be returned and m2 scheduled
        assertEquals(m1, t.replaceMeeting(0, 1, m2));
        assertEquals(m2, t.getMeeting(m2.getCourse(), 0, 1));
        assertEquals(1, t.getMeetings().size());

        // none should be there
        t.removeMeeting(m2);
        assertNull(t.getMeeting(m2.getCourse(), 0, 1));
        assertEquals(0, t.getMeetings().size());
    }

}