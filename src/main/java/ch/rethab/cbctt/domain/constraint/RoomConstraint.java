package ch.rethab.cbctt.domain.constraint;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Room;

import java.util.Objects;

/**
 * @author Reto Habluetzel, 2015
 */
public class RoomConstraint implements Constraint {

    private final Course course;
    private final Room room;

    public RoomConstraint(Course course, Room room) {
        this.course = course;
        this.room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomConstraint that = (RoomConstraint) o;
        return Objects.equals(course, that.course) &&
                Objects.equals(room, that.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, room);
    }

    public Course getCourse() {
        return course;
    }

    public Room getRoom() {
        return room;
    }
}
