package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Room;

/**
 * @author Reto Habluetzel, 2015
 */
public class Meeting {

    private final Course course;

    private final Room room;

    private final int day;

    private final int period;

    public Meeting(Course course, Room room, int day, int period) {
        this.course = course;
        this.room = room;
        this.day = day;
        this.period = period;
    }

    public Meeting copy(Room room) {
        return new Meeting(course, room, day, period);
    }

    public Course getCourse() {
        return course;
    }

    public Room getRoom() {
        return room;
    }

    public int getDay() {
        return day;
    }

    public int getPeriod() {
        return period;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Meeting{");
        sb.append("course=").append(course);
        sb.append(", room=").append(room);
        sb.append(", day=").append(day);
        sb.append(", period=").append(period);
        sb.append('}');
        return sb.toString();
    }
}
