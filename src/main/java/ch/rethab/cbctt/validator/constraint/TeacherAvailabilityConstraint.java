package ch.rethab.cbctt.validator.constraint;

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
    public boolean satisfies(Timetable t) {
        UnavailabilityConstraints unav = spec.getUnavailabilityConstraints();
        for (Meeting m : t.getMeetings()) {
            if (!unav.checkAvailability(m.getCourse(), m.getDay(), m.getPeriod())) {
                return false;
            }
        }
        return true;
    }
}
