package ch.rethab.cbctt.validator.constraint;


import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;

import java.util.List;

/**
 * All lectures of a course have are scheduled at distinct periods
 *
 * @author Reto Habluetzel, 2015
 */
public class LecturesConstraint implements Constraint {

    private Specification spec;

    public LecturesConstraint(Specification spec) {
        this.spec = spec;
    }

    @Override
    public boolean satisfies(Timetable t) {
        for (Course c : spec.getCourses()) {
            List<Meeting> meetings = t.getMeetingsByCourse(c);

            // all lectures scheduled
            if (meetings.size() != c.getNumberOfLectures()) {
                System.out.printf("Not all lectures scheduled scheduled=%d vs amount=%d\n", meetings.size(), c.getNumberOfLectures());
                return false;
            }

            // no two meetings at the same time
            boolean[][] occupieds = new boolean[spec.getNumberOfDaysPerWeek()][spec.getPeriodsPerDay()];
            for (Meeting m : meetings) {
                boolean occupied = occupieds[m.getDay()][m.getPeriod()];
                if (occupied) {
                    return false;
                } else {
                    occupieds[m.getDay()][m.getPeriod()] = true;
                }
            }
        }
        return true;
    }
}
