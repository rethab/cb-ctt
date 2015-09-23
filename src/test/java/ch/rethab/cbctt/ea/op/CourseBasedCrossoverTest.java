package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.initializer.TeacherGreedyInitializer;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.formulation.constraint.Constraint;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.parser.ECTTParser;
import org.junit.Before;
import org.junit.Test;
import org.moeaframework.core.Solution;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    RoomAssigner roomAssigner = new GreedyRoomAssigner(spec);
    CourseBasedCrossover courseBasedCrossover = new CourseBasedCrossover(spec, solutionConverter, roomAssigner);

    @Before
    public void init() {
        curr1.setCourses(Arrays.asList(c1, c4));
        curr2.setCourses(Arrays.asList(c2, c4));
        curr3.setCourses(Collections.singletonList(c3));
    }

    @Test
    public void shouldCopyFromTheSecond() {
        TimetableWithRooms.Builder p1Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p1Builder.addMeeting(c2, r2, 1, 0);
        Solution s1 = solutionConverter.toSolution(p1Builder.build());

        TimetableWithRooms.Builder p2Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p2Builder.addMeeting(c2, r2, 1, 1);
        Solution s2 = solutionConverter.toSolution(p2Builder.build());

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        TimetableWithRooms child1 = solutionConverter.fromSolution(kids[0]);

        // that meeting should now exist in child 1
        assertEquals(c2, child1.getMeeting(c2, 1, 1).getCourse());
        // the old position should have been removed
        assertNull(child1.getMeeting(c2, 1, 0));
    }

    @Test
    public void shouldPlaceInSamePeriodWithOtherCourseIfDifferentCurriculum() {
        TimetableWithRooms.Builder p1Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p1Builder.addMeeting(c1, r1, 0, 0);
        p1Builder.addMeeting(c2, r2, 1, 0);
        Solution s1 = solutionConverter.toSolution(p1Builder.build());

        TimetableWithRooms.Builder p2Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p2Builder.addMeeting(c1, r1, 1, 0);
        p2Builder.addMeeting(c2, r2, 0, 0);
        Solution s2 = solutionConverter.toSolution(p2Builder.build());

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        TimetableWithRooms child1 = solutionConverter.fromSolution(kids[0]);

        // either c2 is moved to the same period as c1 or c1 is moved to the same as c2
        boolean c2Moved = child1.getMeeting(c1, 0, 0) != null && child1.getMeeting(c2, 0, 0) != null;
        boolean c1Moved = child1.getMeeting(c1, 1, 0) != null && child1.getMeeting(c2, 1, 0) != null;

        // either has has moved, but not both
        assertTrue(c1Moved || c2Moved);
        assertFalse(c1Moved && c2Moved);

        // total number should stay
        assertEquals(2, child1.getMeetings().size());
    }

    @Test
    public void shouldNotPlaceIfOtherCourseFromSameCurriculumIsThere() {
        TimetableWithRooms.Builder p1Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p1Builder.addMeeting(c1, r1, 0, 0);
        p1Builder.addMeeting(c4, r2, 1, 0);
        Solution s1 = solutionConverter.toSolution(p1Builder.build());

        TimetableWithRooms.Builder p2Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p2Builder.addMeeting(c1, r1, 1, 0);
        p2Builder.addMeeting(c4, r2, 0, 0);
        Solution s2 = solutionConverter.toSolution(p2Builder.build());

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        TimetableWithRooms child1 = solutionConverter.fromSolution(kids[0]);

        // regardless of which one is picked for replication,
        // there will already be a conflicting lesson in that spot.
        // so it will be inserted into a preferred period, which is the one it was before

        assertNotNull(child1.getMeeting(c1, 0, 0));
        assertNotNull(child1.getMeeting(c4, 1, 0));

        // other one should be somewhere
        assertEquals(2, child1.getMeetings().size());
    }

    @Test
    public void shouldNotPlaceInViolationOfRoomConstraints() {
        TimetableWithRooms.Builder p1Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p1Builder.addMeeting(c1, r1, 0, 0);
        p1Builder.addMeeting(c2, r1, 1, 0);
        Solution s1 = solutionConverter.toSolution(p1Builder.build());

        TimetableWithRooms.Builder p2Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p2Builder.addMeeting(c1, r1, 1, 0); // if picked: fail to put in 1/0 where c2 already is
        p2Builder.addMeeting(c2, r1, 0, 0); // if picked: fail to put in 0/0 where c1 already is
        Solution s2 = solutionConverter.toSolution(p2Builder.build());

        // usually both could be scheduled with the other
        roomConstraints.addRoomConstraint(c2, r2);
        roomConstraints.addRoomConstraint(c1, r2);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        TimetableWithRooms child1 = solutionConverter.fromSolution(kids[0]);

        // regardless of which one is picked for replication, they will
        // be exchanged, because they cannot be placed alongside each
        // other which means whichever is picked replaces the old
        // meeting and then the other one is scheduled at the preferred
        // period
        assertNotNull(child1.getMeeting(c2, 0 ,0));
        assertNotNull(child1.getMeeting(c1, 1, 0));

        // no others should be here
        assertEquals(2, child1.getMeetings().size());
    }

    @Test
    public void shouldNotPlaceInViolationOfUnavailabilityConstraints() {
        TimetableWithRooms.Builder p1Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p1Builder.addMeeting(c1, r1, 0, 0);
        Solution s1 = solutionConverter.toSolution(p1Builder.build());

        TimetableWithRooms.Builder p2Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p2Builder.addMeeting(c1, r1, 1, 1);
        Solution s2 = solutionConverter.toSolution(p2Builder.build());

        // c1 should be replicated in child1 at 1/1, but it's unavailable there
        unavailabilityConstraints.addUnavailability(c1, 1, 1);

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        TimetableWithRooms child1 = solutionConverter.fromSolution(kids[0]);

        // c1 should not have been placed at the unavailable period
        assertNull(child1.getMeeting(c1, 1, 1));
        // but it should be somewhere
        assertTrue(child1.getMeetings().stream()
                .filter(m -> m.getCourse().equals(c1))
                .findAny().isPresent());
    }

    @Test
    public void shouldReplicateAllBothSides() {
        TimetableWithRooms.Builder p1Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p1Builder.addMeeting(c1, r1, 0, 0);
        p1Builder.addMeeting(c1, r2, 0, 1);
        p1Builder.addMeeting(c1, r1, 0, 2);
        Solution s1 = solutionConverter.toSolution(p1Builder.build());

        TimetableWithRooms.Builder p2Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p2Builder.addMeeting(c1, r1, 1, 0);
        p2Builder.addMeeting(c1, r2, 1, 1);
        p2Builder.addMeeting(c1, r1, 1, 2);
        Solution s2 = solutionConverter.toSolution(p2Builder.build());

        Solution kids[] = courseBasedCrossover.evolve(new Solution[]{s1, s2});
        TimetableWithRooms child1 = solutionConverter.fromSolution(kids[0]);
        TimetableWithRooms child2 = solutionConverter.fromSolution(kids[1]);

        // all from p2 should now be in child1
        assertEquals(3, child1.getMeetings().size());
        assertNotNull(child1.getMeeting(c1, 1, 0));
        assertNotNull(child1.getMeeting(c1, 1, 1));
        assertNotNull(child1.getMeeting(c1, 1, 2));

        // all from p1 should now be in child2
        assertEquals(3, child2.getMeetings().size());
        assertNotNull(child2.getMeeting(c1, 0, 0));
        assertNotNull(child2.getMeeting(c1, 0, 1));
        assertNotNull(child2.getMeeting(c1, 0, 2));
    }

    @Test
    public void shouldNotModifyParents() {
        TimetableWithRooms.Builder p1Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p1Builder.addMeeting(c1, r1, 0, 0);
        p1Builder.addMeeting(c2, r2, 0, 0);
        p1Builder.addMeeting(c3, r1, 0, 1);
        TimetableWithRooms parent1 = p1Builder.build();
        Solution s1 = solutionConverter.toSolution(parent1);

        TimetableWithRooms.Builder p2Builder = TimetableWithRooms.Builder.newBuilder(spec);
        p2Builder.addMeeting(c1, r2, 1, 1);
        p2Builder.addMeeting(c2, r1, 1, 0);
        p2Builder.addMeeting(c3, r1, 0, 1);
        TimetableWithRooms parent2 = p2Builder.build();
        Solution s2 = solutionConverter.toSolution(parent2);

        courseBasedCrossover.evolve(new Solution[]{s1, s2});

        assertEquals(3, parent1.getMeetings().size());
        assertNotNull(parent1.getMeeting(c1, 0, 0));
        assertNotNull(parent1.getMeeting(c2, 0, 0));
        assertNotNull(parent1.getMeeting(c3, 0, 1));

        assertEquals(3, parent2.getMeetings().size());
        assertNotNull(parent2.getMeeting(c1, 1, 1));
        assertNotNull(parent2.getMeeting(c2, 1, 0));
        assertNotNull(parent2.getMeeting(c3, 0, 1));
    }


    @Test
    public void shouldProduceFeasibleOffspringsFromRealSample() throws Exception {
        String filename = String.format("comp%02d.ectt", 19);
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ECTTParser parser = new ECTTParser(br);
        Specification spec = parser.parse();
        Formulation v = new UD1Formulation(spec);
        RoomAssigner roomAssigner = new GreedyRoomAssigner(spec);
        List<TimetableWithRooms> ts = new TeacherGreedyInitializer(spec, roomAssigner).initialize(2);

        SolutionConverter solutionConverter = new SolutionConverter(v);
        Solution parents[] = new Solution[]{solutionConverter.toSolution(ts.get(0)), solutionConverter.toSolution(ts.get(1))};
        AbstractLessonBasedCrossover courseBasedCrossover = new CourseBasedCrossover(spec, solutionConverter, roomAssigner);

        int runs = 160; // repeated, because crossover involves randomness
        while (runs-- >= 0) {
            Solution kids[] = courseBasedCrossover.evolve(parents);
            TimetableWithRooms offspring1 = solutionConverter.fromSolution(kids[0]);
            TimetableWithRooms offspring2 = solutionConverter.fromSolution(kids[1]);

            for (Constraint c : v.getConstraints()) {
                assertEquals(0, c.violations(offspring1));
            }
            for (Constraint c : v.getConstraints()) {
                assertEquals(0, c.violations(offspring2));
            }

            parents = kids;
        }
    }
}