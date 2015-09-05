package ch.rethab.cbctt.ea.phenotype;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Specification;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CurriculumTimetableWithRooms implements Serializable {

    private final Specification spec;

    private final MeetingWithRoom[] meetings;

    public CurriculumTimetableWithRooms(Specification spec, MeetingWithRoom[] meetings) {
        this.spec = spec;
        this.meetings = new MeetingWithRoom[meetings.length];
        System.arraycopy(meetings, 0, this.meetings, 0, meetings.length);
    }

    public Stream<MeetingWithRoom> getAll() {
        return Arrays.stream(meetings).filter(m -> m != null);
    }

    public MeetingWithRoom get(int day, int period) {
        return meetings[toSlotIdx(day, period)];
    }

    private int toSlotIdx(int day, int period) {
        return day * spec.getPeriodsPerDay() + period;
    }

    public Set<MeetingWithRoom> getMeetingsByCourse(Course c) {
        return Arrays.stream(meetings)
                .filter(m -> m != null && m.getCourse().getId().equals(c.getId()))
                .collect(Collectors.toSet());
    }

    public Stream<MeetingWithRoom> getMeetingsByTeacher(String teacher) {
        return Arrays.stream(meetings)
                .filter(m -> m != null && m.getCourse().getTeacher().equals(teacher));
    }
}


