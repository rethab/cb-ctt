package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.StaticParameters;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;

import java.util.concurrent.ExecutorService;

/**
 * @author Reto Habluetzel, 2015
 */
public class CbcttRunner {

    private final ExecutorService executorService;

    private final CbcttStaticParameters cbcttStaticParameters;

    private final ParametrizationPhenotype params;

    public CbcttRunner(ExecutorService executorService, CbcttStaticParameters cbcttStaticParameters, ParametrizationPhenotype params) {
        this.executorService = executorService;
        this.cbcttStaticParameters = cbcttStaticParameters;
        this.params = params;
    }

    public NondominatedPopulation run() {
        Executor exec = new Executor();
        exec.usingAlgorithmFactory(cbcttStaticParameters.algorithmFactory);
        exec.withProblemClass(CurriculumBasedTimetabling.class, cbcttStaticParameters.formulation, cbcttStaticParameters.evaluator);
        exec.withAlgorithm(CbcttStaticParameters.ALGORITHM_NAME);
        exec.withProperty("populationSize", params.getPopulationSize());
        exec.withProperty("archiveSize", params.getArchiveSize()); // todo verify params. jmetal seems to use this, but the native doesnt
        exec.withMaxEvaluations(StaticParameters.MAX_EVALUATIONS);
        exec.distributeWith(executorService);
        return exec.run();
    }

}
