package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.domain.Course;
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

    public Meeting getMeeting(Course course, int day, int period) {
        return course.getCurricula().stream()
                .map(currID -> curriculumTimetables.get(currID).get(day, period))
                .filter(m -> m != null && m.getCourse().getId().equals(course.getId()))
                .findFirst().orElse(null);
    }

    public String getFreeRoomId(int day, int period) {
        return roomOccupancy.entrySet().stream()
                .map(entry -> !entry.getValue()[toSlotIdx(day, period)] ? entry.getKey() : null)
                .filter(roomID -> roomID != null)
                .findFirst().orElse(null);
    }

    /**
     * Schedules the specified meeting at the specified day and period
     * and returns the existing meeting if there is one.
     */
    public Meeting replaceMeeting(int day, int period, Meeting m) {
        /*
         * Find a meeting to be replaced and remove all instances of it's
         * courses in all curricula. Then set the new one. These multiple
         * steps are required since the two meetings may not belong to the
         * same curricula
         */
        Meeting toBeRemoved = m.getCourse().getCurricula().stream().map(currID ->
            curriculumTimetables.get(currID).get(day, period)
        ).findFirst().orElse(null);

        if (toBeRemoved != null) {
            toBeRemoved.getCourse().getCurricula().stream().forEach(currID -> {
                curriculumTimetables.get(currID).unsetMeeting(day, period);
            });
        }

        m.getCourse().getCurricula().stream().forEach(currID -> {
            curriculumTimetables.get(currID).setMeeting(m);
        });

        return toBeRemoved;
    }

    public void removeMeeting(Meeting m) {
        m.getCourse().getCurricula().stream().forEach(currID ->
            curriculumTimetables.get(currID).unsetMeeting(m.getDay(), m.getPeriod())
        );
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

    @Override
    public String toString() {
        return new PrettyTextPrinter().print(this);
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

    private int toSlotIdx(int day, int period) {
        return day * periodsPerDay + period;
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
