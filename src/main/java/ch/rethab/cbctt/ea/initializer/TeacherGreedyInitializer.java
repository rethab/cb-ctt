package ch.rethab.cbctt.ea.initializer;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * Implements the classic idea of scheduling the most constrained lectures first.
 * The most constrained is determined with the following ordering. Each lecture
 * has a notion of hardness which is calculated with:
 *
 *  hardness(lecture) = number_of_lectures_taught(lecture.teacher)
 *                   + number_of_unavailabilities_of_teacher(lecture.teacher)
 *
 * Algorithm.
 * 1. Order lectures in descending order of hardness
 * 2. For each Lecture l:
 *   a. Schedule l at first feasible period in any free room (feasible = teacher is available, has free rooms)
 *   b. All rooms of period scheduled? --> Advance period, goto 2.
 *   c. Otherwise --> Take next lecture of next curriculum with different teacher, schedule in same period
 *   d. Nothing to schedule? (e.g. no more curricula, no feasible lecture) --> Advance period, goto 2.
 *   e. Otherwise: goto c (schedule next lecture in same period)
 *   f. Cannot schedule lecture? Increase hardness manually and start all over again
 * 3. Assign rooms per period. Cant produce constraint violations
 *
 * @author Reto Habluetzel, 2015
 */
public class TeacherGreedyInitializer implements Initializer {

    private Map<String, Integer> hardness;

    @Override
    public List<Timetable> initialize(Specification spec, int size) {
        this.hardness = new HashMap<>();
        return IntStream.range(0, size).mapToObj(i -> createTimetable(spec)).collect(Collectors.toList());
    }

    private static class LectureIterator {
        private final List<Lecture> lectures;
        public LectureIterator(List<Lecture> lectures) {
            this.lectures = new LinkedList<>(lectures);
            Collections.sort(this.lectures);
        }
        public boolean hasMore() {
            return !lectures.isEmpty();
        }
        public Lecture peek() {
            return lectures.get(0);
        }
        public void remove(Lecture l) {
            lectures.remove(l);
        }
        // distinct: other teacher, other curriculum, feasible for this period
        public Lecture peekFeasible(Specification spec, List<Lecture>[][] timetable, int day, int period) {
            for (Lecture lec : lectures) {
                if (feasiblePeriod(lec, spec, timetable, day, period)) {
                    return lec;
                }
            }
            // no more distinct, need to advance period
            return null;
        }
    }

    private Timetable createTimetable(Specification spec) {
        List<Lecture> lectures = initLectures(spec);

        for (int i = 0; i < 100; i++) {
            LectureIterator it = new LectureIterator(lectures);
            if (i == 50) {
                System.out.println();
            }
            Timetable t = getTimetable0(spec, it, lectures);
            if (t != null) {
                return t;
            } else {
                this.hardness = new HashMap<>();
            }
        }

        throw new RuntimeException("Failed to find feasible solution");
    }

    private Timetable getTimetable0(Specification spec, LectureIterator it, List<Lecture> originalLectures) {
        System.out.printf("First is %s with hardness %d\t", it.peek().c.getId(), it.peek().hardness);
        List<Lecture>[][] timetable = newTimetable(spec);
        int day = 0;
        int period = 0;
        Lecture l;
        while (it.hasMore()) {
            l = it.peek();
            if (feasiblePeriod(l, spec, timetable, day, period)) {

                // if period is feasible, it can be schedule directly
                timetable[day][period].add(l);
                it.remove(l);

                // all rooms are assigned, advance period
                if (timetable[day][period].size() == spec.getRooms().size()) {
                    if (period == spec.getPeriodsPerDay()-1) {
                        period = 0;
                        day = (day + 1) % spec.getNumberOfDaysPerWeek();
                    } else {
                        period++;
                    }
                    continue; // schedule next lecture
                } else {

                    // still room in period
                    l = it.peekFeasible(spec, timetable, day, period);
                    while (l != null) {
                        timetable[day][period].add(l);
                        it.remove(l);

                        l = it.peekFeasible(spec, timetable, day, period);
                    }

                    // nothing can be scheduled in this period
                    if (period == spec.getPeriodsPerDay()-1) {
                        period = 0;
                        day = (day + 1) % spec.getNumberOfDaysPerWeek();
                    } else {
                        period++;
                    }
                }

            // advance period, if none found, maybe start again (submethod) with tabu list to avoid same fuckup?)
            } else if (tryToAssignInOtherPeriod(spec, timetable, day, period, it)) {
                // successfully scheduled
                continue;
            } else {

                // could not find a suitable period for lecture. try again with increased hardness
                int increase = Math.max(2, l.hardness / 10);
                int newHardness = l.hardness+increase;
                System.out.printf("Set hardness for %s from %d to %d\n", l.c.getId(), l.hardness, newHardness);

                // update all lectures of same course as the other lectures are essentially equal
                Course c = l.c;
                originalLectures.stream().filter(lec -> lec.c.equals(c)).forEach(lec ->
                        lec.hardness = newHardness
                );
                return null;
            }
        }
        return toTimetable(spec, timetable);
    }

