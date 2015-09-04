package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.ea.initializer.Initializer;
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
public final class InitializationFactory {

    private final Initializer initializer;

    private final SolutionConverter solutionConverter;

    public InitializationFactory(Formulation formulation, Initializer initializer) {
        this.initializer = initializer;
        this.solutionConverter = new SolutionConverter(formulation);
    }

    public Initialization create(int populationSize) {
        return () -> {
            List<TimetableWithRooms> timetables = initializer.initialize(populationSize);
            List<Solution> solutions =  timetables.stream().map(solutionConverter::toSolution).collect(Collectors.toList());
            return solutions.toArray(new Solution[populationSize]);
        };
    }

}
