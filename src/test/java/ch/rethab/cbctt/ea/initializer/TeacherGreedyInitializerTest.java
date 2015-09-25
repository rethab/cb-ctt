package ch.rethab.cbctt.ea.initializer;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.Meeting;
import ch.rethab.cbctt.ea.phenotype.Timetable;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.formulation.constraint.Constraint;
import ch.rethab.cbctt.parser.ECTTParser;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class TeacherGreedyInitializerTest {


    @Test
    public void shouldProduceValidToyTimetable() {
        Specification toySpec = testData();
        Initializer teacherGreedyInitializer = new TeacherGreedyInitializer(toySpec, new GreedyRoomAssigner(toySpec));
        Formulation v = new UD1Formulation(toySpec);

        for (TimetableWithRooms t : teacherGreedyInitializer.initialize(20)) {
            for (Constraint c : v.getConstraints()) {
                assertEquals(0, c.violations(t));
            }
        }
    }

    @Test
    public void shouldNotBlowUpDuringInitializationInMultiThreadedEnvironment() throws Exception {
        String filename = "comp01.ectt";
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ECTTParser parser = new ECTTParser(br);
        Specification spec = parser.parse();

        List<Callable<List<TimetableWithRooms>>> tasks = new LinkedList<>();
        for (int i = 0; i < 100; i++) {
            tasks.add(() -> new TeacherGreedyInitializer(spec, new GreedyRoomAssigner(spec)).initialize(30));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        try {
            executorService.invokeAll(tasks).forEach(f -> {
                try {
                    // get is required to make them blow up
                    f.get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    public void shouldPorduceFeasbibleTimetablesInMultiThreadedEnvironment() throws Exception {
        String filename = "comp01.ectt";
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ECTTParser parser = new ECTTParser(br);
        Specification spec = parser.parse();
        Formulation formulation = new UD1Formulation(spec);

        List<Callable<List<TimetableWithRooms>>> tasks = new LinkedList<>();
        for (int i = 0; i < 100; i++) {
            tasks.add(() -> new TeacherGreedyInitializer(spec, new GreedyRoomAssigner(spec)).initialize(1));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<Future<List<TimetableWithRooms>>> tts;
        try {
            tts = executorService.invokeAll(tasks);
        } finally {
            executorService.shutdownNow();
        }
        for (Future<List<TimetableWithRooms>> tt : tts) {
            List<TimetableWithRooms> ttwrs = tt.get(1, TimeUnit.MINUTES);
            for (TimetableWithRooms ttwr : ttwrs) {
                for (Constraint constraint : formulation.getConstraints()) {
                    assertEquals("Violates constraint " + constraint.name(), 0, constraint.violations(ttwr));
                }
            }
        }

    }

    @Test
    public void shouldProduceFeasibleTimetablesForCompTests() throws IOException {
        for (int i = 1; i <= 21; i++) {
            String filename = String.format("comp%02d.ectt", i);
            InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            ECTTParser parser = new ECTTParser(br);
            Specification spec = parser.parse();
            Initializer teacherGreedyInitializer = new TeacherGreedyInitializer(spec, new GreedyRoomAssigner(spec));
            Formulation v = new UD1Formulation(spec);
            TimetableWithRooms t = teacherGreedyInitializer.initialize(1).get(0);
            for (Constraint c : v.getConstraints()) {
                assertEquals(0, c.violations(t));
            }
        }
    }

    @Test
    public void shouldProduceDistinctTimetables() throws IOException {
        String filename = "comp01.ectt";
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ECTTParser parser = new ECTTParser(br);
        Specification spec = parser.parse();
        Initializer teacherGreedyInitializer = new TeacherGreedyInitializer(spec, new GreedyRoomAssigner(spec));
        List<TimetableWithRooms> ts = teacherGreedyInitializer.initialize(10);
        assertDistinct(ts, 0, 1); assertDistinct(ts, 0, 2); assertDistinct(ts, 0, 3);
        assertDistinct(ts, 0, 4); assertDistinct(ts, 0, 5); assertDistinct(ts, 0, 6);
        assertDistinct(ts, 0, 7); assertDistinct(ts, 0, 8); assertDistinct(ts, 0, 9);
        assertDistinct(ts, 1, 2); assertDistinct(ts, 1, 3); assertDistinct(ts, 1, 4);
        assertDistinct(ts, 1, 5); assertDistinct(ts, 1, 6); assertDistinct(ts, 1, 7);
        assertDistinct(ts, 1, 8); assertDistinct(ts, 1, 9); assertDistinct(ts, 2, 3);
        assertDistinct(ts, 2, 4); assertDistinct(ts, 2, 5); assertDistinct(ts, 2, 6);
        assertDistinct(ts, 2, 7); assertDistinct(ts, 2, 8); assertDistinct(ts, 2, 9);
        assertDistinct(ts, 3, 4); assertDistinct(ts, 3, 5); assertDistinct(ts, 3, 6);
        assertDistinct(ts, 3, 7); assertDistinct(ts, 3, 8); assertDistinct(ts, 3, 9);
        assertDistinct(ts, 4, 5); assertDistinct(ts, 4, 6); assertDistinct(ts, 4, 7);
        assertDistinct(ts, 4, 8); assertDistinct(ts, 4, 9); assertDistinct(ts, 5, 6);
        assertDistinct(ts, 5, 7); assertDistinct(ts, 5, 8); assertDistinct(ts, 5, 9);
        assertDistinct(ts, 6, 7); assertDistinct(ts, 6, 8); assertDistinct(ts, 6, 9);
        assertDistinct(ts, 7, 8); assertDistinct(ts, 7, 9); assertDistinct(ts, 8, 9);
    }

    private void assertDistinct(List<TimetableWithRooms> ts, int i1, int i2) {
        assertTrue(i1 + " and " + i2 + " are the same", ts.get(i1).getMeetings().stream().anyMatch(
                m1 -> ts.get(i2).getMeeting(m1.getCourse(), m1.getDay(), m1.getPeriod()) == null
        ));
    }

    @Test
    public void shouldProduceCorrectAmount() {
        Specification toySpec = testData();
        Initializer teacherGreedyInitializer = new TeacherGreedyInitializer(toySpec, new GreedyRoomAssigner(toySpec));
        assertEquals(1, teacherGreedyInitializer.initialize(1).size());
        assertEquals(10, teacherGreedyInitializer.initialize(10).size());
    }

    @Test
    public void shouldBeConsistentlyCorrect() throws IOException {
        String filename = "comp19.ectt";
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ECTTParser parser = new ECTTParser(br);
        Specification spec = parser.parse();
        UD1Formulation formulation = new UD1Formulation(spec);
        Initializer teacherGreedyInitializer = new TeacherGreedyInitializer(spec, new GreedyRoomAssigner(spec));
        for (TimetableWithRooms timetable : teacherGreedyInitializer.initialize(200)) {
            for (Constraint constraint : formulation.getConstraints()) {
                assertEquals(constraint.name() + " has violations", 0, constraint.violations(timetable));
            }

        }
    }

    @Test
    public void shouldBeAbleToReproduceItelself() throws IOException {
        String filename = "comp01.ectt";
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ECTTParser parser = new ECTTParser(br);
        Specification spec = parser.parse();
        Initializer teacherGreedyInitializer = new TeacherGreedyInitializer(spec, new GreedyRoomAssigner(spec));
        for (TimetableWithRooms timetable : teacherGreedyInitializer.initialize(100)) {
            Timetable offspring = timetable.newChild();
            for (Meeting offspringMeeting : offspring.getMeetings()) {
                assertNotNull(timetable.getMeeting(offspringMeeting.getCourse(), offspringMeeting.getDay(), offspringMeeting.getPeriod()));
            }
        }
    }

    private Specification testData() {

        Specification.Builder builder = Specification.Builder.name("Toy");

        builder.days(5).periodsPerDay(4);
        builder.minLectures(2).maxLectures(3);

        Room rA = new Room("rA", 32, 1);
        Room rB = new Room("rB", 50, 0);
        Room rC = new Room("rC", 40, 0);
        builder.room(rA).room(rB).room(rC);

        Curriculum cur1 = new Curriculum("Cur1");
        Curriculum cur2 = new Curriculum("Cur2");
        builder.curriculum(cur1).curriculum(cur2);

        Course sceCosc = Course.Builder.id("SceCosC").curriculum(cur1).teacher("Ocra").nlectures(3).nWorkingDays(3).nStudents(30).doubleLectures(true).build();
        Course arcTec = Course.Builder.id("ArcTec").curriculum(cur1).teacher("Indaco").nlectures(3).nWorkingDays(2).nStudents(42).doubleLectures(false).build();
        Course tecCos = Course.Builder.id("TecCos").curriculum(cur1).curriculum(cur2).teacher("Rosa").nlectures(5).nWorkingDays(4).nStudents(40).doubleLectures(false).build();
        Course geotec = Course.Builder.id("Geotec").curriculum(cur2).teacher("Scarlatti").nlectures(5).nWorkingDays(4).nStudents(18).doubleLectures(true).build();
        builder.course(sceCosc).course(arcTec).course(tecCos).course(geotec);

        cur1.setCourses(Arrays.asList(sceCosc, arcTec, tecCos));
        cur2.setCourses(Arrays.asList(tecCos, geotec));

        UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(5, 4);
        unavailabilityConstraints.addUnavailability(tecCos, 2, 0);
        unavailabilityConstraints.addUnavailability(tecCos, 2, 1);
        unavailabilityConstraints.addUnavailability(tecCos, 3, 2);
        unavailabilityConstraints.addUnavailability(tecCos, 3, 3);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 0);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 1);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 2);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 3);
        builder.unavailabilityConstraints(unavailabilityConstraints);

        RoomConstraints roomConstraints = new RoomConstraints();
        roomConstraints.addRoomConstraint(sceCosc, rA);
        roomConstraints.addRoomConstraint(geotec, rB);
        roomConstraints.addRoomConstraint(tecCos, rC);
        builder.roomConstraints(roomConstraints);

        return builder.build();
    }

    @Test
    public void shouldReachAllIndicesWithFlatTimetable() {
        // which periods are reached is based on a PRNG, which
        // mean it is possible that one period is not reached,
        // however after three attempts it should be *very*
        // unlikely to still not be reached
        int a = getUnreachedPeriod();
        int b = getUnreachedPeriod();
        int c = getUnreachedPeriod();
        int d = getUnreachedPeriod();
        if (a != -1 && a == b && a == c && a == d) {
            fail("Four times the same period not reached. Probably buggy!");
        }

    }

    private int getUnreachedPeriod() {
        Specification spec = testData();
        TeacherGreedyInitializer.FlatTimetable t;
        boolean reachedPeriods[];
        for (int days = 2; days <= spec.getNumberOfDaysPerWeek()-1; days++) {
            for (int periods = 2; periods <= spec.getPeriodsPerDay()-1; periods++) {
                t = new TeacherGreedyInitializer.FlatTimetable(spec);
                reachedPeriods = new boolean[spec.getNumberOfDaysPerWeek()*spec.getPeriodsPerDay()];

                for (int i = 0; i < spec.getNumberOfDaysPerWeek() * spec.getPeriodsPerDay(); i++) {
                    int x = t.getX();
                    reachedPeriods[x] = true;
                    t.advancePeriod();
                }

                for (int i = 0; i < days * periods; i++) {
                    if (!reachedPeriods[i]) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

}
