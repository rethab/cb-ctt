package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.ea.Timetable;

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

    @Override
    public int violations(Timetable t) {
        return 0;
    }
}
