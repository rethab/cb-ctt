package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.ea.initializer.Initializer;
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

    private final Specification spec;

    private final SolutionConverter solutionConverter;

    public InitializationFactory(Formulation formulation, Initializer initializer, Specification spec) {
        this.initializer = initializer;
        this.spec = spec;
        this.solutionConverter = new SolutionConverter(formulation);
    }

    public Initialization create(int populationSize) {
        return () -> {
            List<Timetable> timetables = initializer.initialize(spec, populationSize);
            List<Solution> solutions =  timetables.stream().map(solutionConverter::toSolution).collect(Collectors.toList());
            return solutions.toArray(new Solution[populationSize]);
        };
    }

}
