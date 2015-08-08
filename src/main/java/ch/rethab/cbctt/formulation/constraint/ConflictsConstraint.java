package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;

import java.util.List;

/**
 * Lectures of same curriculum or same teacher must be in different periods
 * @author Reto Habluetzel, 2015
 */
public class ConflictsConstraint implements Constraint {

    private Specification spec;

    public ConflictsConstraint(Specification spec) {
        this.spec = spec;
    }

    @Override
    public int violations(Timetable t) {
        return lecturesByCurriculaViolations(t) + teacherViolations(t);
    }

    private int teacherViolations(Timetable t) {
        int count = 0;
        for (String teacher : spec.getTeachers()) {
            // occupied day/periods by teacher
            boolean occupieds[][] = new boolean[spec.getNumberOfDaysPerWeek()][spec.getPeriodsPerDay()];

            List<Meeting> meetings = t.getMeetingsByTeacher(teacher);
            for (Meeting meeting : meetings) {
                boolean occupied = occupieds[meeting.getDay()][meeting.getPeriod()];
                if (occupied) {
                    count++;
                } else {
                    occupieds[meeting.getDay()][meeting.getPeriod()] = true;
                }
            }
        }
        return count;
    }

    private int lecturesByCurriculaViolations(Timetable t) {
        int count = 0;
        for (Curriculum curriculum : spec.getCurricula()) {
            // no two lectures within curriculum on same day
            boolean occupieds[][] = new boolean[spec.getNumberOfDaysPerWeek()][spec.getPeriodsPerDay()];
            for (Course course : curriculum.getCourses()) {
                List<Meeting> meetings = t.getMeetingsByCourse(course);
                for (Meeting meeting : meetings) {
                    boolean occupied = occupieds[meeting.getDay()][meeting.getPeriod()];
                    if (occupied) {
                        count++;
                    } else {
                        occupieds[meeting.getDay()][meeting.getPeriod()] = true;
                    }
                }

            }
        }
        return count;
    }
}
