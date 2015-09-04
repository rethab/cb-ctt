package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.phenotype.MeetingWithRoom;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;

import java.util.HashMap;
import java.util.Map;

/**
 * From 'Benchmarking Curriculum-Based Course Timetabling:
 *       Formulations, Data Formats, Instances, Validation, and
 *       Results' (De Cesco et al):
 *
 * RoomOccupancy: Two lectures cannot take place in the same room in the same period.
 * Two lectures in the same room at the same period represent one violation. Any
 * extra lecture in the same period and room counts as one more violation.
 *
 * @author Reto Habluetzel, 2015
 */
public class RoomOccupancyConstraint implements Constraint {

    private Specification spec;

    public RoomOccupancyConstraint(Specification spec) {
        this.spec = spec;
    }

    @Override
    public String name() {
        return "RoomOccupancy";
    }

    @Override
    public int violations(TimetableWithRooms t) {
        int count = 0;
        Map<Room, boolean[][]> occupies = new HashMap<>();
        for (MeetingWithRoom meeting : t.getMeetings()) {
            boolean[][] roomOccupancy = occupies.get(meeting.getRoom());
            if (roomOccupancy == null) {
                roomOccupancy = new boolean[spec.getNumberOfDaysPerWeek()][spec.getPeriodsPerDay()];
                occupies.put(meeting.getRoom(), roomOccupancy);
            }
            boolean occupied = roomOccupancy[meeting.getDay()][meeting.getPeriod()];
            if (occupied) {
                count++;
            } else {
                roomOccupancy[meeting.getDay()][meeting.getPeriod()] = true;
            }
        }
        return count;
    }
}
