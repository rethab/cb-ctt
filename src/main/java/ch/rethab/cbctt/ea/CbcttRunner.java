package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.Logger;
import ch.rethab.cbctt.ea.op.CbcttVariation;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import ch.rethab.cbctt.moea.InitializingAlgorithmFactory;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.spi.AlgorithmFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author Reto Habluetzel, 2015
 */
public class CbcttRunner {

    private final ExecutorService executorService;

    private final AlgorithmFactory algorithmFactory;

    private final CbcttStaticParameters cbcttStaticParameters;

    private final ParametrizationPhenotype params;

    public CbcttRunner(ExecutorService executorService, CbcttStaticParameters cbcttStaticParameters, ParametrizationPhenotype params) {
        this.executorService = executorService;
        this.algorithmFactory = new InitializingAlgorithmFactory(cbcttStaticParameters, params.getVariation());
        this.cbcttStaticParameters = cbcttStaticParameters;
        this.params = params;
    }

    public NondominatedPopulation run() {
        Executor exec = new Executor();
        exec.usingAlgorithmFactory(algorithmFactory);
        exec.withProblemClass(CurriculumBasedTimetabling.class, cbcttStaticParameters.formulation, cbcttStaticParameters.evaluator);
        exec.withAlgorithm(cbcttStaticParameters.algorithmName());
        exec.withProperty("populationSize", params.getPopulationSize());
        exec.withProperty("numberOfOffspring", params.getOffspringSize());
        exec.withProperty("k", params.getK());
        exec.withMaxEvaluations(cbcttStaticParameters.maxEvaluations());
        exec.distributeWith(executorService);

        Logger.info(String.format("Before actual run. Parameters: PopulationSize=%d, OffspringSize=%d, k=%d, CrossoverOps=[%s], MutationOps=[%s]",
                params.getPopulationSize(), params.getOffspringSize(), params.getK(),
                formatOperators(params.getCrossoverOperators()), formatOperators(params.getMutationOperators())));

        NondominatedPopulation result = exec.run();

        Logger.trace("EXIT (" + result.size() + ")");
        return result;
    }

    private static String formatOperators(List<CbcttVariation> ops) {
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
