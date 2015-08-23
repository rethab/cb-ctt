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
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class CourseBasedCrossoverTest {

    int days = 3;
    int periodsPerDay = 3;

    Curriculum curr1 = new Curriculum("curr1");
    Curriculum curr2 = new Curriculum("curr2");
    Curriculum curr3 = new Curriculum("curr3");

    Course c1 = Course.Builder.id("c1").teacher("t1").curriculum(curr1).nlectures(2)
            .nWorkingDays(3).nStudents(3).doubleLectures(false).build();
    Course c2 = Course.Builder.id("c2").teacher("t2").curriculum(curr2).nlectures(1)
            .nWorkingDays(3).nStudents(3).doubleLectures(false).build();
    Course c3 = Course.Builder.id("c3").teacher("t3").curriculum(curr3).nlectures(1)
            .nWorkingDays(3).nStudents(3).doubleLectures(false).build();
    Course c4 = Course.Builder.id("c4").teacher("t4").curriculum(curr1).curriculum(curr2)
            .nlectures(1).nWorkingDays(3).nStudents(3).doubleLectures(false).build();

    Room r1 = new Room("r1", 3, 0);
    Room r2 = new Room("r2", 4, 1);

    Set<String> curricula;
    Set<String> rooms;

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(days, periodsPerDay);
    RoomConstraints roomConstraints = new RoomConstraints();

    Specification spec = Specification.Builder.name("specification")
            .days(days).periodsPerDay(periodsPerDay).minLectures(3).maxLectures(5)
            .room(r1).room(r2)
            .course(c1).course(c2).course(c3).course(c4)
            .curriculum(curr1).curriculum(curr2).curriculum(curr3)
            .unavailabilityConstraints(unavailabilityConstraints)
            .roomConstraints(roomConstraints)
            .build();

    SolutionConverter solutionConverter = new SolutionConverter(new UD1Formulation(spec));

    CourseBasedCrossover courseBasedCrossover = new CourseBasedCrossover(solutionConverter, spec);

    @Before
    public void init() {
        curr1.setCourses(Arrays.asList(c1, c4));
        curr2.setCourses(Arrays.asList(c2, c4));
        curr3.setCourses(Collections.singletonList(c3));
        curricula = spec.getCurricula().stream().map(Curriculum::getId).collect(Collectors.toSet());
        rooms = spec.getRooms().stream().map(Room::getId).collect(Collectors.toSet());
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
        parent1.addMeeting(new Meeting(c4, r2, 1, 0));
        Solution s1 = solutionConverter.toSolution(parent1);

        Timetable parent2 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting parent2Meeting = new Meeting(c4, r2, 0, 0);
        parent2.addMeeting(parent2Meeting);
        Solution s2 = solutionConverter.toSolution(parent2);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        Timetable child1 = solutionConverter.fromSolution(kids[0]);

        // original meeting should still be there
        assertEquals(m, child1.getMeeting(c1, 0, 0));
        // new meeting should be not have been scheduled here
        assertNull(child1.getMeeting(c4, 0, 0));
        // should not override other existing either..
        assertNull(child1.getMeeting(c4, 0, 1));
        // ..but should be placed somewhere (two from c1, one from c4)
        assertEquals(3, child1.getMeetings().size());
    }

    @Test
    public void shouldNotPlaceInViolationOfRoomConstraints() {
        Timetable parent1 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting m1 = new Meeting(c1, r1, 0, 0);
        parent1.addMeeting(m1);
        Meeting m2 = new Meeting(c1, r1, 0, 1);
        parent1.addMeeting(m2);
        parent1.addMeeting(new Meeting(c4, r2, 1, 0));
        Solution s1 = solutionConverter.toSolution(parent1);

        Timetable parent2 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting parent2Meeting = new Meeting(c4, r1, 0, 0);
        parent2.addMeeting(parent2Meeting);
        Solution s2 = solutionConverter.toSolution(parent2);

        // usually c4 could be scheduled in r1 at 0/0, but now c1 occupies r1 and c4 cannot happen in r2
        roomConstraints.addRoomConstraint(c4, r2);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        Timetable child1 = solutionConverter.fromSolution(kids[0]);

        // original meetings should still be there
        assertEquals(m1, child1.getMeeting(c1, 0, 0));
        assertEquals(m2, child1.getMeeting(c1, 0, 1));

        // c4 must not be here due to room constraint
        assertNull(child1.getMeeting(c4, 0, 0));

        // ..but should be placed somewhere (two from c1, one from c4)
        assertEquals(3, child1.getMeetings().size());
    }

    @Test
    public void shouldNotPlaceInViolationOfUnavailabilityConstraints() {
        Timetable parent1 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting m1 = new Meeting(c1, r1, 0, 0);
        parent1.addMeeting(m1);
        Meeting m2 = new Meeting(c1, r1, 0, 1);
        parent1.addMeeting(m2);
        parent1.addMeeting(new Meeting(c4, r2, 1, 0));
        Solution s1 = solutionConverter.toSolution(parent1);

        Timetable parent2 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting parent2Meeting = new Meeting(c4, r1, 0, 0);
        parent2.addMeeting(parent2Meeting);
        Solution s2 = solutionConverter.toSolution(parent2);

        // usually c4 could be scheduled in r1 at 0/0, but now t4 cannot teach at 0/0
        unavailabilityConstraints.addUnavailability(c4, 0, 0);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        Timetable child1 = solutionConverter.fromSolution(kids[0]);

        // original meetings should still be there
        assertEquals(m1, child1.getMeeting(c1, 0, 0));
        assertEquals(m2, child1.getMeeting(c1, 0, 1));

        // c4 must not be here, because teacher is unavailable
        assertNull(child1.getMeeting(c4, 0, 0));

        // ..but should be placed somewhere (two from c1, one from c4)
        assertEquals(3, child1.getMeetings().size());
    }

    @Test
    public void shouldReplaceExistingMeetingIfFeasible() {
        Timetable parent1 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting m1 = new Meeting(c1, r1, 0, 0);
        parent1.addMeeting(m1);
        Meeting m2 = new Meeting(c2, r2, 0, 0);
        parent1.addMeeting(m2);
        Solution s1 = solutionConverter.toSolution(parent1);

        Timetable parent2 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting parent2Meeting = new Meeting(c3, r1, 0, 0);
        parent2.addMeeting(parent2Meeting);
        Solution s2 = solutionConverter.toSolution(parent2);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        Timetable child1 = solutionConverter.fromSolution(kids[0]);

        // new meeting should exist at 0/0. cannot compare entire meeting due to room change
        assertEquals(c3, child1.getMeeting(c3, 0, 0).getCourse());
        // one old meeting should also still be there..
        assertTrue(child1.getMeeting(c1, 0, 0) != null || child1.getMeeting(c2, 0, 0) != null);
        // ..but not both
        assertFalse(child1.getMeeting(c1, 0, 0) != null && child1.getMeeting(c2, 0, 0) != null);

        // ..all should be scheduled somewhere
        assertEquals(3, child1.getMeetings().size());
    }

    @Test
    public void shouldPlaceFromFirstParentInSecondChild() {
        Timetable parent1 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting parent1Meeting = new Meeting(c3, r1, 0, 0);
        parent1.addMeeting(parent1Meeting);
        Solution s1 = solutionConverter.toSolution(parent1);

        Timetable parent2 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting m1 = new Meeting(c1, r1, 0, 0);
        parent2.addMeeting(m1);
        Meeting m2 = new Meeting(c2, r2, 0, 0);
        parent2.addMeeting(m2);
        Solution s2 = solutionConverter.toSolution(parent2);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        Timetable child2 = solutionConverter.fromSolution(kids[1]);

        // new meeting should exist at 0/0. cannot compare entire meeting due to room change
        assertEquals(c3, child2.getMeeting(c3, 0, 0).getCourse());
        // one old meeting should also still be there..
        assertTrue(child2.getMeeting(c1, 0, 0) != null || child2.getMeeting(c2, 0, 0) != null);
        // ..but not both
        assertFalse(child2.getMeeting(c1, 0, 0) != null && child2.getMeeting(c2, 0, 0) != null);

        // ..all should be scheduled somewhere
        assertEquals(3, child2.getMeetings().size());
    }

    @Test
    public void shouldNotModifyParents() {
        Timetable parent1 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting m1 = new Meeting(c1, r1, 0, 0);
        parent1.addMeeting(m1);
        Meeting m2 = new Meeting(c2, r2, 0, 0);
        parent1.addMeeting(m2);
        Solution s1 = solutionConverter.toSolution(parent1);

        Timetable parent2 = new Timetable(curricula, rooms, days, periodsPerDay);
        Meeting parent2Meeting = new Meeting(c3, r1, 0, 0);
        parent2.addMeeting(parent2Meeting);
        Solution s2 = solutionConverter.toSolution(parent2);

        courseBasedCrossover.evolve(new Solution[]{s1, s2});

        assertEquals(2, parent1.getMeetings().size());
        assertEquals(m1, parent1.getMeeting(c1, 0, 0));
        assertEquals(m2, parent1.getMeeting(c2, 0, 0));

        assertEquals(1, parent2.getMeetings().size());
        assertEquals(parent2Meeting, parent2.getMeeting(c3, 0, 0));
    }
}