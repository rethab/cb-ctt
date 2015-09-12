package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.ea.phenotype.Timetable;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.moea.SolutionConverter;
import org.junit.Test;
import org.moeaframework.core.Solution;

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
    CourseBasedMutation courseBasedMutation = new CourseBasedMutation(solutionConverter, roomAssigner);

    @Test
    public void shouldExchangeTwoCourses() {
        TimetableWithRooms t = TimetableWithRooms.Builder
                .newBuilder(spec)
                .addMeeting(c1, r1, 0, 0)
                .addMeeting(c2, r1, 0, 1)
                .build();

        Solution sol = courseBasedMutation.evolve(new Solution[] {solutionConverter.toSolution(t)})[0];
        TimetableWithRooms mutated = solutionConverter.fromSolution(sol);

        // the two should have been exchanged
        assertNotNull(mutated.getMeeting(c1, 0, 1));
        assertNotNull(mutated.getMeeting(c2, 0, 0));

        // total size should not have changed
        assertEquals(2, mutated.getMeetings().size());
    }

    @Test
    public void shouldNotModifyOriginal() {
        TimetableWithRooms t = TimetableWithRooms.Builder
                .newBuilder(spec)
                .addMeeting(c1, r1, 0, 0)
                .addMeeting(c2, r1, 0, 1)
                .build();

        courseBasedMutation.evolve(new Solution[] {solutionConverter.toSolution(t)});

        assertNotNull(t.getMeeting(c1, 0, 0));
        assertNotNull(t.getMeeting(c2, 0, 1));

        // total size should not have changed
        assertEquals(2, t.getMeetings().size());
    }
}