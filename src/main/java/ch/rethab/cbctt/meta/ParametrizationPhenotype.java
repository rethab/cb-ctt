package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.StaticParameters;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Phenotype of the meta-evolutionary algorithm. Holds all variables that
 * are to be optimized.
 */
public final class ParametrizationPhenotype {

    // total number of variables to optimize
    public static int NVARIABLES = 6;

    public static final double PROBABILITY_LOWER_BOUND = 0;
    public static final double PROBABILITY_UPPER_BOUND = 1;
    public static final int POPULATION_LOWER_BOUND = 1;
    public static final int POPULATION_UPPER_BOUND = 1000;
    public static final int ARCHIVE_SIZE_LOWER_BOUND = 1;
    public static final int ARCHIVE_SIZE_UPPER_BOUND = 1000;

    // GENERIC EA/CB-CTT VARIABLES
    private final List<Variation> crossoverOperators;

    private final double crossoverProbability;

     /* there currently is only one. however, the meta-ea may come to the conclusion that it
      * is better not to use one at all. at least as of now, the crossover operator has a
      * repair function, which also introduces some mutation. this may be a reason for that. */
    private final List<Variation> mutationOperators;

    private final double mutationProbability;

    private final int populationSize;

    // SPEA2 SPECIFIC VARIABLES
    private final int archiveSize;

    public ParametrizationPhenotype(List<Variation> crossoverOperators, double crossoverProbability,
                                    List<Variation> mutationOperators, double mutationProbability,
                                    int populationSize, int archiveSize) {
        this.crossoverOperators = crossoverOperators;
        this.crossoverProbability  = crossoverProbability;
        this.mutationOperators = mutationOperators;
        this.mutationProbability = mutationProbability;
        this.populationSize = populationSize;
        this.archiveSize = archiveSize;
    }

    public static List<Variable> newVariables(StaticParameters ops) {
        List<Variable> variables = new ArrayList<>(NVARIABLES);
        variables.add(EncodingUtils.newBinary(ops.crossover.size())); // crossover op
        variables.add(EncodingUtils.newReal(PROBABILITY_LOWER_BOUND, PROBABILITY_UPPER_BOUND)); // crossover prob
        variables.add(EncodingUtils.newBinary(ops.mutation.size())); // mutation op
        variables.add(EncodingUtils.newReal(PROBABILITY_LOWER_BOUND, PROBABILITY_UPPER_BOUND)); // mutation prob
        variables.add(EncodingUtils.newInt(POPULATION_LOWER_BOUND, POPULATION_UPPER_BOUND)); // population size
        variables.add(EncodingUtils.newInt(ARCHIVE_SIZE_LOWER_BOUND, ARCHIVE_SIZE_UPPER_BOUND)); // archive size
        return variables;
    }

    public static ParametrizationPhenotype decode(StaticParameters ops, List<Variable> variables) {
        if (NVARIABLES != variables.size()) {
            throw new IllegalArgumentException("Number of variables must be equal to the statically configured amount");
        }

        BinaryVariable crossoverOpsVar = (BinaryVariable) variables.get(0);
        List<Variation> crossoverOps = new LinkedList<>();
        IntStream.range(0, crossoverOpsVar.getNumberOfBits())
                .filter(crossoverOpsVar::get)
                .forEach(i -> crossoverOps.add(ops.crossover.get(i)));

        double crossoverProb = ((RealVariable) variables.get(1)).getValue();

        BinaryVariable mutationOpsVar = (BinaryVariable) variables.get(2);
        List<Variation> mutationOps = new LinkedList<>();
        IntStream.range(0, mutationOpsVar.getNumberOfBits())
                .filter(mutationOpsVar::get)
                .forEach(i -> mutationOps.add(ops.mutation.get(i)));

        double mutationProb = ((RealVariable) variables.get(3)).getValue();

        int populationSize = (int) ((RealVariable) variables.get(4)).getValue();
        int archiveSize = (int) ((RealVariable) variables.get(5)).getValue();

        return new ParametrizationPhenotype(crossoverOps, crossoverProb, mutationOps, mutationProb,
                populationSize, archiveSize);

    }

    public List<Variation> getCrossoverOperators() {
        return Collections.unmodifiableList(crossoverOperators);
    }

    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    public List<Variation> getMutationOperators() {
        return Collections.unmodifiableList(mutationOperators);
    }

    public double getMutationProbability() {
        return mutationProbability;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getArchiveSize() {
        return archiveSize;
    }
}

