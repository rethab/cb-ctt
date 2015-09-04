package ch.rethab.cbctt.ea.phenotype;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.domain.Specification;

import java.util.*;
import java.util.stream.Collectors;

public class TimetableWithRooms {

    private final Specification spec;

    private final Map<String, CurriculumTimetableWithRooms> curriculumTimetables;

    private TimetableWithRooms(Specification spec, Map<String, CurriculumTimetableWithRooms> curriculumTimetables) {
        this.spec = spec;
        this.curriculumTimetables = curriculumTimetables;
    }

    public Map<String, CurriculumTimetableWithRooms> getCurriculumTimetables() {
        return Collections.unmodifiableMap(curriculumTimetables);
    }

    public Set<MeetingWithRoom> getMeetingsByCourse(Course c) {
        // get first, because meetings by course exist in all curricula timetables
        String currID = c.getCurricula().get(0);
        return curriculumTimetables.get(currID).getMeetingsByCourse(c);
    }

    public Set<MeetingWithRoom> getMeetingsByTeacher(String teacher) {
        return curriculumTimetables.values().stream().flatMap(ct -> ct.getMeetingsByTeacher(teacher)).collect(Collectors.toSet());
    }

    public Set<MeetingWithRoom> getMeetings() {
        return curriculumTimetables.values().stream()
                .flatMap(CurriculumTimetableWithRooms::getAll)
                .collect(Collectors.toSet());
    }

    public Timetable newChild() {
        Timetable t = new Timetable(spec);
        getMeetings().stream().forEach(m -> t.addMeeting(m.withoutRoom()));
        return t;
    }

    public static class Builder {
        private Map<String, Set<MeetingWithRoom>> meetings = new HashMap<>();

        private final Specification spec;

        private Builder(Specification spec) {
            this.spec = spec;
        }

        public Builder addMeeting(Course course, Room room, int day, int period) {
            course.getCurricula().forEach(currId -> {
                meetings.get(currId).add(new MeetingWithRoom(course, room, day, period));
            });
            return this;
        }

        public TimetableWithRooms build() {
            Map<String, CurriculumTimetableWithRooms> ctt = new HashMap<>(meetings.size());
            meetings.entrySet().forEach(entry -> {
                Set<MeetingWithRoom> value = entry.getValue();
                MeetingWithRoom[] meetings = value.toArray(new MeetingWithRoom[value.size()]);
                ctt.put(entry.getKey(), new CurriculumTimetableWithRooms(spec, meetings));
            });
            return new TimetableWithRooms(spec, ctt);
        }

        public static Builder newBuilder(Specification spec) {
            Builder b = new Builder(spec);
            spec.getCurricula().stream().forEach(curr -> {
                b.meetings.put(curr.getId(), new LinkedHashSet<>());
            });
            return b;
        }
    }

}
