package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.moea.SolutionConverter;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import java.security.SecureRandom;
import java.util.*;

/**
 * Inspired by 'New Crossover Algorithms for Timetabling with
 * Evolutionary Algorithms' (Lewis and Paechter), this crossover
 * operator takes one course from the second parent and tries to
 * schedule it at the same periods in a copy of the first parent.
 * That copy is the first child. The course is selected at random.
 * The second child is constructed by reversing the roles.
 *
 * @author Reto Habluetzel, 2015
 */
public class CourseBasedCrossover implements Variation {

    private final SolutionConverter solutionConverter;

    private final Specification spec;

    public CourseBasedCrossover(SolutionConverter solutionConverter, Specification spec) {
        this.solutionConverter = solutionConverter;
        this.spec = spec;
    }

    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public Solution[] evolve(Solution[] solutions) {
        Timetable parent1 = solutionConverter.fromSolution(solutions[0]);
        Timetable parent2 = solutionConverter.fromSolution(solutions[1]);

        Timetable[] kids = crossover(parent1, parent2);
        return new Solution[]{solutionConverter.toSolution(kids[0]), solutionConverter.toSolution(kids[1])};
    }

    private Timetable[] crossover(Timetable parent1, Timetable parent2) {
        Timetable child1 = parent1.copy();
        Timetable child2 = parent2.copy();

        try {
            Course p2Course = getRandomCourse(parent2);
            Set<Meeting> p2Meetings = parent2.getMeetingsByCourse(p2Course);
            scheduleMeetings(p2Course, p2Meetings, child1);

            Course p1Course = getRandomCourse(parent1);
            Set<Meeting> p1Meetings = parent1.getMeetingsByCourse(p1Course);
            scheduleMeetings(p1Course, p1Meetings, child2);

            return new Timetable[]{child1, child2};
        } catch (CrossoverFailedException cfe) {
            System.err.println(cfe.getMessage());
            cfe.printStackTrace();

            // TODO ideally, we could signal somehow that the crossover has failed
            return new Timetable[]{ parent1.copy(), parent2.copy() };
        }
    }

    private void scheduleMeetings(Course course, Set<Meeting> meetings, Timetable t) throws CrossoverFailedException {
        /* Procedure:
         * 1. For each meeting m1 in meetings:
         *   a) try to set m at m1.day/m1.period.
         *   b) if already occupied by m2 but place would be feasible:
         *     I)  schedule m1 in that slot
         *     II) add m2 to the list of to_be_scheduled
         *   c) if feasible and not occupied:
         *     I) schedule m1 in that slot
         *   d) otherwise:
         *     I) add m1 to to_be_scheduled
         * 2. Greedy insert all from to_be_scheduled
         */

        unscheduleMeetingsByCourse(t, course);
        List<Course> leftovers = scheduleAtSpecifiedPeriods(t, meetings);
        scheduleGreedy(t, leftovers);
    }

    private void unscheduleMeetingsByCourse(Timetable t, Course course) {
        Set<Meeting> meetings = t.getMeetingsByCourse(course);
        meetings.stream().forEach(t::removeMeeting);
    }

    private List<Course> scheduleAtSpecifiedPeriods(Timetable t, Set<Meeting> meetings) {
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
                Room r = toRoom(t.getFreeRoomId(m.getDay(), m.getPeriod()));
                // there still is a free room. schedule directly
                if (r != null) {
                    t.addMeeting(m.copy(r));
                } else {
                    // replace one meeting at this period with the new one
                    Meeting m2 = t.replaceMeeting(m.getDay(), m.getPeriod(), m);
                    leftovers.add(m2.getCourse());
                }
            } else {
                leftovers.add(m.getCourse());
            }
        });

        return leftovers;
    }

    /** Places the specified courses wherever they fit */
    private void scheduleGreedy(Timetable t, List<Course> toBeScheduled) throws CrossoverFailedException {
        for (Course c : toBeScheduled) {

            int day = 0;
            int period = 0;
            while (true) {

                Room r;
                if (isFeasible(t, c, day, period) && (r = toRoom(t.getFreeRoomId(day, period))) != null) {
                    t.addMeeting(new Meeting(c, r, day, period));
                    break;
                } else {
                    if (period < spec.getPeriodsPerDay() - 1) {
                        period++;
                    } else if (day < spec.getNumberOfDaysPerWeek() - 1) {
                        day++;
                        period = 0;
                    } else {
                        throw new CrossoverFailedException("Failed to schedule Course " + c.getId());
                    }
                }

            }
        }
    }

    private Room toRoom(String roomID) {
        if (roomID == null) {
            return null;
        } else {
            return spec.getRooms().stream()
                    .filter(r -> r.getId().equals(roomID))
                    .findFirst().orElse(null);
        }
    }

    private Course getRandomCourse(Timetable t) {
        Meeting[] meetings = t.getMeetings().toArray(new Meeting[t.getMeetings().size()]);
        int idx = new SecureRandom().nextInt(meetings.length);
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

}
