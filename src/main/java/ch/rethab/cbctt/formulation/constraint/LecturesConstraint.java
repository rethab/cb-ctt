package ch.rethab.cbctt.formulation.constraint;


import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;

import java.util.List;
import java.util.Set;

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
    public int violations(Timetable t) {
        int count = 0;
        for (Course c : spec.getCourses()) {
            Set<Meeting> meetings = t.getMeetingsByCourse(c);

            // all lectures scheduled
            if (meetings.size() != c.getNumberOfLectures()) {
                count++;
            }

            // no two meetings at the same time
            boolean[][] occupieds = new boolean[spec.getNumberOfDaysPerWeek()][spec.getPeriodsPerDay()];
            for (Meeting m : meetings) {
                boolean occupied = occupieds[m.getDay()][m.getPeriod()];
                if (occupied) {
                    count++;
                } else {
                    occupieds[m.getDay()][m.getPeriod()] = true;
                }
            }
        }
        return count;
    }
}
