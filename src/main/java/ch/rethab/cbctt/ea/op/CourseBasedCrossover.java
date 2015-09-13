package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.phenotype.*;
import ch.rethab.cbctt.moea.SolutionConverter;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Inspired by 'New Crossover Algorithms for Timetabling with
 * Evolutionary Algorithms' (Lewis and Paechter), this crossover
 * operator takes one course from the second parent and tries to
 * assignRooms it at the same periods in a copy of the first parent.
 * That copy is the first child. The course is selected at random.
 * The second child is constructed by reversing the roles.
 *
 * @author Reto Habluetzel, 2015
 */
public class CourseBasedCrossover implements Variation {

    // if a crossover fails, it is restarted this many times
    private static final int ATTEMPTS_AFTER_FAIL = 100;

    private final SecureRandom rand = new SecureRandom();

    private final SolutionConverter solutionConverter;

    private final RoomAssigner roomAssigner;

    private final Specification spec;

    public CourseBasedCrossover(SolutionConverter solutionConverter, RoomAssigner roomAssigner, Specification spec) {
        this.solutionConverter = solutionConverter;
        this.roomAssigner = roomAssigner;
        this.spec = spec;
    }

    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public Solution[] evolve(Solution[] solutions) {
        TimetableWithRooms parent1 = solutionConverter.fromSolution(solutions[0]);
        TimetableWithRooms parent2 = solutionConverter.fromSolution(solutions[1]);

        TimetableWithRooms[] kids = crossover(parent1, parent2);

        return new Solution[]{solutionConverter.toSolution(kids[0]), solutionConverter.toSolution(kids[1])};
    }

    private TimetableWithRooms[] crossover(TimetableWithRooms parent1, TimetableWithRooms parent2) {
        TimetableWithRooms child2 = null;
        TimetableWithRooms child1 = null;

        for (int i = 0; i < ATTEMPTS_AFTER_FAIL; i++) {
            try {
                Timetable tmpChild1 = parent1.newChild();
                // assignRooms course from p2 in p1's offspring
                Course p2Course = getRandomCourse(parent2);
                Set<MeetingWithRoom> p2Meetings = parent2.getMeetingsByCourse(p2Course);
                scheduleMeetings(p2Course, p2Meetings, tmpChild1);

                child1 = roomAssigner.assignRooms(tmpChild1);
                break;
            } catch (CrossoverFailedException cfe) {
                System.err.println("Crossover failed ("+i+"). Restarting..");
            }
        }

        // permanently failed
        if (child1 == null) {
            // copy is not required, since timetable is unmodifiable
            child1 = parent1;
        }

        for (int i = 0; i < ATTEMPTS_AFTER_FAIL; i++) {
            try {
                Timetable tmpChild2 = parent2.newChild();
                // assignRooms course from p1 in p2's offspring
                Course p1Course = getRandomCourse(parent1);
                Set<MeetingWithRoom> p1Meetings = parent1.getMeetingsByCourse(p1Course);
                scheduleMeetings(p1Course, p1Meetings, tmpChild2);

                child2 = roomAssigner.assignRooms(tmpChild2);
                break;
            } catch (CrossoverFailedException cfe) {
                System.err.println("Crossover failed ("+i+"). Restarting..");
            }
        }

        // permanently failed
        if (child2 == null) {
            // copy is not required, since timetable is unmodifiable
            child2 = parent2;
        }

        return new TimetableWithRooms[]{child1, child2};
    }

    private void scheduleMeetings(Course course, Set<MeetingWithRoom> meetings, Timetable t) throws CrossoverFailedException {
        /* Procedure:
         * 1. For each meeting m1 in meetings:
         *   a) try to set m at m1.day/m1.period.
         *   b) if already occupied by m2 but place would be feasible:
         *     I)  assignRooms m1 in that slot
         *     II) add m2 to the list of to_be_scheduled
         *   c) if feasible and not occupied:
         *     I) assignRooms m1 in that slot
         *   d) otherwise:
         *     I) add m1 to to_be_scheduled
         * 2. Greedy insert all from to_be_scheduled
         */

        Set<Period> preferredPeriods = unscheduleMeetingsByCourse(t, course);
        List<Course> leftovers = scheduleAtSpecifiedPeriods(t, meetings);
        scheduleGreedy(t, preferredPeriods, leftovers);
    }

