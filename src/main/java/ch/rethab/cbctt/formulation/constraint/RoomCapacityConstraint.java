package ch.rethab.cbctt.formulation.constraint;

import ch.rethab.cbctt.ea.Timetable;

/**
 * From 'Benchmarking Curriculum-Based Course Timetabling:
 *       Formulations, Data Formats, Instances, Validation, and
 *       Results' (De Cesco et al):
 *
 * RoomCapacity: For each lecture, the number of students that attend the course must
 * be less or equal than the number of seats of all the rooms that host its lectures.
 *
 * @author Reto Habluetzel, 2015
 */
public class RoomCapacityConstraint implements Constraint {

    @Override
    public int violations(Timetable t) {
        return 0;
    }
}
