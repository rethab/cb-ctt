package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.Room;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Reto Habluetzel, 2015
 */
public class Timetable {

    /** the curriculum timetables */
    private final Map<String, CurriculumTimetable> curriculumTimetables = new HashMap<>();

    /** RoomID -> OccupiedTimeslots for room */
    private final Map<String, boolean[]> roomOccupancy = new HashMap<>();

    private final int days;

    private final int periodsPerDay;

    public Timetable(List<Curriculum> curricula, int days, int periodsPerDay, List<Room> rooms) {
        this(curricula.stream().map(Curriculum::getId).collect(Collectors.toSet()),
             rooms.stream().map(Room::getId).collect(Collectors.toSet()),
             days, periodsPerDay);
    }

    private Timetable(Set<String> curricula, Set<String> rooms, int days, int periodsPerDay) {
        this.days = days;
        this.periodsPerDay = periodsPerDay;
        curricula.forEach(cid -> curriculumTimetables.put(cid, new CurriculumTimetable(days, periodsPerDay)));
        rooms.forEach(rid -> roomOccupancy.put(rid, new boolean[days * periodsPerDay]));
    }

    public void addMeeting(Meeting meeting) {
        String currID = meeting.getCourse().getCurriculum().getId();
        curriculumTimetables.get(currID).setMeeting(meeting);
    }

    public Set<Meeting> getMeetingsByCourse(Course c) {
        String currID = c.getCurriculum().getId();
        return curriculumTimetables.get(currID).getMeetingsByCourse(c);
    }

    public Set<Meeting> getMeetingsByTeacher(String teacher) {
        return curriculumTimetables.values().stream().flatMap(ct -> ct.getMeetingsByTeacher(teacher)).collect(Collectors.toSet());
    }

    public Set<Meeting> getMeetings() {
        return curriculumTimetables.values().stream().flatMap(ct -> ct.getAll()).collect(Collectors.toSet());
    }

    public Timetable copy() {
        Timetable copy = new Timetable(curriculumTimetables.keySet(), roomOccupancy.keySet(), days, periodsPerDay);
        getMeetings().forEach(copy::addMeeting);
        return copy;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        curriculumTimetables.forEach((cid, timetable) -> {
            sb.append("Curriculum: ").append(cid).append("\n");
            sb.append(timetable.toString());
            sb.append("\n");
        });

        return sb.toString();
    }

    private final class CurriculumTimetable {

        // Array of Meeting (array elements are timeslots)
        private final Meeting[] meetings;

        private final int periodsPerDay;

        public CurriculumTimetable(int days, int periodsPerDay) {
            this.periodsPerDay = periodsPerDay;
            this.meetings = new Meeting[days * periodsPerDay];
        }

        public void setMeeting(Meeting meeting) {

            // all slots are organized linearly in one list
            int slotIdx = meeting.getDay() * periodsPerDay + meeting.getPeriod();

            boolean[] occupancy = roomOccupancy.get(meeting.getRoom().getId());
            if (occupancy[slotIdx]) {
                throw new InfeasibilityException("Room is occupied at Day="+meeting.getDay()+" and Period="+meeting.getPeriod());
            }

            if (meetings[slotIdx] != null) {
                throw new IllegalStateException("Meeting should not be occupied due to room constraint list");
            }

            meetings[slotIdx] = meeting;
            occupancy[slotIdx] = true;
        }

        public Set<Meeting> getMeetingsByCourse(Course c) {
            return Arrays.stream(meetings).filter(m -> m != null && m.getCourse().getId().equals(c.getId())).collect(Collectors.toSet());
        }

        public Stream<Meeting> getMeetingsByTeacher(String teacher) {
            return Arrays.stream(meetings).filter(m -> m != null && m.getCourse().getTeacher().equals(teacher));
        }

        public Stream<Meeting> getAll() {
            return Arrays.stream(meetings).filter(m -> m != null);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            int cellLen = 10;
            int days = meetings.length / periodsPerDay;
            int lineLen = cellLen * (days + 1);

            // headline
            sb.append(padLeft("", cellLen));
            IntStream.range(0, days).forEach(day ->
                    sb.append(padLeft(String.format("Day %2d", day), cellLen))
            );
            sb.append("\n");

            for (int period = 0; period < periodsPerDay; period++) {

                sb.append(repeat("-", lineLen)).append("\n");

                // course
                sb.append(padLeft(String.format("Slot %2d | ", period), cellLen));
                for (int day = 0; day < days; day++) {
                    int slotIdx = day * periodsPerDay + period;
                    Meeting m = meetings[slotIdx];
                    sb.append(padLeft(m != null ? m.getCourse().getId() : "", cellLen));
                }
                sb.append("\n");

                // room
                sb.append(padLeft("| ", cellLen));
                for (int day = 0; day < days; day++) {
                    int slotIdx = day * periodsPerDay + period;
                    Meeting m = meetings[slotIdx];
                    sb.append(padLeft(m != null ? m.getRoom().getId() : "", cellLen));
                }
                sb.append("\n");

                // teacher
                sb.append(padLeft("| ", cellLen));
                for (int day = 0; day < days; day++) {
                    int slotIdx = day * periodsPerDay + period;
                    Meeting m = meetings[slotIdx];
                    sb.append(padLeft(m != null ? m.getCourse().getTeacher() : "", cellLen));
                }

                sb.append("\n");
            }

            return sb.toString();
        }

        // source: http://stackoverflow.com/questions/388461/how-can-i-pad-a-string-in-java/391978#391978
        private String padLeft(String s, int n) {
            return String.format("%1$" + n + "s", s);
        }

        // source: http://stackoverflow.com/a/4903603
        private String repeat(String s, int n) {
            return new String(new char[n]).replace("\0", s);
        }
    }

    private static final class InfeasibilityException extends RuntimeException {
        public InfeasibilityException(String message) {
            super(message);
        }
    }

}
