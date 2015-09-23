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
public class SectorBasedCrossoverTest {

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

    @Test
    public void shouldMoveSpecifiedNumberOfLessons() {
        int sectorSize = 2; // two means the entire curriculum is moved
        SectorBasedCrossover sectorBasedCrossover = new SectorBasedCrossover(spec, solutionConverter, roomAssigner, sectorSize);

        Solution parent1 = solutionConverter.toSolution(TimetableWithRooms.Builder.newBuilder(spec)
                .addMeeting(c1, r1, 0, 0).addMeeting(c1, r1, 0, 1)
                .addMeeting(c2, r1, 1, 1).addMeeting(c2, r1, 1, 2)
                .addMeeting(c3, r2, 0, 0).addMeeting(c3, r2, 0, 1)
                .build()
        );

        Solution parent2 = solutionConverter.toSolution(TimetableWithRooms.Builder.newBuilder(spec)
                .addMeeting(c1, r1, 1, 2).addMeeting(c1, r2, 2, 2)
                .addMeeting(c2, r1, 1, 1).addMeeting(c2, r1, 2, 2)
                .addMeeting(c3, r1, 2, 0).addMeeting(c3, r2, 2, 1)
                .build()
        );

        // in the past, this has only failed occasionally, so we leave the loop
        for (int i = 0; i < 100; i++) {
            Solution[] kids = sectorBasedCrossover.evolve(new Solution[]{parent1, parent2});
            TimetableWithRooms kid1 = solutionConverter.fromSolution(kids[0]);
            assertEquals(6, kid1.getMeetings().size());

            boolean curr1Moved = kid1.getMeeting(c1, 1, 2) != null && kid1.getMeeting(c1, 2, 2) != null;
            boolean curr2Moved = kid1.getMeeting(c2, 1, 1) != null && kid1.getMeeting(c2, 2, 2) != null;
            boolean curr3Moved = kid1.getMeeting(c3, 2, 0) != null && kid1.getMeeting(c3, 2, 1) != null;
            // one should have moved..
            assertTrue(curr1Moved || curr2Moved || curr3Moved);
            // ..but not both
            assertFalse(curr1Moved && curr2Moved && curr3Moved);
            assertEquals(6, kid1.getMeetings().size());
        }
    }

    @Test
    public void shouldNotScrewUpWithCourseInTwoCurricula() {
        int sectorSize = 2; // two means the entire curriculum is moved
        SectorBasedCrossover sectorBasedCrossover = new SectorBasedCrossover(spec, solutionConverter, roomAssigner, sectorSize);

        Solution parent1 = solutionConverter.toSolution(TimetableWithRooms.Builder.newBuilder(spec)
                .addMeeting(c1, r1, 0, 0)
                .addMeeting(c2, r1, 0, 0).addMeeting(c2, r1, 0, 1)
                .addMeeting(c3, r1, 0, 1).addMeeting(c3, r1, 0, 2)
                .addMeeting(c4, r1, 1, 0).addMeeting(c4, r2, 1, 1)
                .build()
        );

        Solution parent2 = solutionConverter.toSolution(TimetableWithRooms.Builder.newBuilder(spec)
                .addMeeting(c1, r1, 1, 0)
                .addMeeting(c2, r1, 1, 0).addMeeting(c2, r1, 1, 1)
                .addMeeting(c3, r1, 1, 1).addMeeting(c3, r1, 1, 2)
                .addMeeting(c4, r1, 1, 2).addMeeting(c4, r2, 2, 1)
                .build()
        );

        for (int i = 0; i < 20; i++) {
            Solution[] kids = sectorBasedCrossover.evolve(new Solution[]{parent1, parent2});
            TimetableWithRooms kid1 = solutionConverter.fromSolution(kids[0]);
            TimetableWithRooms kid2 = solutionConverter.fromSolution(kids[1]);
            assertEquals(7, kid1.getMeetings().size());
            assertEquals(7, kid2.getMeetings().size());
        }
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
        AbstractLessonBasedCrossover crossover = new SectorBasedCrossover(spec, solutionConverter, roomAssigner, 7);
        Solution kids[] = crossover.evolve(parents);
        TimetableWithRooms offspring1 = solutionConverter.fromSolution(kids[0]);
        TimetableWithRooms offspring2 = solutionConverter.fromSolution(kids[1]);

        for (Constraint c : v.getConstraints()) {
            assertEquals(0, c.violations(offspring1));
        }
        for (Constraint c : v.getConstraints()) {
            assertEquals(0, c.violations(offspring2));
        }
    }

    @Test
    public void shouldNotExplodeOnBounds() {
        Solution parent1 = solutionConverter.toSolution(TimetableWithRooms.Builder.newBuilder(spec)
            .addMeeting(c1, r1, 0, 0).addMeeting(c1, r2, 0, 1)
            .addMeeting(c2, r1, 1, 1).addMeeting(c2, r1, 1, 2)
            .addMeeting(c3, r1, 0, 0).addMeeting(c3, r2, 0, 1)
            .build()
        );

        Solution parent2 = solutionConverter.toSolution(TimetableWithRooms.Builder.newBuilder(spec)
                        .addMeeting(c1, r1, 1, 2).addMeeting(c1, r2, 2, 2)
                        .addMeeting(c2, r1, 1, 1).addMeeting(c2, r1, 2, 2)
                        .addMeeting(c3, r1, 2, 0).addMeeting(c3, r2, 2, 1)
                        .build()
        );

        for (int sectorSize = 0; sectorSize < 10; sectorSize++) {
            new SectorBasedCrossover(spec, solutionConverter, roomAssigner, sectorSize)
                    .evolve(new Solution[]{parent1, parent2});
        }
    }

}