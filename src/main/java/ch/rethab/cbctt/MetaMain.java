package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.ea.op.HuxSbx;
import ch.rethab.cbctt.ea.op.PmBf;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.meta.MetaCurriculumBasedTimetabling;
import ch.rethab.cbctt.meta.MetaEvaluator;
import ch.rethab.cbctt.meta.MetaStaticParameters;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import ch.rethab.cbctt.moea.InitializingAlgorithmFactory;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.moea.TimetableInitializationFactory;
import ch.rethab.cbctt.moea.VariationFactory;
import ch.rethab.cbctt.parser.ECTTParser;
import org.jppf.client.JPPFClient;
import org.jppf.client.concurrent.JPPFExecutorService;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.spi.AlgorithmFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;

import static ch.rethab.cbctt.meta.ParametrizationPhenotype.formatOperators;

/**
 * @author Reto Habluetzel, 2015
 */
public class MetaMain {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new FileNotFoundException("First parameter must be file that exists!");
        }
        String filename = args[0];

        Logger.configuredLevel = Logger.Level.GIBBER;

        Specification spec = new ECTTParser(new BufferedReader(new FileReader(filename))).parse();
        RoomAssigner roomAssigner = new GreedyRoomAssigner(spec);
        Formulation formulation = new UD1Formulation(spec);
        SolutionConverter solutionConverter = new SolutionConverter(formulation);
        Evaluator evaluator = new Evaluator(formulation, solutionConverter);

        int maxEvaluations = 1000;
        int populationSize = 30;
        int offspringSize =  30;
        int k = 1;
        String cpFilename = "max-10000-pop-30-off-30-k-1-cbbgen-75-maxpop-400";

        // ExecutorService executorService = Executors.newFixedThreadPool(5);

        JPPFClient jppfClient = new JPPFClient();
        JPPFExecutorService executorService = new JPPFExecutorService(jppfClient);
        executorService.setBatchSize(populationSize);
        executorService.setBatchTimeout(100);

        // values from moea framework
        double huxProbability = 1;
        double sbxProbability = 1;
        double sbxDistributionIndex = 15;
        double pmProbability = 0.16;
        double pmDistributionIndex = 20;
        double bfProbability = 0.01;

        int cbcttGenerations = 75;

        System.out.printf("Starting new Computation with: MetaPopulationSize=%d, MetaOffspringSize=%d, MetaK=%d, " +
                "MaxEvaluations=%d, CbcttGenerations=%d, CbcttMaxPopulationSize=%d, CbcttRuns=%d\n",
                populationSize, offspringSize, k, maxEvaluations, cbcttGenerations,
                ParametrizationPhenotype.POPULATION_UPPER_BOUND, MetaCurriculumBasedTimetabling.RUNS);

        TimetableInitializationFactory cbcttInitializationFactory = new TimetableInitializationFactory(spec, formulation, roomAssigner);
        VariationFactory variationFactory = new VariationFactory(spec, solutionConverter, roomAssigner);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(cbcttGenerations, Logger.Level.TRACE, formulation, evaluator, cbcttInitializationFactory, variationFactory);
        MetaStaticParameters metaStaticParameters = new MetaStaticParameters(cbcttStaticParameters);

        HuxSbx crossover = new HuxSbx(huxProbability, sbxProbability, sbxDistributionIndex);
        PmBf mutation = new PmBf(pmProbability, pmDistributionIndex, bfProbability);
        Variation metaVariation = new CompoundVariation(crossover, mutation);

        AlgorithmFactory algorithmFactory = new InitializingAlgorithmFactory(metaStaticParameters, metaVariation);

        Executor exec = new Executor();
        exec.withProblemClass(MetaCurriculumBasedTimetabling.class, metaStaticParameters);
        exec.withAlgorithm(metaStaticParameters.algorithmName());
        exec.usingAlgorithmFactory(algorithmFactory);
        exec.withMaxEvaluations(maxEvaluations);
        exec.withProperty("populationSize", populationSize);
        exec.withProperty("numberOfOffspring", offspringSize);
        exec.withProperty("k", k);
        exec.distributeWith(executorService);
        exec.withProgressListener(metaStaticParameters.getProgressListener());
        exec.withCheckpointFile(new File(cpFilename));
        exec.withCheckpointFrequency(populationSize);


        NondominatedPopulation  result;
        try {
            result = exec.run();
        } finally {
            executorService.shutdownNow();
        }

        jppfClient.close();

        System.out.println("End Result Ready");
        for (Solution s : result) {
            ParametrizationPhenotype params = ParametrizationPhenotype.fromSolution(cbcttStaticParameters, s);
            System.out.printf("Parameters: PopulationSize=%d, OffspringSize=%d, k=%d, Ops=[%s]",
                    params.getPopulationSize(), params.getOffspringSize(), params.getK(),
                    formatOperators(params.getOperators()));
        }


        // jppfExecutorService.shutdown();
        // jppfClient.close();
    }
}
