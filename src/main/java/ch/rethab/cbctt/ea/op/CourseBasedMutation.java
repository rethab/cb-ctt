package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.Logger;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.phenotype.Meeting;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.ea.phenotype.Timetable;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import ch.rethab.cbctt.moea.SolutionConverter;
import org.moeaframework.core.Solution;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Inspired by 'New Crossover Algorithms for Timetabling with
 * Evolutionary Algorithms' (Lewis and Paechter), this mutation
 * operator exchanges two courses in a timetable. If the resulting
 * timetable is not feasible, the next two courses are tried.
 *
 * @author Reto Habluetzel, 2015
 */
public class CourseBasedMutation implements CbcttVariation {

    // if a mutation fails, it is restarted this many times
    private static final int ATTEMPTS_AFTER_FAIL = 100;

    private final SecureRandom rand = new SecureRandom();

    private final Specification spec;

    private final SolutionConverter solutionConverter;

    private final RoomAssigner roomAssigner;

    private final double mutationProbability;

    public CourseBasedMutation(Specification spec, SolutionConverter solutionConverter, RoomAssigner roomAssigner,
                               double mutationProbability) {
        this.solutionConverter = solutionConverter;
        this.roomAssigner = roomAssigner;
        this.spec = spec;
        this.mutationProbability = mutationProbability;
    }

    public double getProbability() {
        return mutationProbability;
    }

    @Override
    public String name() {
        return String.format("CourseBasedMutation(prob=%2.3f)", mutationProbability);
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public Solution[] evolve(Solution[] solutions) {

        if (rand.nextDouble() > mutationProbability) {
            return solutions;
        }

        TimetableWithRooms original = solutionConverter.fromSolution(solutions[0]);

        TimetableWithRooms mutated;
        int attempts = ATTEMPTS_AFTER_FAIL;
        while (attempts-- >= 0) {
            mutated = mutation(original);
            if (mutated != null) {
                return new Solution[]{solutionConverter.toSolution(mutated)};
            }
        }

        Logger.info(String.format("Mutation failed after %d attempts\n", ATTEMPTS_AFTER_FAIL));
        return new Solution[0];
    }

    private TimetableWithRooms mutation(TimetableWithRooms original) {

        int attempts = ATTEMPTS_AFTER_FAIL;
        while (attempts-- >= 0) {
            Timetable offspring = original.newChild();
            Set<Meeting> meetings = offspring.getMeetings();

            // get the two meetings to be exchanged from the same curriculum
            ExchangeMeetings exchangeMeetings = getIdx(meetings);

            // could not find distinct meeting.
            if (exchangeMeetings == null) {
                // returns directly since this should only happen if there are no two distinct meetings
                return null;
            }

            if (exchange(offspring, exchangeMeetings)) {
                return roomAssigner.assignRooms(offspring);
            }

        }

        return null;
    }

    private boolean exchange(Timetable mutated, ExchangeMeetings exchangeMeetings) {

        Meeting a = exchangeMeetings.a;
        Meeting b = exchangeMeetings.b;
        mutated.removeMeeting(a);
        mutated.removeMeeting(b);

        Meeting aNew = new Meeting(a.getCourse(), b.getDay(), b.getPeriod());
        Meeting bNew = new Meeting(b.getCourse(), a.getDay(), a.getPeriod());

        if (!isReduceFeasible(mutated, aNew) || !isReduceFeasible(mutated, bNew)) {
            return false;
        }

        // add checks for: rooms-per-period and not-other-course-of-same-curriculum-in-smae-period
        try {
            return mutated.addMeeting(aNew) && mutated.addMeeting(bNew);
        } catch (Timetable.InfeasibilityException efe) {
            return false;
        }
    }

    /**
     * Makes a reduced feasibility check for the meeting if it was scheduled in the
     * specified timetable.
     *
     * Reduced means: There are other checks necessary to make sure adding this
     *                meeting actually results in a feasible timetable.
     *
     * This checks: Unavailability Constraint, Same Teacher in Period Constraint
     */
    private boolean isReduceFeasible(Timetable timetable, Meeting m) {
        if (!spec.getUnavailabilityConstraints().checkAvailability(m.getCourse(), m.getDay(), m.getPeriod())) {
            return false;
        } else if (timetable.hasLectureWithSameTeacher(m.getCourse().getTeacher(), m.getDay(), m.getPeriod())) {
            return false;
        } else {
            return true;
        }
    }

    private ExchangeMeetings getIdx(Set<Meeting> meetings) {
        int idxA = rand.nextInt(meetings.size());
        int idxB;

        int attempts = ATTEMPTS_AFTER_FAIL;
        while (attempts-- >= 0) {
            idxB = rand.nextInt(meetings.size());
            if (idxB != idxA) {
                // convert for index-access
                List<Meeting> list = new ArrayList<>(meetings);
                return new ExchangeMeetings(list.get(idxA), list.get(idxB));
            }
        }

        Logger.info(String.format("Failed to find a distinct index after %d attempts\n", ATTEMPTS_AFTER_FAIL));
        return null;
    }

    // help class to return which two meetings are to be exchanged
    private static class ExchangeMeetings {
        final Meeting a;
        final Meeting b;

        public ExchangeMeetings(Meeting a, Meeting b) {
            this.a = a;
            this.b = b;
        }
    }
}
