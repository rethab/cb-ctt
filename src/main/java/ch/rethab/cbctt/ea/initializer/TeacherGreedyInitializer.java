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
 * Technical Notes.
 * Periods: Rather than having a 2d array with days and periods within a day, the
 *          whole timetable is flattened to make it easier accessible with following
 *          formula: index(day, period) = day * periods_per_day + period
 *
 * Generating Distinct Timetables: The above described algorithm is deterministic. However, we may
 *                                 want to construct several distinct timetables based on the same
 *                                 specification. To achieve this, the lectures are not scheduled
 *                                 to the periods in increasing order starting from 0 (try to
 *                                 schedule lecture on first period of first day, then second period
 *                                 on first day, etc), but with a linear congruential generator
 *                                 (Lehmer, 1951). Every time a new timetable is to be constructed,
 *                                 a new seed is used.
 *
 * @author Reto Habluetzel, 2015
 */
public class TeacherGreedyInitializer implements Initializer {

    private Map<String, Integer> hardness;

    @Override
    public List<Timetable> initialize(Specification spec, int size) {
        System.out.println("PopulationSize: " + size);
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
        public Lecture peekFeasible(Specification spec, FlatTimetable timetable) {
            for (Lecture lec : lectures) {
                if (timetable.feasiblePeriod(lec, spec)) {
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
        FlatTimetable timetable = new FlatTimetable(spec.getNumberOfDaysPerWeek(), spec.getPeriodsPerDay());
        Lecture l;
        while (it.hasMore()) {
            l = it.peek();
            if (timetable.feasiblePeriod(l, spec)) {

                // if period is feasible, it can be schedule directly
                timetable.add(l);
                it.remove(l);

                // all rooms are assigned, advance period
                if (timetable.lecturesInPeriod() == spec.getRooms().size()) {
                    timetable.advancePeriod();
                    continue; // schedule next lecture
                } else {

                    // still room in period
                    l = it.peekFeasible(spec, timetable);
                    while (l != null) {
                        timetable.add(l);
                        it.remove(l);

                        l = it.peekFeasible(spec, timetable);
                    }

                    // nothing can be scheduled in this period
                    timetable.advancePeriod();
                }

            // advance period, if none found, maybe start again (submethod) with tabu list to avoid same fuckup?)
            } else if (tryToAssignInOtherPeriod(spec, timetable, it)) {
                // successfully scheduled
                continue;
            } else {

                // could not find a suitable period for lecture. try again with increased hardness
                int increase = Math.max(2, l.hardness / 10);
                int newHardness = l.hardness+increase;

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

    private Timetable toTimetable(Specification spec, FlatTimetable timetable) {
        Timetable t = new Timetable(spec.getCurricula(), spec.getNumberOfDaysPerWeek(), spec.getPeriodsPerDay(), spec.getRooms());
        for (int day = 0; day < spec.getNumberOfDaysPerWeek(); day++) {
            for (int period = 0; period < spec.getPeriodsPerDay(); period++) {
                Iterator<Room> rooms = spec.getRooms().iterator();
                for (Lecture l : timetable.getLectures(day, period)) {
                    t.addMeeting(new Meeting(l.c, rooms.next(), day, period));
                }
            }
        }
        return t;
    }

    private boolean tryToAssignInOtherPeriod(Specification spec, FlatTimetable timetable, LectureIterator it) {
        Lecture l = it.peek();
        int x = timetable.getNextX(timetable.getX());
        for (int i = 0; i < timetable.size(); i++) {
            if (timetable.feasiblePeriod(l, spec, x)) {
                timetable.add(l, x);
                it.remove(l);
                return true;
            }
            x = timetable.getNextX(x);
        }
        return false;
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

    static class FlatTimetable {
        private List<Lecture>[] lectures;

        private final int periodsPerDay;

        // this is also the seed we are reusing
        private static int x = 0;

        public FlatTimetable(int daysPerWeek, int periodsPerDay) {
            int nslots = daysPerWeek * periodsPerDay;

            this.periodsPerDay = periodsPerDay;

            this.lectures = initLectures(nslots);

            // new x every time
            FlatTimetable.x++;
            FlatTimetable.x %= nslots;
        }

        private List<Lecture>[] initLectures(int nslots) {
            List<Lecture>[] lectures = new List[nslots];
            for (int i = 0; i < lectures.length; i++) {
                lectures[i] = new LinkedList<>();
            }
            return lectures;
        }

        public void add(Lecture l) {
            add(l, x);
        }

        public void add(Lecture l, int x) {
            lectures[x].add(l);
        }

        public int lecturesInPeriod(int x) {
            return lectures[x].size();
        }

        public int lecturesInPeriod() {
            return lecturesInPeriod(x);
        }

        public int size() {
            return lectures.length;
        }

        public void advancePeriod() {
            x = getNextX(x);
        }

        public int getX() {
            return x;
        }

        public int getNextX(int x) {
            x++;
            x %= lectures.length;
            return x;
        }

        public boolean feasiblePeriod(Lecture l, Specification spec) {
            return feasiblePeriod(l, spec, x);
        }

        public boolean feasiblePeriod(Lecture l, Specification spec, int x) {
            int day = Math.floorDiv(x, periodsPerDay);
            int period = x % periodsPerDay;
            if (lecturesInPeriod(x) == spec.getRooms().size()) {
                return false;
            } else if (!spec.getUnavailabilityConstraints().checkAvailability(l.c, day, period)) {
                return false;
            } else if (hasLectureOfSameCurriculum(spec, l, x)) {
                return false;
            } else if (hasLectureWithSameTeacher(l, x)) {
                return false;
            } else {
                return true;
            }
        }

        private boolean hasLectureOfSameCurriculum(Specification spec, Lecture l, int x) {
            Set<Curriculum> cs = spec.getByCourse(l.c);
            for (Lecture lecture : lectures[x]) {
                Set<Curriculum> curCurricula = spec.getByCourse(lecture.c);
                for (Curriculum curCurriculum : curCurricula) {
                    if (cs.contains(curCurriculum)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean hasLectureWithSameTeacher(Lecture l, int x) {
            for (Lecture lecture : lectures[x]) {
                if (lecture.c.getTeacher().equals(l.c.getTeacher())) {
                    return true;
                }
            }
            return false;
        }

        public List<Lecture> getLectures(int day, int period) {
            return lectures[day * periodsPerDay + period];
        }
    }

}
