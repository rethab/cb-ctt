package ch.rethab.cbctt.ea;

import ch.rethab.cbctt.Logger;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import ch.rethab.cbctt.moea.InitializingAlgorithmFactory;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.spi.AlgorithmFactory;

import java.util.concurrent.ExecutorService;

import static ch.rethab.cbctt.meta.ParametrizationPhenotype.formatOperators;

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

    public NondominatedPopulation run(Instrumenter instrumenter) {
        Executor exec = new Executor();
        exec.usingAlgorithmFactory(algorithmFactory);
        exec.withProblemClass(CurriculumBasedTimetabling.class, cbcttStaticParameters.formulation, cbcttStaticParameters.evaluator);
        exec.withAlgorithm(cbcttStaticParameters.algorithmName());
        exec.withProperty("populationSize", params.getPopulationSize());
        exec.withProperty("numberOfOffspring", params.getOffspringSize());
        exec.withProperty("k", params.getK());
        exec.withMaxEvaluations(params.getMaxEvaluations(cbcttStaticParameters));
        exec.distributeWith(executorService);
        exec.withProgressListener(cbcttStaticParameters.getProgressListener());

        if (instrumenter != null) {
            exec.withInstrumenter(instrumenter);
        }

        Logger.info(String.format("Before actual run. Parameters: PopulationSize=%d, OffspringSize=%d, k=%d, Ops=[%s], MaxEvaluations=%d",
                params.getPopulationSize(), params.getOffspringSize(), params.getK(),
                formatOperators(params.getOperators()), params.getMaxEvaluations(cbcttStaticParameters)));

        NondominatedPopulation result = exec.run();

        Logger.info("After actual run");
        return result;
    }

}
