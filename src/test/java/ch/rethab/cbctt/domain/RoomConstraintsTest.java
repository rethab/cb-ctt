package ch.rethab.cbctt.domain;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class RoomConstraintsTest {

    Course c1 = Course.Builder.id("c1")
            .teacher("t1")
            .curriculum(new Curriculum("curr1"))
            .doubleLectures(false).nStudents(3).nWorkingDays(1)
            .build();

    Room r1 = new Room("r1", 1, 0);
    Room r2 = new Room("r2", 1, 0);
    Room r3 = new Room("r3", 1, 0);

    @Test
    public void shouldReportRoomAsUnsuitableIfAdded() {
        RoomConstraints roomConstraints = new RoomConstraints();
        roomConstraints.addRoomConstraint(c1, r1);
        assertTrue(roomConstraints.isUnsuitable(c1, r1));
    }

    @Test
    public void shouldReportRoomAsSuitableIfNotAdded() {
        RoomConstraints roomConstraints = new RoomConstraints();
        roomConstraints.addRoomConstraint(c1, r1);
        assertFalse(roomConstraints.isUnsuitable(c1, r2));
    }

    @Test
    public void shouldReportRoomAsUnsuitableIfAddedMultiple() {
        RoomConstraints roomConstraints = new RoomConstraints();
        roomConstraints.addRoomConstraint(c1, r1);
        roomConstraints.addRoomConstraint(c1, r2);
        roomConstraints.addRoomConstraint(c1, r3);
        assertTrue(roomConstraints.isUnsuitable(c1, r1));
        assertTrue(roomConstraints.isUnsuitable(c1, r2));
        assertTrue(roomConstraints.isUnsuitable(c1, r3));
    }

}