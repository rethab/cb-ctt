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
        if (!success) {
            return false;
        }

        try {
            m.getCourse().getCurricula().stream().forEach(currID -> curriculumTimetables.get(currID).setMeeting(m) );
            return true;
        } catch (InfeasibilityException ife) {
            assignments.remove(m.getCourse());
            m.getCourse().getCurricula().stream().forEach(currID ->
                // unset meeting again. could be that meeting was already set in some
                // curricula and only then the exception was thrown
                curriculumTimetables.get(currID).unsetMeeting(m.getDay(), m.getPeriod())
            );
            throw ife;
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

    public Set<Meeting> getMeetingsByPeriod(int day, int period) {
        return curriculumTimetables.values().stream()
                .map(ctt -> ctt.get(day, period))
                .filter(ctt -> ctt != null)
                .collect(Collectors.toSet());
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
     * @return the meeting that was removed or null if scheduling the new
     *         meeting was not possible. In the latter case, the old
     *         meeting is added again.
     */
    public Meeting replaceMeeting(int day, int period, Meeting m) {
        /*
         * Find a meeting to be replaced and remove all instances of it's
         * courses in all curricula. Then set the new one. These multiple
         * steps are required since the two meetings may not belong to the
         * same curricula
         */

        // find candidates to be replaced
        Set<Meeting> candidates = getMeetingsByPeriod(day, period);

        for (Meeting candidate : candidates) {

            removeMeeting(candidate);

            // try to add new meeting. if not possible (maybe it is more constrained)
            // add the old one back
            if (!addMeeting(m)) {
                if (!addMeeting(candidate)) {
                    throw new IllegalStateException("Should be able to re-add meeting");
                }
                // keep on searching
            } else {
                // replace
                return candidate;
            }
        }

        return null;
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
