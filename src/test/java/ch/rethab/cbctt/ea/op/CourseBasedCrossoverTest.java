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
    Course c3 = Course.Builder.id("c3").teacher("t3").curriculum(curr1).curriculum(curr2)
            .nlectures(1).nWorkingDays(3).nStudents(3).doubleLectures(false).build();

    Room r1 = new Room("r1", 3, 0);
    Room r2 = new Room("r2", 4, 1);

    Set<String> curricula = new HashSet(Arrays.asList(curr1.getId(), curr2.getId()));
    Set<String> rooms = new HashSet(Arrays.asList(r1.getId(), r2.getId()));

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(days, periodsPerDay);
    RoomConstraints roomConstraints = new RoomConstraints();

    Specification spec = Specification.Builder.name("specification")
            .days(days).periodsPerDay(periodsPerDay).minLectures(3).maxLectures(5)
            .room(r1).room(r2)
            .course(c1).course(c2).course(c3)
            .curriculum(curr1).curriculum(curr2)
            .unavailabilityConstraints(unavailabilityConstraints)
            .roomConstraints(roomConstraints)
            .build();

    SolutionConverter solutionConverter = new SolutionConverter(new UD1Formulation(spec));

    CourseBasedCrossover courseBasedCrossover = new CourseBasedCrossover(solutionConverter, spec);

    @Before
    public void init() {
        curr1.setCourses(Arrays.asList(c1, c3));
        curr2.setCourses(Arrays.asList(c2, c3));
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
    public void shouldPlaceInSamePeriodWithOtherCourseIfDifferentCurriculum() {
        Timetable parent1 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting m1 = new Meeting(c1, r1, 0, 0); // c2 will be scheduled here, but it should stay
        parent1.addMeeting(m1);
        parent1.addMeeting(new Meeting(c1, r1, 0, 1));
        parent1.addMeeting(new Meeting(c2, r2, 1, 0)); // this should be removed
        Solution s1 = solutionConverter.toSolution(parent1);

        Timetable parent2 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting parent2Meeting = new Meeting(c2, r2, 0, 0);
        parent2.addMeeting(parent2Meeting);
        Solution s2 = solutionConverter.toSolution(parent2);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        Timetable child1 = solutionConverter.fromSolution(kids[0]);

        // the old and the new meeting should be at the same period
        assertEquals(m1, child1.getMeeting(c1, 0, 0));
        assertEquals(new Meeting(c2, r2, 0, 0), child1.getMeeting(c2, 0, 0));
        // two meetings for c1, one for c2
        assertEquals(3, child1.getMeetings().size());
    }

    @Test
    public void shouldNotPlaceIfOtherCourseFromSameCurriculumIsThere() {
        Timetable parent1 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting m = new Meeting(c1, r1, 0, 0);
        parent1.addMeeting(m);
        parent1.addMeeting(new Meeting(c1, r1, 0, 1));
        parent1.addMeeting(new Meeting(c3, r2, 1, 0));
        Solution s1 = solutionConverter.toSolution(parent1);

        Timetable parent2 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting parent2Meeting = new Meeting(c3, r2, 0, 0);
        parent2.addMeeting(parent2Meeting);
        Solution s2 = solutionConverter.toSolution(parent2);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        Timetable child1 = solutionConverter.fromSolution(kids[0]);

        // original meeting should still be there
        assertEquals(m, child1.getMeeting(c1, 0, 0));
        // new meeting should be not have been scheduled here
        assertNull(child1.getMeeting(c3, 0, 0));
        // should not override other existing either..
        assertNull(child1.getMeeting(c3, 0, 1));
        // ..but should be placed somewhere (two from c1, one from c3)
        assertEquals(3, child1.getMeetings().size());
    }

    @Test
    public void shouldNotPlaceInViolationOfRoomConstraints() {
        Timetable parent1 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting m1 = new Meeting(c1, r1, 0, 0);
        parent1.addMeeting(m1);
        Meeting m2 = new Meeting(c1, r1, 0, 1);
        parent1.addMeeting(m2);
        parent1.addMeeting(new Meeting(c3, r2, 1, 0));
        Solution s1 = solutionConverter.toSolution(parent1);

        Timetable parent2 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting parent2Meeting = new Meeting(c3, r1, 0, 0);
        parent2.addMeeting(parent2Meeting);
        Solution s2 = solutionConverter.toSolution(parent2);

        // usually c3 could be scheduled in r1 at 0/0, but now c1 occupies r1 and c3 cannot happen in r2
        roomConstraints.addRoomConstraint(c3, r2);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        Timetable child1 = solutionConverter.fromSolution(kids[0]);

        // original meetings should still be there
        assertEquals(m1, child1.getMeeting(c1, 0, 0));
        assertEquals(m2, child1.getMeeting(c1, 0, 1));

        // c3 must not be here due to room constraint
        assertNull(child1.getMeeting(c3, 0, 0));

        // ..but should be placed somewhere (two from c1, one from c3)
        assertEquals(3, child1.getMeetings().size());
    }

    @Test
    public void shouldNotPlaceInViolationOfUnavailabilityConstraints() {
        Timetable parent1 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting m1 = new Meeting(c1, r1, 0, 0);
        parent1.addMeeting(m1);
        Meeting m2 = new Meeting(c1, r1, 0, 1);
        parent1.addMeeting(m2);
        parent1.addMeeting(new Meeting(c3, r2, 1, 0));
        Solution s1 = solutionConverter.toSolution(parent1);

        Timetable parent2 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting parent2Meeting = new Meeting(c3, r1, 0, 0);
        parent2.addMeeting(parent2Meeting);
        Solution s2 = solutionConverter.toSolution(parent2);

        // usually c3 could be scheduled in r1 at 0/0, but now t3 cannot teach at 0/0
        unavailabilityConstraints.addUnavailability(c3, 0, 0);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        Timetable child1 = solutionConverter.fromSolution(kids[0]);

        // original meetings should still be there
        assertEquals(m1, child1.getMeeting(c1, 0, 0));
        assertEquals(m2, child1.getMeeting(c1, 0, 1));

        // c3 must not be here, because teacher is unavailable
        assertNull(child1.getMeeting(c3, 0, 0));

        // ..but should be placed somewhere (two from c1, one from c3)
        assertEquals(3, child1.getMeetings().size());
    }

    @Test
    public void shouldReplaceExistingMeetingIfFeasible() {
        fail("implement me");
    }

    @Test
    public void shouldNotIncreaseOrDecreaseTotalNumberOfMeetings() {
        fail("implement me");
    }

    @Test
    public void shouldNotModifyParents() {
        fail("implement me");
    }

}