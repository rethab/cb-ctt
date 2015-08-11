package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.Room;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class TimetableTest {

    Curriculum cur1 = new Curriculum("curr1");
    Curriculum cur2 = new Curriculum("curr2");
    Set<String> curricula = new HashSet<>(Arrays.asList(cur1.getId(), cur2.getId()));

    Course c1 = new Course("c1", cur1.getId(), "t1", 1, 1, 1, false);
    Course c2 = new Course("c2", cur1.getId(), "t2", 1, 1, 1, false);
    Course c3 = new Course("c3", cur2.getId(), "t3", 1, 1, 1, false);

    Room r1 = new Room("r1", 1, 1);
    Room r2 = new Room("r2", 1, 1);
    Set<String> rooms = new HashSet<>(Arrays.asList(r1.getId(), r2.getId()));

    @Test(expected = Timetable.InfeasibilityException.class)
    public void shouldNotAllowTwoMeetingsAtTheSameTime() {
        Timetable tt = new Timetable(curricula, rooms, 1, 1);

        tt.addMeeting(new Meeting(c1, r1, 0, 0));
        tt.addMeeting(new Meeting(c2, r1, 0, 0));
    }

    @Test(expected = Timetable.InfeasibilityException.class)
    public void shouldNotAllowToScheduleTwoCoursesOfSameCurriculumAtSameTime() {
        Timetable tt = new Timetable(curricula, rooms, 1, 1);

        tt.addMeeting(new Meeting(c1, r1, 0, 0));
        tt.addMeeting(new Meeting(c2, r2, 0, 0));
    }

    @Test
    public void shouldAllowTwoCursesOfDifferentCurriculaAtSameTime() {
        Timetable tt = new Timetable(curricula, rooms, 1, 1);

        tt.addMeeting(new Meeting(c1, r1, 0, 0));
        tt.addMeeting(new Meeting(c3, r2, 0, 0));
    }

}