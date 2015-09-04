package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Curriculum;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.phenotype.MeetingWithRoom;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;

import java.util.Set;

/**
 * From 'Benchmarking Curriculum-Based Course Timetabling:
 *       Formulations, Data Formats, Instances, Validation, and
 *       Results' (De Cesco et al):
 *
 * Conflicts: Lectures of courses in the same curriculum or taught by the same teacher
 * must be all scheduled in different periods. Two conflicting lectures in the same
 * period represent one violation. Three conflicting lectures count as 3 violations: one
 * for each pair.
 *
 * @author Reto Habluetzel, 2015
 */
public class ConflictsConstraint implements Constraint {

    private Specification spec;

    public ConflictsConstraint(Specification spec) {
        this.spec = spec;
    }

    @Override
    public String name() {
        return "Conflicts";
    }

    @Override
    public int violations(TimetableWithRooms t) {
        return lecturesByCurriculaViolations(t) + teacherViolations(t);
    }

    private int teacherViolations(TimetableWithRooms t) {
        int count = 0;
        for (String teacher : spec.getTeachers()) {
            // occupied day/periods by teacher
            boolean occupieds[][] = new boolean[spec.getNumberOfDaysPerWeek()][spec.getPeriodsPerDay()];

            Set<MeetingWithRoom> meetings = t.getMeetingsByTeacher(teacher);
            for (MeetingWithRoom meeting : meetings) {
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

    private int lecturesByCurriculaViolations(TimetableWithRooms t) {
        int count = 0;
        for (Curriculum curriculum : spec.getCurricula()) {
            // no two lectures within curriculum on same day
            boolean occupieds[][] = new boolean[spec.getNumberOfDaysPerWeek()][spec.getPeriodsPerDay()];
            for (Course course : curriculum.getCourses()) {
                Set<MeetingWithRoom> meetings = t.getMeetingsByCourse(course);
                for (MeetingWithRoom meeting : meetings) {
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
