package ch.rethab.cbctt.meta;

import ch.rethab.cbctt.ea.CbcttRunner;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Reto Habluetzel, 2015
 */
public class MetaCurriculumBasedTimetabling implements Problem {

    private static final int NOBJECTIVES = 1;

    private final MetaStaticParameters metaStaticParameters;

    private final ExecutorService executorService;

    public MetaCurriculumBasedTimetabling(MetaStaticParameters metaStaticParameters, ExecutorService executorService) {
        this.metaStaticParameters = metaStaticParameters;
        this.executorService = executorService;
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
        CbcttRunner cbcttRunner = new CbcttRunner(executorService, cbcttStaticParameters, params);
        MetaEvaluator evaluator = new MetaEvaluator(this, metaStaticParameters.referenceSet);
        NondominatedPopulation result = cbcttRunner.run();
        solution.setObjective(0, evaluator.evaluate(result));
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
