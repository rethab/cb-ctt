package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.phenotype.MeetingWithRoom;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;

import java.util.stream.Collectors;

/**
 * From 'Benchmarking Curriculum-Based Course Timetabling:
 *       Formulations, Data Formats, Instances, Validation, and
 *       Results' (De Cesco et al):
 *
 * MinWorkingDays: The lectures of each course must be spread into the given minimum
 * number of days. Each day below the minimum counts as 1 violation.
 *
 * @author Reto Habluetzel, 2015
 */
public class MinWorkingDaysConstraint implements Constraint {

    private final Specification spec;

    public MinWorkingDaysConstraint(Specification spec) {
        this.spec = spec;
    }

    @Override
    public String name() {
        return "MinWorkingDays";
    }

    @Override
    public int violations(TimetableWithRooms t) {
        return spec.getCourses().stream()
                .map(c -> Math.max(0, c.getMinWorkingDays() - countWorkingDays(c, t)))
                .collect(Collectors.summingInt(Integer::valueOf));
    }

    private int countWorkingDays(Course c, TimetableWithRooms t) {
        boolean[] days = new boolean[spec.getNumberOfDaysPerWeek()];

        int ndays = 0;
        for (MeetingWithRoom meeting : t.getMeetingsByCourse(c)) {
            if (!days[meeting.getDay()]) {
                ndays++;
            }
            days[meeting.getDay()] = true;
        }

        return ndays;
    }
}
