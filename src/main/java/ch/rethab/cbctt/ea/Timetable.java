package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Room;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Reto Habluetzel, 2015
 */
public class Timetable {

    private final List<Meeting> meetings = new LinkedList<>();

    public void addMeeting(Meeting meeting) {
        meetings.add(meeting);
    }

    public List<Meeting> getMeetingsByCourse(Course c) {
        return meetings.stream().filter(m -> m.getCourse().equals(c)).collect(Collectors.toList());
    }

    public List<Meeting> getMeetingsByTeacher(String teacher) {
        return meetings.stream().filter(m -> m.getCourse().getTeacher().equals(teacher)).collect(Collectors.toList());
    }

    public List<Meeting> getMeetings() {
        return meetings;
    }

    public boolean isFree(Room r, int day, int period) {
        for (Meeting m : meetings) {
            if (m.getRoom().equals(r) && m.getDay() == day && m.getPeriod() == period) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Timetable{");
        sb.append("meetings=").append(meetings);
        sb.append('}');
        return sb.toString();
    }

}
