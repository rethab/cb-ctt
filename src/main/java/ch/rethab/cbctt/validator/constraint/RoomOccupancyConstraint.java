package ch.rethab.cbctt.validator.constraint;

import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;

import java.util.HashMap;
import java.util.Map;

/**
 * Only one lecture may be scheduled in a room at a certain period
 *
 * @author Reto Habluetzel, 2015
 */
public class RoomOccupancyConstraint implements Constraint {

    private Specification spec;

    public RoomOccupancyConstraint(Specification spec) {
        this.spec = spec;
    }

    @Override
    public boolean satisfies(Timetable t) {
        Map<Room, boolean[][]> occupies = new HashMap<>();
        for (Meeting meeting : t.getMeetings()) {
            boolean[][] roomOccupancy = occupies.get(meeting.getRoom());
            if (roomOccupancy == null) {
                roomOccupancy = new boolean[spec.getNumberOfDaysPerWeek()][spec.getPeriodsPerDay()];
                occupies.put(meeting.getRoom(), roomOccupancy);
            }
            boolean occupied = roomOccupancy[meeting.getDay()][meeting.getPeriod()];
            if (occupied) {
                return false;
            } else {
                roomOccupancy[meeting.getDay()][meeting.getPeriod()] = true;
            }
        }
        return true;
    }
}
