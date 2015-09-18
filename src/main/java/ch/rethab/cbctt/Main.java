package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.CbcttRunner;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.ea.initializer.Initializer;
import ch.rethab.cbctt.ea.initializer.TeacherGreedyInitializer;
import ch.rethab.cbctt.ea.op.*;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import ch.rethab.cbctt.moea.InitializationFactory;
import ch.rethab.cbctt.moea.InitializingAlgorithmFactory;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.parser.ECTTParser;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.spi.AlgorithmFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Reto Habluetzel, 2015
 */
public class Main {

    public static void main(String args[]) throws IOException, InterruptedException {
        if (args.length != 1) {
            throw new FileNotFoundException("First parameter must be file that exists!");
        }
        String filename = args[0];

        double crossoverProbability = 0.5;
        double mutationProbability = 0.2;
        int populationSize = 100;
        int archiveSize = 30;

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        // JPPFClient jppfClient = new JPPFClient();
        // JPPFExecutorService jppfExecutorService = new JPPFExecutorService(jppfClient);
        // jppfExecutorService.setBatchSize(100);
        // jppfExecutorService.setBatchTimeout(100);


        Specification spec = new ECTTParser(new BufferedReader(new FileReader(filename))).parse();
        RoomAssigner roomAssigner = new GreedyRoomAssigner(spec);
        Formulation formulation = new UD1Formulation(spec);
        SolutionConverter solutionConverter = new SolutionConverter(formulation);
        Evaluator evaluator = new Evaluator(formulation, solutionConverter);

        List<Variation> crossovers = Arrays.asList(
            new CourseBasedCrossover(solutionConverter, roomAssigner, spec),
            new CurriculumBasedCrossover(solutionConverter, roomAssigner, spec),
            new SectorBasedCrossover(solutionConverter, roomAssigner, spec, 10)
        );
        List<Variation> mutators = Collections.singletonList(
            new CourseBasedMutation(solutionConverter, roomAssigner, spec)
        );

        ParametrizationPhenotype params = new ParametrizationPhenotype(
                crossovers, crossoverProbability,
                mutators, mutationProbability,
                populationSize, archiveSize);

        Initializer initializer = new TeacherGreedyInitializer(spec, roomAssigner);
        InitializationFactory initializationFactory = new InitializationFactory(formulation, initializer);
        // crossover operators must be applied first, only then the mutation is applied to the offsprings
        // individually. see documentation of CompoundVariation
        CompoundVariation variation = new CompoundVariation(/*crossovers.get(0), crossovers.get(1), crossovers.get(2),*/ mutators.get(0));
        AlgorithmFactory algorithmFactory = new InitializingAlgorithmFactory(initializationFactory, variation, executorService);

        CbcttStaticParameters staticParameters = new CbcttStaticParameters(crossovers, mutators, algorithmFactory, formulation, evaluator);

        CbcttRunner cbcttRunner = new CbcttRunner(executorService, staticParameters, params);
        try {
            cbcttRunner.run();
        } finally {
            executorService.shutdown();
        }

//        Instrumenter instrumenter = new Instrumenter()
//                .withProblemClass(CurriculumBasedTimetabling.class, formulation, evaluator)
//                // every 100 evaluations
//                .withFrequency(100)
//                .withReferenceSet(new File("src/test/resources/reference-set-comp01"))
//                .attachAll();



        // jppfExecutorService.shutdown();
        // jppfClient.close();

//        Accumulator accumulator = instrumenter.getLastAccumulator();
//        for (int i=0; i<accumulator.size("NFE"); i++) {
//            System.out.println(accumulator.get("NFE", i) + "\t" + accumulator.get("GenerationalDistance", i));
//        }

    }
}