    private Set<Period> unscheduleMeetingsByCourse(Timetable t, Course course) {
        Set<Meeting> meetings = t.getMeetingsByCourse(course);
        meetings.stream().forEach(t::removeMeeting);
        return meetings.stream()
                .map(m -> new Period(m.getDay(), m.getPeriod()))
                .collect(Collectors.toSet());
    }

    private List<Course> scheduleAtSpecifiedPeriods(Timetable t, Set<MeetingWithRoom> meetings) {
        List<Course> leftovers = new LinkedList<>();

        meetings.forEach(m -> {
            // check if this course has already scheduled a lecture here
            Meeting existing = t.getMeeting(m.getCourse(), m.getDay(), m.getPeriod());

            // already scheduled here. this is what we want
            if (existing != null) {
                throw new IllegalStateException("Meetings should haven been unscheduled before");
            }

            // could this be scheduled here while maintaining feasibility
            if (isFeasible(t, m.getCourse(), m.getDay(), m.getPeriod())) {
                // there still is a free room.
                boolean scheduled = t.addMeeting(m.withoutRoom());
                if (!scheduled) {
                    // replace one meeting at this period with the new one
                    Meeting old = t.replaceMeeting(m.getDay(), m.getPeriod(), m.withoutRoom());

                    if (old == null) {
                        // could not be replaced
                        leftovers.add(m.getCourse());
                    } else {
                        // m is now where old was
                        leftovers.add(old.getCourse());
                    }
                }
            } else {
                leftovers.add(m.getCourse());
            }
        });

        return leftovers;
    }

    /** Places the specified courses wherever they fit */
    private void scheduleGreedy(Timetable t, Set<Period> preferredPeriods, List<Course> toBeScheduled) throws CrossoverFailedException {

        int cIdx = 0; // the index of the course we want to schedule

        LinkedList<Course> courses = new LinkedList<>(toBeScheduled);

        // try to schedule in preferred periods (where other lessons were)
        for (Period p : preferredPeriods) {

            while (!courses.isEmpty()) {
                // take first
                Course c = courses.pop();

                // try to schedule
                if (isFeasible(t, c, p.day, p.period) && t.addMeeting(new Meeting(c, p.day, p.period))) {
                    break;
                }

                // failed, add last
                else {
                    courses.addLast(c);
                }
            }
        }

        // note that the above code truncates the list, while this one only iterates over it
        nextCourse: for (Course c : courses) {
            int attempts = ATTEMPTS_AFTER_FAIL;
            while (attempts-- >= 0) {

                int day = rand.nextInt(spec.getNumberOfDaysPerWeek());
                int period = rand.nextInt(spec.getPeriodsPerDay());

                if (isFeasible(t, c, day, period) && t.addMeeting(new Meeting(c, day, period))) {
                    continue nextCourse;
                }
            }

            String msg = String.format("Failed to schedule meeting after %d attempts\n", ATTEMPTS_AFTER_FAIL);
            throw new CrossoverFailedException(msg);
        }
    }

    private Course getRandomCourse(TimetableWithRooms t) {
        MeetingWithRoom[] meetings = t.getMeetings().toArray(new MeetingWithRoom[t.getMeetings().size()]);
        int idx = rand.nextInt(meetings.length);
        return meetings[idx].getCourse();
    }
    /**
     * Returns true if the course is feasible to be scheduled here.
     * There may be meetings scheduled already.
     */
    private boolean isFeasible(Timetable t, Course course, int day, int period) {
        if (!spec.getUnavailabilityConstraints().checkAvailability(course, day, period)) {
            return false;
        } else if (t.hasLectureOfSameCurriculum(course.getCurricula(), day, period)) {
            return false;
        } else if (t.hasLectureWithSameTeacher(course.getTeacher(), day, period)) {
            return false;
        } else {
            return true;
        }
    }

    private static class CrossoverFailedException extends Exception {
        public CrossoverFailedException(String message) {
            super(message);
        }
    }

    private static class Period {
        final int day;
        final int period;
        public Period(int day, int period) {
            this.day = day;
            this.period = period;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Period period1 = (Period) o;
            return Objects.equals(day, period1.day) &&
                    Objects.equals(period, period1.period);
        }

        @Override
        public int hashCode() {
            return Objects.hash(day, period);
        }
    }

}
