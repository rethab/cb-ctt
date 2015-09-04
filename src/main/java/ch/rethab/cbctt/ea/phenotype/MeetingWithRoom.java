package ch.rethab.cbctt.ea.phenotype;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Room;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Reto Habluetzel, 2015
 */
public class MeetingWithRoom implements Serializable {

    private final Course course;

    private final Room room;

    private final int day;

    private final int period;

    public MeetingWithRoom(Course course, Room room, int day, int period) {
        this.course = course;
        this.room = room;
        this.day = day;
        this.period = period;
    }

    public Room getRoom() {
        return room;
    }

    public Course getCourse() {
        return course;
    }

    public int getDay() {
        return day;
    }

    public int getPeriod() {
        return period;
    }

    public Meeting withoutRoom() {
        return new Meeting(course, day, period);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingWithRoom meeting = (MeetingWithRoom) o;
        return Objects.equals(day, meeting.day) &&
                Objects.equals(period, meeting.period) &&
                Objects.equals(room, meeting.room) &&
                Objects.equals(course, meeting.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, room, day, period);
    }
}
