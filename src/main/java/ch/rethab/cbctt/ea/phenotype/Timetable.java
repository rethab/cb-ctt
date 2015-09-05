package ch.rethab.cbctt.ea.phenotype;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Specification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Reto Habluetzel, 2015
 */
public class Timetable implements Serializable {

    /** the curriculum timetables */
    private final Map<String, CurriculumTimetable> curriculumTimetables = new HashMap<>();

    private final PeriodRoomAssignments[] periodRoomAssignmentses;

    private final Specification spec;

    public Timetable(Specification spec) {
        this.spec = spec;

        spec.getCurricula().forEach(c -> curriculumTimetables.put(c.getId(), new CurriculumTimetable(spec)));
        periodRoomAssignmentses = new PeriodRoomAssignments[spec.getNumberOfDaysPerWeek() * spec.getPeriodsPerDay()];
        for (int period = 0; period < periodRoomAssignmentses.length; period++) {
            periodRoomAssignmentses[period] = new PeriodRoomAssignments(spec);
        }
    }

    public boolean addMeeting(Meeting m) {
        PeriodRoomAssignments assignments = periodRoomAssignmentses[toSlotIdx(m.getDay(), m.getPeriod())];
        boolean success = assignments.add(m.getCourse());
        if (success) {
            m.getCourse().getCurricula().stream().forEach(currID ->
                curriculumTimetables.get(currID).setMeeting(m)
            );
            return true;
        } else {
            return false;
        }
    }

    public Set<Meeting> getMeetingsByCourse(Course c) {
        // get first, because meetings by course exist in all curricula timetables
        String currID = c.getCurricula().get(0);
        return curriculumTimetables.get(currID).getMeetingsByCourse(c);
    }

    public Set<Meeting> getMeetings() {
        return curriculumTimetables.values().stream().flatMap(CurriculumTimetable::getAll).collect(Collectors.toSet());
    }

    public PeriodRoomAssignments[] getPeriodRoomAssignmentses() {
        return periodRoomAssignmentses;
    }

    public Map<String, CurriculumTimetable> getCurriculumTimetables() {
        return curriculumTimetables;
    }

    public Meeting getMeeting(Course course, int day, int period) {
        return course.getCurricula().stream()
                .map(currID -> curriculumTimetables.get(currID).get(day, period))
                .filter(m -> m != null && m.getCourse().getId().equals(course.getId()))
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
        Meeting toBeRemoved = getMeetings().stream()
                .filter(m1 -> m1.getDay() == day && m1.getPeriod() == period)
                .findFirst().orElse(null);

        Meeting toBeScheduled;
        if (toBeRemoved != null) {
            removeMeeting(toBeRemoved);

            // todo why would we take the room of the removed? toBeScheduled = m.copy(toBeRemoved.getRoom());
            toBeScheduled = m;

        } else {
            toBeScheduled = m;
        }

        PeriodRoomAssignments assignments = periodRoomAssignmentses[toSlotIdx(day, period)];
        if (!assignments.add(m.getCourse())) {
            throw new IllegalStateException("not sure if this should happen..");
        }

        m.getCourse().getCurricula().stream().forEach(currID ->
            curriculumTimetables.get(currID).setMeeting(toBeScheduled)
        );

        return toBeRemoved;
    }

    public void removeMeeting(Meeting m) {
        m.getCourse().getCurricula().stream().forEach(currID ->
            curriculumTimetables.get(currID).unsetMeeting(m.getDay(), m.getPeriod())
        );
        periodRoomAssignmentses[toSlotIdx(m.getDay(), m.getPeriod())].remove(m.getCourse());
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

    private int toSlotIdx(int day, int period) {
        return day * spec.getPeriodsPerDay() + period;
    }

    public static final class InfeasibilityException extends RuntimeException {
        public InfeasibilityException(String message) {
            super(message);
        }
    }

}
