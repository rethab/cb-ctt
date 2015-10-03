package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.Logger;
import ch.rethab.cbctt.ea.CbcttRunner;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Reto Habluetzel, 2015
 */
public class MetaCurriculumBasedTimetabling implements Problem, Serializable {

    private static final int NOBJECTIVES = 1;

    /** run the algorithm several times to reduce noise */
    public static final int RUNS = 3;

    private final MetaStaticParameters metaStaticParameters;

    public MetaCurriculumBasedTimetabling(MetaStaticParameters metaStaticParameters) {
        this.metaStaticParameters = metaStaticParameters;
    }

    @Override
    public String getName() {
        return MetaCurriculumBasedTimetabling.class.getSimpleName();
    }

    @Override
    public int getNumberOfVariables() {
        return ParametrizationPhenotype.NVARIABLES;
    }

    @Override
    public int getNumberOfObjectives() {
        return NOBJECTIVES;
    }

    @Override
    public int getNumberOfConstraints() {
        return 0;
    }

    @Override
    public void evaluate(Solution solution) {
        List<Variable> variables = IntStream
                .range(0, solution.getNumberOfVariables())
                .mapToObj(solution::getVariable)
                .collect(Collectors.toList());

        CbcttStaticParameters cbcttStaticParameters = metaStaticParameters.getCbcttStaticParameters();
        ParametrizationPhenotype params = ParametrizationPhenotype.decode(cbcttStaticParameters, variables);
        CbcttRunner cbcttRunner = new CbcttRunner(cbcttStaticParameters, params);
        MetaEvaluator evaluator = new MetaEvaluator(this, metaStaticParameters.referenceSet);

        boolean failedAlready = false;
        double[] results = new double[RUNS];
        int i = 0;
        while (i < RUNS) {
            try {
                NondominatedPopulation result = cbcttRunner.run(null);
                results[i] = evaluator.evaluate(result);

                // reset failed flag. it should be per run
                failedAlready = false;
                i++;
            } catch (Exception e) {
                if (failedAlready) {
                    Logger.info("It's happening again! There's no hope..");
                    throw e;
                } else {
                    Logger.info("Exception in CbCttRunner (will try again): " + e.getMessage());
                    e.printStackTrace();
                    failedAlready = true;
                }
            }
        }

        double avg = Arrays.stream(results).average().getAsDouble();
        Logger.info("Average Indicator: " + (int) avg);

        solution.setObjective(0, avg);
    }

    @Override
    public Solution newSolution() {
        List<Variable> variables = ParametrizationPhenotype.newVariables(metaStaticParameters.getCbcttStaticParameters());
        Solution solution = new Solution(variables.size(), NOBJECTIVES);
        IntStream.range(0, variables.size()).forEach(i -> solution.setVariable(i, variables.get(i)) );
        return solution;
    }

    @Override
    public void close() {

    }
}
