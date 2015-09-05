package ch.rethab.cbctt.ea.phenotype;

import ch.rethab.cbctt.domain.*;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Reto Habluetzel, 2015
 */
public class PeriodRoomAssignmentsTest {

    int day = 1;
    int period = 1;

    RoomConstraints roomConstraints = new RoomConstraints();

    UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(day, period);

    Room r1 = new Room("r1", 0, 1);
    Room r2 = new Room("r2", 1, 1);
    Room r3 = new Room("r3", 2, 1);
    Room r4 = new Room("r4", 3, 1);

    Curriculum cur1 = new Curriculum("curr1");
    Curriculum cur2 = new Curriculum("curr2");
    Curriculum cur3 = new Curriculum("curr3");
    Curriculum cur4 = new Curriculum("curr4");

    Course c1 = Course.Builder.id("c1")
            .curriculum(cur1)
            .teacher("t1").doubleLectures(false).nWorkingDays(2)
            .nStudents(4)
            .build();

    Course c2 = Course.Builder.id("c2")
            .curriculum(cur2)
            .teacher("t2").doubleLectures(false).nWorkingDays(2)
            .nStudents(4)
            .build();

    Course c3 = Course.Builder.id("c3")
            .curriculum(cur3)
            .teacher("t3").doubleLectures(false).nWorkingDays(2)
            .nStudents(4)
            .build();

    Course c4 = Course.Builder.id("c4")
            .curriculum(cur4)
            .teacher("t4").doubleLectures(false).nWorkingDays(2)
            .nStudents(4)
            .build();

    Specification spec = Specification.Builder.name("spec")
            .days(day).periodsPerDay(period)
            .room(r1).room(r2).room(r3).room(r4)
            .course(c1).course(c2).course(c3).course(c4)
            .curriculum(cur1).curriculum(cur2).curriculum(cur3).curriculum(cur4)
            .minLectures(1).maxLectures(2)
            .roomConstraints(roomConstraints)
            .unavailabilityConstraints(unavailabilityConstraints)
            .build();

    @Test
    public void shouldUpdateRoomConstrainednessDuringAssignment() {
        PeriodRoomAssignments pra = new PeriodRoomAssignments(spec);
        /* Idea: (c=constraint, f=free, o=occupied by other, r=rooms)
         *
         *  r = 1 2 3 4
         * c1 = c c c f --> 1
         * c2 = c c f f --> 2
         * c3 = f f c c --> 2
         * c4 = c f f f --> 3
         *
         * --> c4 is easiest to assignRooms in the beginning, but
         *     after c1 and c2 have been scheduled, c4 will be
         *     harder to assignRooms than c3.
         *     this means c4 must get a better room than c3.
         */

        roomConstraints.addRoomConstraint(c1, r1);
        roomConstraints.addRoomConstraint(c1, r2);
        roomConstraints.addRoomConstraint(c1, r3);
        roomConstraints.addRoomConstraint(c2, r1);
        roomConstraints.addRoomConstraint(c2, r2);
        roomConstraints.addRoomConstraint(c3, r3);
        roomConstraints.addRoomConstraint(c3, r4);
        roomConstraints.addRoomConstraint(c4, r1);

        // all courses have too many students. since r1 is the smallest,
        // whichever course is assigned to that, was scheduled last
        assertTrue(pra.add(c1));
        assertTrue(pra.add(c2));
        assertTrue(pra.add(c3));
        assertTrue(pra.add(c4));

        List<PeriodRoomAssignments.CourseWithRoom> courses = pra.assignRooms();
        assertEquals(4, courses.size());
        Optional<PeriodRoomAssignments.CourseWithRoom> c1wr = courses.stream().filter(cwr -> cwr.course.equals(c1)).findFirst();
        Optional<PeriodRoomAssignments.CourseWithRoom> c2wr = courses.stream().filter(cwr -> cwr.course.equals(c2)).findFirst();
        Optional<PeriodRoomAssignments.CourseWithRoom> c3wr = courses.stream().filter(cwr -> cwr.course.equals(c3)).findFirst();
        Optional<PeriodRoomAssignments.CourseWithRoom> c4wr = courses.stream().filter(cwr -> cwr.course.equals(c4)).findFirst();
        assertEquals(r4, c1wr.get().room);
        assertEquals(r3, c2wr.get().room);
        assertEquals(r2, c4wr.get().room);
        assertEquals(r1, c3wr.get().room);
    }

}