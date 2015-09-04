package ch.rethab.cbctt.formulation.constraint;


import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;

import java.util.stream.Collectors;

/**
 * From 'Benchmarking Curriculum-Based Course Timetabling:
 *       Formulations, Data Formats, Instances, Validation, and
 *       Results' (De Cesco et al):
 *
 * Lectures: All lectures of a course must be scheduled, and they must be assigned to
 * distinct periods. A violation occurs if a lecture is not scheduled or two lectures are
 * in the same period.
 *
 * @author Reto Habluetzel, 2015
 */
public class LecturesConstraint implements Constraint {

    private Specification spec;

    public LecturesConstraint(Specification spec) {
        this.spec = spec;
    }

    @Override
    public String name() {
        return "Lectures";
    }

    @Override
    public int violations(TimetableWithRooms t) {
        /*
         * the construction of the timetable makes sure no
         * two lectures may be scheduled at the same period
         */
        return spec.getCourses()
                .stream()
                .map(c -> c.getNumberOfLectures() - t.getMeetingsByCourse(c).size())
                .collect(Collectors.summingInt(Integer::valueOf));
    }
}
