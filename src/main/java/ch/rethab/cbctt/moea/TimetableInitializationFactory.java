package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.initializer.TeacherGreedyInitializer;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import ch.rethab.cbctt.formulation.Formulation;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Solution;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter to the MOEA Framework Initializer. Essentially doing
 * the conversion from the cb-ctt initializer to the the one from
 * the MOEA framework.
 *
 * @author Reto Habluetzel, 2015
 */
public final class TimetableInitializationFactory implements InitializationFactory{

    private final Specification spec;

    private final RoomAssigner roomAssigner;

    private final SolutionConverter solutionConverter;

    public TimetableInitializationFactory(Specification spec, Formulation formulation, RoomAssigner roomAssigner) {
        this.spec = spec;
        this.roomAssigner = roomAssigner;
        this.solutionConverter = new SolutionConverter(formulation);
    }

    @Override
    public Initialization create(int populationSize) {
        return () -> {
            TeacherGreedyInitializer teacherGreedyInitializer = new TeacherGreedyInitializer(spec, roomAssigner);
            List<TimetableWithRooms> timetables = teacherGreedyInitializer.initialize(populationSize);
            List<Solution> solutions = timetables.stream().map(solutionConverter::toSolution).collect(Collectors.toList());
            return solutions.toArray(new Solution[populationSize]);
        };
    }

}
