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
public class InitializerAdapter implements Initialization {

    private final Initializer initializer;

    private final Specification spec;

    private final int populationSize;

    private final SolutionConverter solutionConverter;

    public InitializerAdapter(Formulation formulation, Initializer initializer, Specification spec, int populationSize) {
        this.initializer = initializer;
        this.spec = spec;
        this.populationSize = populationSize;
        this.solutionConverter = new SolutionConverter(formulation);
    }

    @Override
    public Solution[] initialize() {
        List<Timetable> timetables = this.initializer.initialize(this.spec, this.populationSize);
        List<Solution> solutions =  timetables.stream().map(t -> this.solutionConverter.toSolution(t)).collect(Collectors.toList());
        return solutions.toArray(new Solution[0]);
    }
}
