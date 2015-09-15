package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.moea.SolutionConverter;
import org.junit.Test;
import org.moeaframework.core.Solution;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class CurriculumBasedCrossoverTest {

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
    CurriculumBasedCrossover curriculumBasedCrossover = new CurriculumBasedCrossover(solutionConverter, roomAssigner, spec);

    @Test
    public void shouldReplicateEntireCurriculum() {
        Solution p1 = solutionConverter.toSolution(TimetableWithRooms.Builder.newBuilder(spec)
            .addMeeting(c1, r1, 0, 0).addMeeting(c1, r1, 0, 1)
            .addMeeting(c2, r2, 0, 2)
            .addMeeting(c3, r2, 1, 0)
            .addMeeting(c4, r1, 2, 0).addMeeting(c4, r1, 2, 1)
            .build());

        Solution p2 = solutionConverter.toSolution(TimetableWithRooms.Builder.newBuilder(spec)
                .addMeeting(c2, r2, 0, 0)
                .addMeeting(c3, r1, 0, 0)
                .addMeeting(c1, r1, 0, 1).addMeeting(c1, r2, 1, 0)
                .addMeeting(c4, r1, 1, 1).addMeeting(c4, r1, 1, 2)
                .build());

        Solution[] kids = curriculumBasedCrossover.evolve(new Solution[]{p1, p2});
        TimetableWithRooms child2 = solutionConverter.fromSolution(kids[1]);

        // moved curr3: c3
        boolean c3Moved =
               // should have stayed
               child2.getMeeting(c1, 0, 1) != null
            && child2.getMeeting(c1, 1, 0) != null
            && child2.getMeeting(c2, 0, 0) != null
            && child2.getMeeting(c4, 1, 1) != null
            && child2.getMeeting(c4, 1, 2) != null
               // should have moved here
            && child2.getMeeting(c3, 1, 0) != null;

        // moved curr2: c2, c4
        boolean c2Moved =
                // should have stayed
                child2.getMeeting(c1, 0, 1) != null
             && child2.getMeeting(c3, 0, 0) != null
             && child2.getMeeting(c1, 1, 0) != null
                // should have moved here
             && child2.getMeeting(c2, 0, 2) != null
             && child2.getMeeting(c4, 2, 0) != null
             && child2.getMeeting(c4, 2, 1) != null;


        // moved curr1: c1, c4
        boolean c1Moved =
               // was moved here
               child2.getMeeting(c1, 0, 0) != null
               // one should have stayed
            && (child2.getMeeting(c2, 0, 0) != null || child2.getMeeting(c3, 0, 0) != null)
               // was moved here
            && child2.getMeeting(c1, 0, 1) != null
               // was moved here
            && child2.getMeeting(c4, 2, 0) != null
               // was moved here
            && child2.getMeeting(c4, 2, 1) != null
            && 6 == child2.getMeetings().size();

        // one of them should have moved..
        assertTrue(c1Moved || c2Moved || c3Moved);
        // ..but not all
        assertFalse(c1Moved && c2Moved && c3Moved);
    }

}