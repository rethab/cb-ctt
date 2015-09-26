package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.ea.op.CbcttVariation;
import ch.rethab.cbctt.ea.op.Noop;
import ch.rethab.cbctt.moea.VariationFactory;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
    public static final int K_MEANS_LOWER_BOUND = 1;
    // rather arbitrary, but laumanns  et al suggest 1 one for performance reasons in the
    // pisa implementation of spea2 and it shouldn't get too big
    public static final int K_MEANS_UPPER_BOUND = 20;

    public static final int POPULATION_SIZE_IDX = 0;
    public static final int SECTOR_SIZE_IDX = 1;
    public static final int ARCHIVE_SIZE_IDX = 5;
    public static final int K_IDX = 6;

    // GENERIC EA/CB-CTT VARIABLES
    private final List<CbcttVariation> crossoverOperators;
    private final List<CbcttVariation> mutationOperators;

    private final int populationSize;

    // SPEA2 SPECIFIC VARIABLES

    /* see commend in moea/spea impl as to why this is offspring size and not archive size (its equivalent) */
    private final int offspringSize;

    private final int k;

    public ParametrizationPhenotype(List<CbcttVariation> crossoverOperators, List<CbcttVariation> mutationOperators,
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

        if (variables.size() != POPULATION_SIZE_IDX) { throw new IllegalStateException("Update population size idx"); }
        variables.add(EncodingUtils.newInt(POPULATION_LOWER_BOUND, POPULATION_UPPER_BOUND)); // population size

        if (variables.size() != SECTOR_SIZE_IDX) { throw new IllegalStateException("Update sector size idx"); }
        variables.add(EncodingUtils.newInt(SECTOR_SIZE_LOWER_BOUND, SECTOR_SIZE_UPPER_BOUND)); // sector size
        variables.add(EncodingUtils.newBinary(variationFactory.getNumberOfCrossoverOperators())); // crossover op
        variables.add(EncodingUtils.newReal(PROBABILITY_LOWER_BOUND, PROBABILITY_UPPER_BOUND)); // mutation prob
        variables.add(EncodingUtils.newBinary(variationFactory.getNumberOfMutationOperators())); // mutation op

        if (variables.size() != ARCHIVE_SIZE_IDX) { throw new IllegalStateException("Update archive size idx"); }
        // the upper bound is rather arbitrary and will be reset later
        variables.add(EncodingUtils.newInt(ARCHIVE_SIZE_LOWER_BOUND, POPULATION_UPPER_BOUND));

        if (variables.size() != K_IDX) { throw new IllegalStateException("Update k idx"); }
        // the upper bound is rather arbitrary and will be reset later
        variables.add(EncodingUtils.newInt(K_MEANS_LOWER_BOUND, K_MEANS_UPPER_BOUND));
        return variables;
    }

    public static ParametrizationPhenotype decode(CbcttStaticParameters cbcttStaticParameters, List<Variable> variables) {
        if (NVARIABLES != variables.size()) {
            String msg = "Number of variables %d must be equal to the statically configured amount %d";
            throw new IllegalArgumentException(String.format(msg, variables.size(), NVARIABLES));
        }

        int populationSize = (int) ((RealVariable) variables.get(POPULATION_SIZE_IDX)).getValue();

        int sectorSize = (int) ((RealVariable) variables.get(SECTOR_SIZE_IDX)).getValue();

        BinaryVariable crossoverOpsVar = (BinaryVariable) variables.get(2);
        List<CbcttVariation> crossoverOps = new LinkedList<>();
        IntStream.range(0, crossoverOpsVar.getNumberOfBits())
                .filter(crossoverOpsVar::get)
                .forEach(i -> crossoverOps.add(cbcttStaticParameters.getVariationFactory().getCrossoverOperator(i, sectorSize)));

        double mutationProb = ((RealVariable) variables.get(3)).getValue();

        BinaryVariable mutationOpsVar = (BinaryVariable) variables.get(4);
        List<CbcttVariation> mutationOps = new LinkedList<>();
        IntStream.range(0, mutationOpsVar.getNumberOfBits())
                .filter(mutationOpsVar::get)
                .forEach(i -> mutationOps.add(cbcttStaticParameters.getVariationFactory().getMutationOperator(i, mutationProb)));

        int archiveSize = (int) ((RealVariable) variables.get(ARCHIVE_SIZE_IDX)).getValue();

        int k = (int) ((RealVariable) variables.get(K_IDX)).getValue();

        return new ParametrizationPhenotype(crossoverOps, mutationOps, populationSize, archiveSize, k);
    }

    public static ParametrizationPhenotype fromSolution(CbcttStaticParameters cbcttStaticParameters, Solution s) {
        List<Variable> variables = new ArrayList<>(s.getNumberOfVariables());
        for (int v = 0; v < s.getNumberOfVariables(); v++) {
            variables.add(s.getVariable(v));
        }
        return ParametrizationPhenotype.decode(cbcttStaticParameters, variables);
    }

    public Variation getVariation() {
        if (crossoverOperators.isEmpty() && mutationOperators.isEmpty()) {
            return new Noop();
        } else {
            CompoundVariation variation = new CompoundVariation();
            crossoverOperators.forEach(variation::appendOperator);
            mutationOperators.forEach(variation::appendOperator);
            return variation;
        }
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

    public List<CbcttVariation> getCrossoverOperators() {
        return crossoverOperators;
    }

    public List<CbcttVariation> getMutationOperators() {
        return mutationOperators;
    }

    public static String formatOperators(List<CbcttVariation> ops) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (CbcttVariation op : ops) {
            if (!first) sb.append(", ");
            else        first = false;

            sb.append(op.name());
        }
        return sb.toString();
    }
}

