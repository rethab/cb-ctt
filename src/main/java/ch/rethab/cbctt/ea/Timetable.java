package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.domain.Course;
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
        curricula.forEach(cid -> curriculumTimetables.put(cid, new CurriculumTimetable(days)));
        rooms.forEach(rid -> roomOccupancy.put(rid, new boolean[days * periodsPerDay]));
    }

    public void addMeeting(Meeting meeting) {
        checkRoomAvailability(meeting);
        meeting.getCourse().getCurricula().stream().forEach(currID ->
                        curriculumTimetables.get(currID).setMeeting(meeting)
        );
        setRoomToOccupied(meeting);
    }

    private void setRoomToOccupied(Meeting meeting) {
        boolean[] occupancy = roomOccupancy.get(meeting.getRoom().getId());
        occupancy[toSlotIdx(meeting.getDay(), meeting.getPeriod())] = true;
    }

    private void checkRoomAvailability(Meeting meeting) {
        boolean[] occupancy = roomOccupancy.get(meeting.getRoom().getId());
        if (occupancy[toSlotIdx(meeting.getDay(), meeting.getPeriod())]) {
            throw new InfeasibilityException("Room is occupied at Day="+meeting.getDay()+" and Period="+meeting.getPeriod());
        }
    }

    public Set<Meeting> getMeetingsByCourse(Course c) {
        // get first, because meetings by course exist in all curricula timetables
        String currID = c.getCurricula().get(0);
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
        getMeetings().forEach(m -> copy.addMeeting(m.copy(m.getRoom())));
        return copy;
    }

    public Map<String, CurriculumTimetable> getCurriculumTimetables() {
        return Collections.unmodifiableMap(curriculumTimetables);
    }

    public int getPeriodsPerDay() {
        return periodsPerDay;
    }

    public int getDays() {
        return days;
    }

    private int toSlotIdx(int day, int period) {
        return day * periodsPerDay + period;
    }

    @Override
    public String toString() {
        return new PrettyTextPrinter().print(this);
    }

    public Meeting getMeeting(Course course, int day, int period) {
        return curriculumTimetables.get(course.getId()).get(day, period);
    }

    public Set<Meeting> getMeetings(int day, int period) {
        return curriculumTimetables
                .values().stream()
                .map(ctt -> ctt.get(day, period))
                .filter(m -> m != null)
                .collect(Collectors.toSet());
    }

    public String getFreeRoomId(int day, int period) {
        return roomOccupancy.entrySet().stream()
                .map(entry -> !entry.getValue()[toSlotIdx(day, period)] ? entry.getKey() : null)
                .filter(roomID -> roomID != null)
                .findFirst().orElse(null);
    }

    public void setMeeting(Meeting m) {
        setRoomToOccupied(m);
        m.getCourse().getCurricula().stream().forEach(currID -> curriculumTimetables.get(currID).setMeeting(m));
    }

    public Meeting replaceMeeting(int day, int period, Meeting m) {
        return m.getCourse().getCurricula().stream().map(currID -> {
            CurriculumTimetable ctt = curriculumTimetables.get(currID);
            Meeting m2 = ctt.get(day, period);
            ctt.unsetMeeting(day, period);
            return m2;
        }).findFirst().orElse(null);
    }

    public boolean hasLectureOfSameCurriculum(List<String> curricula, int day, int period) {
        return curricula.stream()
                .map(currID -> curriculumTimetables.get(currID).get(day, period))
                .anyMatch(m -> m != null);
    }

    public boolean hasLectureWithSameTeacher(String teacher, int day, int period) {
        return curriculumTimetables.values().stream().anyMatch(ctt -> {
            Meeting m = ctt.get(day, period);
            return m != null && m.getCourse().getTeacher().equals(teacher);
        });
    }

    public final class CurriculumTimetable {

        // Array of Meeting (array elements are timeslots)
        private final Meeting[] meetings;

        public CurriculumTimetable(int days) {
            this.meetings = new Meeting[days * periodsPerDay];
        }

        public void setMeeting(Meeting meeting) {

            // all slots are organized linearly in one list
            int slotIdx = toSlotIdx(meeting.getDay(), meeting.getPeriod());

            Meeting existing = meetings[slotIdx];
            if (existing != null) {
                String msg = String.format("Cannot add Meeting for Course %s, %s is already here",
                                           meeting.getCourse().getId(), existing.getCourse().getId());
                throw new InfeasibilityException(msg);
            }

            meetings[slotIdx] = meeting;
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

        public void unsetMeeting(int day, int period) {
            meetings[toSlotIdx(day, period)] = null;
        }
    }

    public static final class InfeasibilityException extends RuntimeException {
        public InfeasibilityException(String message) {
            super(message);
        }
    }

}
