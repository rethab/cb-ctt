package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.moea.VariationFactory;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
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
    public static int NVARIABLES = 7;

    public static final double PROBABILITY_LOWER_BOUND = 0;
    public static final double PROBABILITY_UPPER_BOUND = 1;
    public static final int POPULATION_LOWER_BOUND = 1;
    public static final int POPULATION_UPPER_BOUND = 1000;
    public static final int SECTOR_SIZE_LOWER_BOUND = 1;
    public static final int SECTOR_SIZE_UPPER_BOUND = POPULATION_UPPER_BOUND;
    public static final int ARCHIVE_SIZE_LOWER_BOUND = 1;
    public static final int ARCHIVE_SIZE_UPPER_BOUND = 1000;
    public static final int K_MEANS_LOWER_BOUND = 1;
    public static final int K_MEANS_UPPER_BOUND = POPULATION_UPPER_BOUND;

    // GENERIC EA/CB-CTT VARIABLES
    private final List<Variation> crossoverOperators;
    private final List<Variation> mutationOperators;

    private final int populationSize;

    // SPEA2 SPECIFIC VARIABLES

    /* see commend in moea/spea impl as to why this is offspring size and not archive size (its equivalent) */
    private final int offspringSize;

    private final int k;

    public ParametrizationPhenotype(List<Variation> crossoverOperators, List<Variation> mutationOperators,
                                    int populationSize, int offspringSize, int k) {
        this.crossoverOperators = crossoverOperators;
        this.mutationOperators = mutationOperators;
        this.populationSize = populationSize;
        this.offspringSize = offspringSize;
        this.k = k;
    }

    public static List<Variable> newVariables(CbcttStaticParameters cbcttStaticParameters) {
        VariationFactory variationFactory = cbcttStaticParameters.getVariationFactory();
        List<Variable> variables = new ArrayList<>(NVARIABLES);
        variables.add(EncodingUtils.newInt(SECTOR_SIZE_LOWER_BOUND, SECTOR_SIZE_UPPER_BOUND)); // sector size
        variables.add(EncodingUtils.newBinary(variationFactory.getNumberOfCrossoverOperators())); // crossover op
        variables.add(EncodingUtils.newReal(PROBABILITY_LOWER_BOUND, PROBABILITY_UPPER_BOUND)); // mutation prob
        variables.add(EncodingUtils.newBinary(variationFactory.getNumberOfMutationOperators())); // mutation op
        variables.add(EncodingUtils.newInt(POPULATION_LOWER_BOUND, POPULATION_UPPER_BOUND)); // population size
        variables.add(EncodingUtils.newInt(ARCHIVE_SIZE_LOWER_BOUND, ARCHIVE_SIZE_UPPER_BOUND)); // archive size
        variables.add(EncodingUtils.newInt(K_MEANS_LOWER_BOUND, K_MEANS_UPPER_BOUND));
        return variables;
    }

    public static ParametrizationPhenotype decode(CbcttStaticParameters cbcttStaticParameters, List<Variable> variables) {
        if (NVARIABLES != variables.size()) {
            String msg = "Number of variables %d must be equal to the statically configured amount %d";
            throw new IllegalArgumentException(String.format(msg, variables.size(), NVARIABLES));
        }

        int sectorSize = (int) ((RealVariable) variables.get(0)).getValue();

        BinaryVariable crossoverOpsVar = (BinaryVariable) variables.get(1);
        List<Variation> crossoverOps = new LinkedList<>();
        IntStream.range(0, crossoverOpsVar.getNumberOfBits())
                .filter(crossoverOpsVar::get)
                .forEach(i -> crossoverOps.add(cbcttStaticParameters.getVariationFactory().getCrossoverOperator(i, sectorSize)));

        double mutationProb = ((RealVariable) variables.get(2)).getValue();

        BinaryVariable mutationOpsVar = (BinaryVariable) variables.get(3);
        List<Variation> mutationOps = new LinkedList<>();
        IntStream.range(0, mutationOpsVar.getNumberOfBits())
                .filter(mutationOpsVar::get)
                .forEach(i -> mutationOps.add(cbcttStaticParameters.getVariationFactory().getMutationOperator(i, mutationProb)));

        int populationSize = (int) ((RealVariable) variables.get(4)).getValue();
        int archiveSize = (int) ((RealVariable) variables.get(5)).getValue();

        int k = (int) ((RealVariable) variables.get(6)).getValue();

        return new ParametrizationPhenotype(crossoverOps, mutationOps, populationSize, archiveSize, k);
    }

    public Variation getVariation() {
        CompoundVariation variation = new CompoundVariation();
        this.crossoverOperators.forEach(variation::appendOperator);
        this.mutationOperators.forEach(variation::appendOperator);
        return variation;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getOffspringSize() {
        return offspringSize;
    }

    public int getK() {
        return k;
    }
}

