package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.moea.SolutionConverter;
import org.junit.Before;
import org.junit.Test;
import org.moeaframework.core.Solution;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class CourseBasedCrossoverTest {

    int days = 3;
    int periodsPerDay = 3;

    Curriculum curr1 = new Curriculum("curr1");
    Curriculum curr2 = new Curriculum("curr2");

    Course c1 = Course.Builder.id("c1").teacher("t1").curriculum(curr1).nlectures(2)
            .nWorkingDays(3).nStudents(3).doubleLectures(false).build();
    Course c2 = Course.Builder.id("c2").teacher("t2").curriculum(curr2).nlectures(1)
            .nWorkingDays(3).nStudents(3).doubleLectures(false).build();

    Room r1 = new Room("r1", 3, 0);
    Room r2 = new Room("r2", 4, 1);

    Set<String> curricula = new HashSet(Arrays.asList(curr1.getId(), curr2.getId()));
    Set<String> rooms = new HashSet(Arrays.asList(r1.getId(), r2.getId()));

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(days, periodsPerDay);
    RoomConstraints roomConstraints = new RoomConstraints();

    Specification spec = Specification.Builder.name("specification")
            .days(days).periodsPerDay(periodsPerDay).minLectures(3).maxLectures(5)
            .room(r1).room(r2)
            .course(c1).course(c2)
            .curriculum(curr1).curriculum(curr2)
            .unavailabilityConstraints(unavailabilityConstraints)
            .roomConstraints(roomConstraints)
            .build();

    SolutionConverter solutionConverter = new SolutionConverter(new UD1Formulation(spec));

    CourseBasedCrossover courseBasedCrossover = new CourseBasedCrossover(solutionConverter, spec);

    @Before
    public void init() {
        curr1.setCourses(Collections.singletonList(c1));
        curr2.setCourses(Collections.singletonList(c2));
    }

    @Test
    public void shouldCopyFromTheSecond() {
        Timetable parent1 = new Timetable(curricula, rooms, days, periodsPerDay);
        parent1.addMeeting(new Meeting(c1, r1, 0, 0));
        parent1.addMeeting(new Meeting(c1, r1, 0, 1));
        parent1.addMeeting(new Meeting(c2, r2, 1, 0));
        Solution s1 = solutionConverter.toSolution(parent1);

        Timetable parent2 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting parent2Meeting = new Meeting(c2, r2, 1, 1);
        parent2.addMeeting(parent2Meeting);
        Solution s2 = solutionConverter.toSolution(parent2);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        Timetable child1 = solutionConverter.fromSolution(kids[0]);

        // that meeting should now exist in child 1
        assertEquals(parent2Meeting, child1.getMeeting(c2, 1, 1));
        // the old position should have been removed
        assertNull(child1.getMeeting(c2, 1, 0));
    }

    @Test
    public void shouldNotPlaceInViolationOfRoomConstraints() {
        fail("implement me");
    }

    @Test
    public void shouldNotPlaceInViolationOfUnavailabilityConstraints() {
        fail("implement me");
    }

}