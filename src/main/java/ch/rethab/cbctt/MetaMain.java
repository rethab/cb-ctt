package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.initializer.Initializer;
import ch.rethab.cbctt.ea.initializer.TeacherGreedyInitializer;
import ch.rethab.cbctt.ea.op.*;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.meta.MetaCurriculumBasedTimetabling;
import ch.rethab.cbctt.meta.MetaStaticParameters;
import ch.rethab.cbctt.moea.InitializationFactory;
import ch.rethab.cbctt.moea.InitializingAlgorithmFactory;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.parser.ECTTParser;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.spi.AlgorithmFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

        // TODO SPEA2 parameters
        // int populationSize = properties.getInt("populationSize", 100);
        // int numberOfOffspring =  properties.getInt("numberOfOffspring", populationSize);
        // int k = properties.getInt("kNearestNeighbour", 1);

        List<Variation> crossovers = Arrays.asList(
                new CourseBasedCrossover(solutionConverter, roomAssigner, spec),
                new CurriculumBasedCrossover(solutionConverter, roomAssigner, spec),
                // todo make sectorSize parameterizable, potentially use these operators with factories
                new SectorBasedCrossover(solutionConverter, roomAssigner, spec, 10)
        );
        List<Variation> mutators = Collections.singletonList(
                new CourseBasedMutation(solutionConverter, roomAssigner, spec)
        );

        Initializer initializer = new TeacherGreedyInitializer(spec, roomAssigner);
        InitializationFactory initializationFactory = new InitializationFactory(formulation, initializer);
        CompoundVariation variation = new CompoundVariation(mutators.get(0), crossovers.get(0), crossovers.get(1), crossovers.get(2));
        AlgorithmFactory algorithmFactory = new InitializingAlgorithmFactory(initializationFactory, variation, executorService);

        StaticParameters metaStaticParameters = new MetaStaticParameters(
                crossovers, mutators, algorithmFactory, formulation, evaluator
        );

        NondominatedPopulation result = new Executor()
                .withProblemClass(MetaCurriculumBasedTimetabling.class, metaStaticParameters)
                .withAlgorithm(MetaStaticParameters.META_ALGORITHM_NAME)
                .withProperty("populationSize", 20)
                .withMaxEvaluations(100000)
                // .withInstrumenter(instrumenter)
                .distributeWith(executorService)
                .run();

        executorService.shutdown();
        // jppfExecutorService.shutdown();
        // jppfClient.close();
    }
}
