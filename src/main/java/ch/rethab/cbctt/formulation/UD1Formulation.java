package ch.rethab.cbctt.formulation;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.formulation.constraint.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class UD1Formulation extends Formulation {

    public UD1Formulation(Specification spec) {
        super(
            new Constraint[] {
                new LecturesConstraint(spec),
                new ConflictsConstraint(spec),
                new RoomOccupancyConstraint(spec),
                new TeacherAvailabilityConstraint(spec)
            },
            new Objective[] {
                new Objective(new RoomCapacityConstraint(), 1),
                new Objective(new MinWorkingDaysConstraint(spec), 5),
                new Objective(new IsolatedLecturesConstraint(spec), 1)
            }
        );
    }
}
