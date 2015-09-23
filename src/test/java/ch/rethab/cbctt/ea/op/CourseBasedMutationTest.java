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
import org.junit.Test;
import org.moeaframework.core.Solution;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class CourseBasedMutationTest {


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
    CourseBasedMutation courseBasedMutation = new CourseBasedMutation(spec, solutionConverter, roomAssigner, 1);

    @Test
    public void shouldExchangeTwoCourses() {
        TimetableWithRooms t = TimetableWithRooms.Builder
                .newBuilder(spec)
                .addMeeting(c1, r1, 0, 0)
                .addMeeting(c4, r1, 0, 1)
                .build();

        Solution sol = courseBasedMutation.evolve(new Solution[] {solutionConverter.toSolution(t)})[0];
        TimetableWithRooms mutated = solutionConverter.fromSolution(sol);

        // the two should have been exchanged
        assertNotNull(mutated.getMeeting(c1, 0, 1));
        assertNotNull(mutated.getMeeting(c4, 0, 0));

        // total size should not have changed
        assertEquals(2, mutated.getMeetings().size());
    }

    @Test
    public void shouldNotMutateWithProbabilityZero() {
        TimetableWithRooms t = TimetableWithRooms.Builder
                .newBuilder(spec)
                .addMeeting(c1, r1, 0, 0)
                .addMeeting(c2, r2, 0, 2)
                .addMeeting(c4, r1, 0, 1)
                .build();

        CourseBasedMutation courseBasedMutation = new CourseBasedMutation(spec, solutionConverter, roomAssigner, 0);
        Solution sol = courseBasedMutation.evolve(new Solution[] {solutionConverter.toSolution(t)})[0];
        TimetableWithRooms mutated = solutionConverter.fromSolution(sol);

        // nohting should have moved
        assertNotNull(mutated.getMeeting(c1, 0, 0));
        assertNotNull(mutated.getMeeting(c2, 0, 2));
        assertNotNull(mutated.getMeeting(c4, 0, 1));

        // total size should not have changed
        assertEquals(3, mutated.getMeetings().size());
    }

    @Test
    public void shouldExchangeOnlyTwoCourses() {
        TimetableWithRooms t = TimetableWithRooms.Builder
                .newBuilder(spec)
                .addMeeting(c1, r1, 0, 0)
                .addMeeting(c2, r1, 0, 1)
                .addMeeting(c4, r1, 0, 2)
                .build();

        Solution sol = courseBasedMutation.evolve(new Solution[] {solutionConverter.toSolution(t)})[0];
        TimetableWithRooms mutated = solutionConverter.fromSolution(sol);

        // one of them should have moved but not both
        if (mutated.getMeeting(c1, 0, 0) == null) {
            assertTrue(mutated.getMeeting(c2, 0, 1) != null || mutated.getMeeting(c4, 0, 2) != null);
            assertFalse(mutated.getMeeting(c2, 0, 1) != null && mutated.getMeeting(c4, 0, 2) != null);
        } else if (mutated.getMeeting(c2, 0, 1) == null) {
            assertTrue(mutated.getMeeting(c1, 0, 0) != null || mutated.getMeeting(c4, 0, 2) != null);
            assertFalse(mutated.getMeeting(c1, 0, 0) != null && mutated.getMeeting(c4, 0, 2) != null);
        } else if (mutated.getMeeting(c4, 0, 1) == null) {
            assertTrue(mutated.getMeeting(c1, 0, 0) != null || mutated.getMeeting(c2, 0, 1) != null);
            assertFalse(mutated.getMeeting(c1, 0, 0) != null && mutated.getMeeting(c2, 0, 1) != null);
        } else {
            fail("Something should have moved!");
        }

        // total size should not have changed
        assertEquals(3, mutated.getMeetings().size());
    }

    @Test
    public void shouldNotModifyOriginal() {
        TimetableWithRooms t = TimetableWithRooms.Builder
                .newBuilder(spec)
                .addMeeting(c1, r1, 0, 0)
                .addMeeting(c4, r1, 0, 1)
                .build();

        courseBasedMutation.evolve(new Solution[] {solutionConverter.toSolution(t)});

        assertNotNull(t.getMeeting(c1, 0, 0));
        assertNotNull(t.getMeeting(c4, 0, 1));

        // total size should not have changed
        assertEquals(2, t.getMeetings().size());
    }

    @Test
    public void shouldProduceFeasibleOffspringsFromRealSample1() throws Exception {
        String filename = String.format("comp%02d.ectt", 1);
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ECTTParser parser = new ECTTParser(br);
        Specification spec = parser.parse();
        Formulation v = new UD1Formulation(spec);
        RoomAssigner roomAssigner = new GreedyRoomAssigner(spec);
        List<TimetableWithRooms> ts = new TeacherGreedyInitializer(spec, roomAssigner).initialize(2);

        SolutionConverter solutionConverter = new SolutionConverter(v);
        CourseBasedMutation mutation = new CourseBasedMutation(spec, solutionConverter, roomAssigner, 1);

        TimetableWithRooms tt = ts.get(0);

        int rounds = 320;
        do {
            Solution kid = mutation.evolve(new Solution[]{ solutionConverter.toSolution(tt) })[0];
            tt = solutionConverter.fromSolution(kid);
            for (Constraint c : v.getConstraints()) {
                assertEquals(c.name() + " has violations", 0, c.violations(tt));
            }
        } while (rounds-- >= 0);
    }

    @Test
    public void shouldProduceFeasibleOffspringsFromRealSample19() throws Exception {
        String filename = String.format("comp%02d.ectt", 19);
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ECTTParser parser = new ECTTParser(br);
        Specification spec = parser.parse();
        Formulation v = new UD1Formulation(spec);
        RoomAssigner roomAssigner = new GreedyRoomAssigner(spec);
        List<TimetableWithRooms> ts = new TeacherGreedyInitializer(spec, roomAssigner).initialize(1);

        SolutionConverter solutionConverter = new SolutionConverter(v);
        CourseBasedMutation mutation = new CourseBasedMutation(spec, solutionConverter, roomAssigner, 1);

        TimetableWithRooms tt = ts.get(0);

        int rounds = 320;
        do {
            Solution kid = mutation.evolve(new Solution[]{ solutionConverter.toSolution(tt) })[0];
            tt = solutionConverter.fromSolution(kid);
            for (Constraint c : v.getConstraints()) {
                assertEquals(c.name() + " has violations", 0, c.violations(tt));
            }
        } while (rounds-- >= 0);
    }
}