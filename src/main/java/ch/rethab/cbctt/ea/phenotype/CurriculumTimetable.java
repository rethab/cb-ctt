package ch.rethab.cbctt.ea.phenotype;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Specification;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CurriculumTimetable implements Serializable {

    // Array of Meeting (array elements are timeslots)
    private final Meeting[] meetings;

    private final Specification spec;

    public CurriculumTimetable(Specification spec) {
        this.spec = spec;
        this.meetings = new Meeting[this.spec.getNumberOfDaysPerWeek() * this.spec.getPeriodsPerDay()];
    }

    public void setMeeting(Meeting m) {

        // all slots are organized linearly in one list
        int slotIdx = toSlotIdx(m.getDay(), m.getPeriod());

        Meeting existing = meetings[slotIdx];
        if (existing != null) {
            String msg = String.format("Cannot add Meeting for Course %s, %s is already here",
                    m.getCourse().getId(), existing.getCourse().getId());
            throw new Timetable.InfeasibilityException(msg);
        }

        meetings[slotIdx] = m;
    }

    private int toSlotIdx(int day, int period) {
        return day * spec.getPeriodsPerDay() + period;
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

    public void unsetMeeting(int day, int period) { // todo checkj
        meetings[toSlotIdx(day, period)] = null;
    }
}


