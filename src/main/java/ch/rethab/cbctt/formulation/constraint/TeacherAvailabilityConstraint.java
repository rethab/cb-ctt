package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.domain.UnavailabilityConstraints;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;

/**
 * @author Reto Habluetzel, 2015
 */
public class TeacherAvailabilityConstraint implements Constraint {

    private final Specification spec;

    public TeacherAvailabilityConstraint(Specification spec) {
        this.spec = spec;
    }

    @Override
    public int violations(Timetable t) {
        int count = 0;
        UnavailabilityConstraints unav = spec.getUnavailabilityConstraints();
        for (Meeting m : t.getMeetings()) {
            if (!unav.checkAvailability(m.getCourse(), m.getDay(), m.getPeriod())) {
                count++;
            }
        }
        return count;
    }
}
