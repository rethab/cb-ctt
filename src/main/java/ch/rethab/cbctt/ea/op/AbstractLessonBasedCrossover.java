package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.phenotype.*;
import ch.rethab.cbctt.moea.SolutionConverter;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Reto Habluetzel, 2015
 */
public abstract class AbstractLessonBasedCrossover implements Variation {

    // if a crossover fails, it is restarted this many times
    private static final int ATTEMPTS_AFTER_FAIL = 100;

    protected final SecureRandom rand = new SecureRandom();

    protected final SolutionConverter solutionConverter;
    protected final Specification spec;
    protected final RoomAssigner roomAssigner;

    public AbstractLessonBasedCrossover(Specification spec, SolutionConverter solutionConverter, RoomAssigner roomAssigner) {
        this.spec = spec;
        this.solutionConverter = solutionConverter;
        this.roomAssigner = roomAssigner;
    }

    protected abstract Set<MeetingWithRoom> getMeetingsFromParent(TimetableWithRooms parent);

    @Override
    public final int getArity() {
        return 2;
    }

    @Override
    public final Solution[] evolve(Solution[] solutions) {
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

                Set<MeetingWithRoom> p2Meetings = getMeetingsFromParent(parent2);
                // assignRooms course from p2 in p1's offspring
                scheduleMeetings(p2Meetings, tmpChild1);

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
                Set<MeetingWithRoom> p1Meetings = getMeetingsFromParent(parent1);
                scheduleMeetings(p1Meetings, tmpChild2);

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

    private void scheduleMeetings(Set<MeetingWithRoom> meetings, Timetable t) throws CrossoverFailedException {
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


        Set<Period> preferredPeriods = unscheduleLessons(t, meetings);
        List<Course> leftovers = scheduleAtSpecifiedPeriods(t, meetings);
        scheduleGreedy(t, preferredPeriods, leftovers);
    }

    private Set<Period> unscheduleLessons(Timetable t, Set<MeetingWithRoom> meetings) {

        // this is where we take meetings from. we want to place the leftovers here
        // afterwards to avoid too much randomness
        Set<Period> preferredPeriods = new LinkedHashSet<>(meetings.size());

        // courses that are already scheduled in the target period, don't have to be touched
        // and are therefor removed from the meetings list
        Set<MeetingWithRoom> toBeRemoved = new LinkedHashSet<>();

        for (MeetingWithRoom meeting : meetings) {

            // if the course is already scheduled at this period,
            // we don't have to unschedule it here just re-add
            // it later
            if (t.getMeeting(meeting.getCourse(), meeting.getDay(), meeting.getPeriod()) != null) {
                toBeRemoved.add(meeting);
                continue;
            }

            // take a random lesson of the course and remove it
            Set<Meeting> meetingsByCourse = t.getMeetingsByCourse(meeting.getCourse());
            meetingsByCourse.removeAll(toBeRemoved.stream().map(MeetingWithRoom::withoutRoom).collect(Collectors.toSet()));
            int nMeetings = meetingsByCourse.size();

            if (nMeetings == 0) {
                String msg = "Not meetings found to unschedule. Data inconsistency: " +
                             "This has previously happened when the two parents had a different set of lessons";
                throw new IllegalStateException(msg);
            }

            // if there is only one left. need no randomness
            int removeIdx = nMeetings == 1 ? 0 : rand.nextInt(nMeetings);

            Meeting m = meetingsByCourse.toArray(new Meeting[nMeetings])[removeIdx];
            t.removeMeeting(m);

            preferredPeriods.add(new Period(m.getDay(), m.getPeriod()));
        }

        meetings.removeAll(toBeRemoved);

        return preferredPeriods;
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

        nextCourse: for (Course c : toBeScheduled) {
            // try to schedule in preferred periods
            for (Period p : preferredPeriods) {
                if (isFeasible(t, c, p.day, p.period) && t.addMeeting(new Meeting(c, p.day, p.period))) {
                    preferredPeriods.remove(p);
                    continue nextCourse;
                }
            }

            // try to schedule at a random period
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
