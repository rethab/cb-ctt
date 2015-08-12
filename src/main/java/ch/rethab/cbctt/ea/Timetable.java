package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.ea.printer.PrettyTextPrinter;

import java.util.*;
import java.util.stream.Collectors;
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

    public Timetable(Set<String> curricula, Set<String> rooms, int days, int periodsPerDay) {
        this.days = days;
        this.periodsPerDay = periodsPerDay;
        curricula.forEach(cid -> curriculumTimetables.put(cid, new CurriculumTimetable(days, periodsPerDay)));
        rooms.forEach(rid -> roomOccupancy.put(rid, new boolean[days * periodsPerDay]));
    }

    public void addMeeting(Meeting meeting) {
        String currID = meeting.getCourse().getCurriculum();
        curriculumTimetables.get(currID).setMeeting(meeting);
    }

    public Set<Meeting> getMeetingsByCourse(Course c) {
        String currID = c.getCurriculum();
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

    public Map<String, CurriculumTimetable> getCurriculumTimetables() {
        return Collections.unmodifiableMap(curriculumTimetables);
    }

    @Override
    public String toString() {
        return new PrettyTextPrinter().print(this);
    }

    public int getPeriodsPerDay() {
        return periodsPerDay;
    }

    public int getDays() {
        return days;
    }

    public final class CurriculumTimetable {

        // Array of Meeting (array elements are timeslots)
        private final Meeting[] meetings;

        private final int periodsPerDay;

        public CurriculumTimetable(int days, int periodsPerDay) {
            this.periodsPerDay = periodsPerDay;
            this.meetings = new Meeting[days * periodsPerDay];
        }

        public void setMeeting(Meeting meeting) {

            // all slots are organized linearly in one list
            int slotIdx = toSlotIdx(meeting.getDay(), meeting.getPeriod());

            boolean[] occupancy = roomOccupancy.get(meeting.getRoom().getId());
            if (occupancy[slotIdx]) {
                throw new InfeasibilityException("Room is occupied at Day="+meeting.getDay()+" and Period="+meeting.getPeriod());
            }

            Meeting existing = meetings[slotIdx];
            if (existing != null) {
                String msg = String.format("Cannot add Meeting for Course %s, %s is already here",
                                           meeting.getCourse().getId(), existing.getCourse().getId());
                throw new InfeasibilityException(msg);
            }

            meetings[slotIdx] = meeting;
            occupancy[slotIdx] = true;
        }
        
        public Meeting get(int slotIdx) {
            return meetings[slotIdx];
        }

        public Meeting get(int day, int period) {
            return meetings[toSlotIdx(day, period)];
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

        private int toSlotIdx(int day, int period) {
            return day * periodsPerDay + period;
        }
    }

    public static final class InfeasibilityException extends RuntimeException {
        public InfeasibilityException(String message) {
            super(message);
        }
    }

}
