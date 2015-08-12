package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Timetable;

import java.util.stream.Collectors;

/**
 * From 'Benchmarking Curriculum-Based Course Timetabling:
 *       Formulations, Data Formats, Instances, Validation, and
 *       Results' (De Cesco et al):
 *
 * Lectures belonging to a curriculum should
 * be adjacent to each other (i.e., in consecutive periods). For a given curriculum we
 * account for a violation every time there is one lecture not adjacent to any other
 * lecture within the same day. Each isolated lecture in a curriculum counts as 1
 * violation.
 *
 * @author Reto Habluetzel, 2015
 */
public class IsolatedLecturesConstraint implements Constraint {

    private final Specification spec;

    public IsolatedLecturesConstraint(Specification spec) {
        this.spec = spec;
    }

    @Override
    public int violations(Timetable t) {
        return t.getCurriculumTimetables()
                .values()
                .stream()
                .map(this::countCurriculumViolations)
                .collect(Collectors.summingInt(Integer::valueOf));
    }

    private int countCurriculumViolations (Timetable.CurriculumTimetable ctt) {
        int violations = 0;
        for (int day = 0; day < spec.getNumberOfDaysPerWeek(); day++) {
            int lecturesPerDay = 0;
            for (int period = 0; period < spec.getPeriodsPerDay(); period++) {

                if (ctt.get(day, period) != null) {
                    lecturesPerDay++;
                }

                if (period == 0) {
                    // first period in day
                    if (ctt.get(day, period) != null && ctt.get(day, period+1) == null) {
                        violations++;
                    }
                } else if (period == spec.getPeriodsPerDay()-1) {
                    // last period of day
                    if (ctt.get(day, period) != null && ctt.get(day, period-1) == null) {
                        violations++;
                    }
                } else {
                    // period in the middle of the day: check both sides
                    if (ctt.get(day, period) != null && ctt.get(day, period-1) == null && ctt.get(day, period+1) == null) {
                        violations++;
                    }
                }
            }

            if (lecturesPerDay == 1) {
                // there was one lecture which was isolated, so remove it again
                violations--;
            }
        }
        return violations;
    }
}
