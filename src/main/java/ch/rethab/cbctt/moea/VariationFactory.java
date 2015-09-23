package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.op.CourseBasedCrossover;
import ch.rethab.cbctt.ea.op.CourseBasedMutation;
import ch.rethab.cbctt.ea.op.CurriculumBasedCrossover;
import ch.rethab.cbctt.ea.op.SectorBasedCrossover;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import org.moeaframework.core.Variation;

/**
 * @author Reto Habluetzel, 2015
 */
public class VariationFactory {

    private final Specification spec;

    private final SolutionConverter solutionConverter;

    private final RoomAssigner roomAssigner;

    public VariationFactory(Specification spec, SolutionConverter solutionConverter, RoomAssigner roomAssigner) {
        this.spec = spec;
        this.solutionConverter = solutionConverter;
        this.roomAssigner = roomAssigner;
    }

    public int getNumberOfMutationOperators() {
        return 1;
    }

    public Variation getMutationOperator(int idx, double mutationProbability) {
        if (idx == 0) {
            return new CourseBasedMutation(spec, solutionConverter, roomAssigner, mutationProbability);
        } else {
            throw new IllegalArgumentException("There is only one mutation operator");
        }
    }

    public int getNumberOfCrossoverOperators() {
        return 3;
    }

    public Variation getCrossoverOperator(int i, int sectorSize) {
        if (i == 0){
            return new CourseBasedCrossover(spec, solutionConverter, roomAssigner);
        } else if (i == 1) {
            return new CurriculumBasedCrossover(spec, solutionConverter, roomAssigner);
        } else if (i == 2) {
            return new SectorBasedCrossover(spec, solutionConverter, roomAssigner, sectorSize);
        } else {
            throw new IllegalArgumentException("There aren't that many crossover operators");
        }
    }
}
