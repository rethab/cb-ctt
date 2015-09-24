package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.meta.MetaCurriculumBasedTimetabling;
import ch.rethab.cbctt.meta.MetaStaticParameters;
import ch.rethab.cbctt.moea.InitializingAlgorithmFactory;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.moea.TimetableInitializationFactory;
import ch.rethab.cbctt.moea.VariationFactory;
import ch.rethab.cbctt.parser.ECTTParser;
import org.moeaframework.Executor;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.real.SBX;
import org.moeaframework.core.spi.AlgorithmFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Reto Habluetzel, 2015
 */
public class MetaMain {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new FileNotFoundException("First parameter must be file that exists!");
        }
        String filename = args[0];

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        // JPPFClient jppfClient = new JPPFClient();
        // JPPFExecutorService jppfExecutorService = new JPPFExecutorService(jppfClient);
        // jppfExecutorService.setBatchSize(100);
        // jppfExecutorService.setBatchTimeout(100);

        Specification spec = new ECTTParser(new BufferedReader(new FileReader(filename))).parse();
        RoomAssigner roomAssigner = new GreedyRoomAssigner(spec);
        Formulation formulation = new UD1Formulation(spec);
        SolutionConverter solutionConverter = new SolutionConverter(formulation);
        Evaluator evaluator = new Evaluator(formulation, solutionConverter);

        int populationSize = 120;
        int offspringSize =  120;
        int k = 1;

        Variation metaVariation = new SBX(0.006, 0.3); // todo undo

        TimetableInitializationFactory cbcttInitializationFactory = new TimetableInitializationFactory(spec, formulation, roomAssigner);
        VariationFactory variationFactory = new VariationFactory(spec, solutionConverter, roomAssigner);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(formulation, evaluator, cbcttInitializationFactory, variationFactory);
        MetaStaticParameters metaStaticParameters = new MetaStaticParameters(cbcttStaticParameters);

        AlgorithmFactory algorithmFactory = new InitializingAlgorithmFactory(metaStaticParameters, metaVariation);

        Executor exec = new Executor();
        exec.withProblemClass(MetaCurriculumBasedTimetabling.class, metaStaticParameters, executorService);
        exec.withAlgorithm(metaStaticParameters.algorithmName());
        exec.usingAlgorithmFactory(algorithmFactory);
        exec.withMaxEvaluations(metaStaticParameters.maxEvaluations());
        exec.withProperty("populationSize", populationSize);
        exec.withProperty("numberOfOffspring", offspringSize);
        exec.withProperty("k", k);
        exec.distributeWith(executorService);

        try {
            exec.run();
        } finally {
            executorService.shutdown();
        }

        // jppfExecutorService.shutdown();
        // jppfClient.close();
    }
}
