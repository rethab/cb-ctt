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

    @Test
    public void shouldNotAllowTwoMeetingsAtTheSameTime() {
        Set<String> curricula = new HashSet(Arrays.asList(new String[] {"curr1"}));
        Set<String> rooms = new HashSet(Arrays.asList(new String[] {"r1"}));
        Timetable tt = new Timetable(curricula, rooms, 1, 1);

        tt.addMeeting(new Meeting(new Course("c1", "curr1", 1, 1, 1, false), new Room("r1", 1, 1), 0, 0));
        tt.addMeeting(new Meeting(new Course("c2", "curr1", 1, 1, 1, false), new Room("r1", 1, 1), 0, 0));
    }

    @Test
    public void makeMoreIntegrityTests() {
        fail("make more integrity tests");
    }

}