    private Timetable toTimetable(Specification spec, List<Lecture>[][] timetable) {
        Timetable t = new Timetable();
        for (int day = 0; day < timetable.length; day++) {
            for (int period = 0; period < timetable[day].length; period++) {
                Iterator<Room> rooms = spec.getRooms().iterator();
                for (Lecture l : timetable[day][period]) {
                    t.addMeeting(new Meeting(l.c, rooms.next(), day, period));
                }
            }
        }
        return t;
    }

    private boolean tryToAssignInOtherPeriod(Specification spec, List<Lecture>[][] timetable, int day, int period, LectureIterator it) {
        Lecture l = it.peek();
        int startPeriod = period+1; // for the current day, we start at the next period. for the next day, we start at 0
        for (int d = day; d < spec.getNumberOfDaysPerWeek(); d++) {
            for (int p = startPeriod; p < spec.getPeriodsPerDay(); p++) {
                if (feasiblePeriod(l, spec, timetable, d, p)) {
                    timetable[d][p].add(l);
                    it.remove(l);
                    return true;
                }
            }
            startPeriod = 0; // start with first period on next day
        }
        return false;
    }

    private static boolean feasiblePeriod(Lecture l, Specification spec, List<Lecture>[][] timetable, int day, int period) {
        if (timetable[day][period].size() == spec.getRooms().size()) {
            return false;
        } else if (!spec.getUnavailabilityConstraints().checkAvailability(l.c, day, period)) {
            return false;
        } else if (hasLectureOfSameCurriculum(spec, timetable[day][period], l)) {
            return false;
        } else if (hasLectureWithSameTeacher(timetable[day][period], l)) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean hasLectureWithSameTeacher(List<Lecture> lectures, Lecture l) {
        for (Lecture lecture : lectures) {
            if (lecture.c.getTeacher().equals(l.c.getTeacher())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasLectureOfSameCurriculum(Specification spec, List<Lecture> lectures, Lecture l) {
        Set<Curriculum> cs = spec.getByCourse(l.c);
        for (Lecture lecture : lectures) {
            Set<Curriculum> curCurricula = spec.getByCourse(lecture.c);
            for (Curriculum curCurriculum : curCurricula) {
                if (cs.contains(curCurriculum)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Lecture>[][] newTimetable(Specification spec) {
        List<Lecture>[][] timetable = new List[spec.getNumberOfDaysPerWeek()][spec.getPeriodsPerDay()];
        for (int day = 0; day < timetable.length; day++) {
            for (int period = 0; period < timetable[day].length; period++) {
                timetable[day][period] = new LinkedList<>();
            }
        }
        return timetable;
    }

    private List<Lecture> initLectures(Specification spec) {
        List<Lecture> lectures = new LinkedList<>();
        for (Course c : spec.getCourses()) {
            int hardness = lookupHardness(c.getTeacher(), spec);
            Set<Curriculum> curricula = spec.getByCourse(c);
            IntStream.range(0, c.getNumberOfLectures()).forEach(i ->
                lectures.add(new Lecture(curricula, c, hardness))
            );
        }
        return Collections.unmodifiableList(lectures);
    }

    // convenience class for notion of hardness
    private static class Lecture implements Comparable {
        public final Course c;

        public final Set<Curriculum> curricula;

        private int hardness;

        public Lecture(Set<Curriculum> curricula, Course c, int hardness) {
            this.curricula = curricula;
            this.c = c;
            this.hardness = hardness;
        }

        @Override
        public int compareTo(Object o) {
            Lecture other = (Lecture)o;
            // by exchanging the order, we sort descending
            return other.hardness - this.hardness;
        }
    }

    private int lookupHardness(String teacher, Specification spec) {
        Integer hardness = this.hardness.get(teacher);
        if (hardness == null) {
            hardness = getHardness(teacher, spec);
            this.hardness.put(teacher, hardness);
        }
        return hardness;
    }

    private int getHardness(String teacher, Specification spec) {
        final int[] hardness = {0};
        spec.getCourses().stream().filter(c -> teacher.equals(c.getTeacher())).forEach(c ->
            hardness[0] += c.getNumberOfLectures()
        );
        hardness[0] += spec.getUnavailabilityConstraints().countByTeacher(teacher);
        return hardness[0];
    }

}
