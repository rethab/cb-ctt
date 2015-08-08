package ch.rethab.cbctt.ea.initializer;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.ea.Timetable;
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
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class TeacherGreedyTest {

    Initializer teacherGreedyInitializer = new TeacherGreedyInitializer();

    @Test
    public void shouldProduceValidToyTimetable() {
        Specification toySpec = testData();
        Formulation v = new UD1Formulation(toySpec);

        for (Timetable t : teacherGreedyInitializer.initialize(toySpec, 20)) {
            for (Constraint c : v.getConstraints()) {
                assertEquals(0, c.violations(t));
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
            Formulation v = new UD1Formulation(spec);
            Timetable t = teacherGreedyInitializer.initialize(spec, 1).get(0);
            for (Constraint c : v.getConstraints()) {
                assertEquals(0, c.violations(t));
            }
        }
    }

    @Test
    public void shouldProduceCorrectAmount() {
        assertEquals(1, teacherGreedyInitializer.initialize(testData(), 1).size());
        assertEquals(10, teacherGreedyInitializer.initialize(testData(), 10).size());
    }

    private Specification testData() {
        Course sceCosc = new Course("SceCosC", "Ocra", 3, 3, 30, true);
        Course arcTec = new Course("ArcTec", "Indaco", 3, 2, 42, false);
        Course tecCos = new Course("TecCos", "Rosa", 5, 4, 40, true);
        Course geotec = new Course("Geotec", "Scarlatti", 5, 4, 18, true);
        List<Course> courses = Arrays.asList(sceCosc, arcTec, tecCos, geotec);

        Room rA = new Room("rA", 32, 1);
        Room rB = new Room("rB", 50, 0);
        Room rC = new Room("rC", 40, 0);
        List<Room> rooms = Arrays.asList(rA, rB, rC);

        List<Curriculum> curricula = Arrays.asList(
                new Curriculum("Cur1", Arrays.asList(sceCosc, arcTec, tecCos)),
                new Curriculum("Cur2", Arrays.asList(tecCos, geotec))
        );

        UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(5, 4);
        unavailabilityConstraints.addUnavailability(tecCos, 2, 0);
        unavailabilityConstraints.addUnavailability(tecCos, 2, 1);
        unavailabilityConstraints.addUnavailability(tecCos, 3, 2);
        unavailabilityConstraints.addUnavailability(tecCos, 3, 3);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 0);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 1);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 2);
        unavailabilityConstraints.addUnavailability(arcTec, 4, 3);

        RoomConstraints roomConstraints = new RoomConstraints();
        roomConstraints.addRoomConstraint(sceCosc, rA);
        roomConstraints.addRoomConstraint(geotec, rB);
        roomConstraints.addRoomConstraint(tecCos, rC);

        return new Specification("Toy", 5, 4, 2, 3, courses, rooms, curricula, unavailabilityConstraints, roomConstraints);
    }

    @Test
    public void shouldReachAllIndicesWithFlatTimetable() {
        int ndays = 7;
        int nperiods = 10;
        TeacherGreedyInitializer.FlatTimetable t;
        boolean reachedPeriods[];
        for (int days = 2; days <= ndays-1; days++) {
            for (int periods = 2; periods <= nperiods-1; periods++) {
                t = new TeacherGreedyInitializer.FlatTimetable(days, periods);
                reachedPeriods = new boolean[ndays*nperiods];

                for (int i = 0; i < ndays * nperiods; i++) {
                    int x = t.getX();
                    reachedPeriods[x] = true;
                    t.advancePeriod();
                }

                for (int i = 0; i < days * periods; i++) {
                    assertTrue("Period "+i+" not reached", reachedPeriods[i]);
                }
            }
        }
    }

}
