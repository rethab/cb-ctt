package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.domain.UnavailabilityConstraints;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;

/**
 * From 'Benchmarking Curriculum-Based Course Timetabling:
 *       Formulations, Data Formats, Instances, Validation, and
 *       Results' (De Cesco et al):
 *
 * Availability: If the teacher of the course is not available to teach that course at a given
 * period, then no lecture of the course can be scheduled at that period. Each lecture
 * in a period unavailable for that course is one violation.
 *
 * @author Reto Habluetzel, 2015
 */
public class TeacherAvailabilityConstraint implements Constraint {

    private final Specification spec;

    public TeacherAvailabilityConstraint(Specification spec) {
        this.spec = spec;
    }

    @Override
    public String name() {
        return "TeacherAvailability";
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
