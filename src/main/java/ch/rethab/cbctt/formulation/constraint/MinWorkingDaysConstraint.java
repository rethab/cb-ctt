package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.ea.Timetable;

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

    @Override
    public int violations(Timetable t) {
        return 0;
    }
}
