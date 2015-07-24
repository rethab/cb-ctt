package ch.rethab.cbctt.validator;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.validator.constraint.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author Reto Habluetzel, 2015
 */
public class UD1Validator implements Validator {

    private final List<Constraint> hardConstraints;

    public UD1Validator(Specification spec) {
         hardConstraints = Arrays.asList(
                 new LecturesConstraint(spec),
                 new ConflictsConstraint(spec),
                 new RoomOccupancyConstraint(spec),
                 new TeacherAvailabilityConstraint(spec)
         );
    }

    @Override
    public boolean isFeasible(Timetable t) {
        for (Constraint constraint : hardConstraints) {
            if (!constraint.satisfies(t)) {
                System.out.println("Constraint fails: " + constraint.getClass().getName());
                return false;
            }
        }
        return true;
    }
}
