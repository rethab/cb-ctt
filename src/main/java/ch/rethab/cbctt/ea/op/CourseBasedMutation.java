package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.ea.phenotype.*;
import ch.rethab.cbctt.moea.SolutionConverter;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

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
public class CourseBasedMutation implements Variation {

    // if a mutation fails, it is restarted this many times
    private static final int ATTEMPTS_AFTER_FAIL = 100;

    private final SecureRandom rand = new SecureRandom();

    private final SolutionConverter solutionConverter;

    private final RoomAssigner roomAssigner;

    public CourseBasedMutation(SolutionConverter solutionConverter, RoomAssigner roomAssigner) {
        this.solutionConverter = solutionConverter;
        this.roomAssigner = roomAssigner;
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public Solution[] evolve(Solution[] solutions) {
        TimetableWithRooms original = solutionConverter.fromSolution(solutions[0]);

        TimetableWithRooms mutated;
        int attempts = ATTEMPTS_AFTER_FAIL;
        while ((mutated = mutation(original)) == null && attempts-- >= 0);

        return new Solution[]{solutionConverter.toSolution(mutated)};
    }

    private TimetableWithRooms mutation(TimetableWithRooms original) {

        Timetable mutated = original.newChild();
        Set<Meeting> meetings = mutated.getMeetings();

        // get the two meetings to be exchanged
        ExchangeMeetings exchangeMeetings = getIdx(meetings);

        Meeting a = exchangeMeetings.a;
        Meeting b = exchangeMeetings.b;
        mutated.removeMeeting(a);
        mutated.removeMeeting(b);

        Meeting aNew = new Meeting(a.getCourse(), b.getDay(), b.getPeriod());
        Meeting bNew = new Meeting(b.getCourse(), a.getDay(), a.getPeriod());
        if (!mutated.addMeeting(aNew) || !mutated.addMeeting(bNew)) {
            return null;
        } else {
            return roomAssigner.assignRooms(mutated);
        }
    }

    private ExchangeMeetings getIdx(Set<Meeting> meetings) {
        int idxA = rand.nextInt(meetings.size());
        int idxB;
        while ((idxB = rand.nextInt(meetings.size())) != idxA);

        // convert for index-access
        List<Meeting> list = new ArrayList<>(meetings);
        return new ExchangeMeetings(list.get(idxA), list.get(idxB));
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
