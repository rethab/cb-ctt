package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.initializer.Initializer;
import ch.rethab.cbctt.ea.initializer.TeacherGreedyInitializer;
import ch.rethab.cbctt.ea.op.CourseBasedCrossover;
import ch.rethab.cbctt.ea.op.CourseBasedMutation;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.moea.InitializationFactory;
import ch.rethab.cbctt.moea.InitializingAlgorithmFactory;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.parser.ECTTParser;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.operator.CompoundVariation;

import java.io.*;
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

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // JPPFClient jppfClient = new JPPFClient();
        // JPPFExecutorService jppfExecutorService = new JPPFExecutorService(jppfClient);
        // jppfExecutorService.setBatchSize(100);
        // jppfExecutorService.setBatchTimeout(100);

        Specification spec = new ECTTParser(new BufferedReader(new FileReader(filename))).parse();
        RoomAssigner roomAssigner = new GreedyRoomAssigner(spec);
        Initializer initializer = new TeacherGreedyInitializer(spec, roomAssigner);
        Formulation formulation = new UD1Formulation(spec);
        SolutionConverter solutionConverter = new SolutionConverter(formulation);
        CourseBasedCrossover cbc = new CourseBasedCrossover(solutionConverter, roomAssigner, spec);
        CourseBasedMutation cbm = new CourseBasedMutation(solutionConverter, roomAssigner);
        CompoundVariation variation = new CompoundVariation(cbc, cbm);
        Evaluator evaluator = new Evaluator(formulation, solutionConverter);
        InitializationFactory initializationFactory = new InitializationFactory(formulation, initializer);

        Instrumenter instrumenter = new Instrumenter()
                .withProblemClass(CurriculumBasedTimetabling.class, formulation, evaluator)
                // every 100 evaluations
                .withFrequency(100)
                .withReferenceSet(new File("src/test/resources/reference-set-comp01"))
                .attachAll();

        NondominatedPopulation result = new Executor()
                .usingAlgorithmFactory(new InitializingAlgorithmFactory(initializationFactory, variation, executorService))
                .withProblemClass(CurriculumBasedTimetabling.class, formulation, evaluator)
                .withAlgorithm("NSGAIII")
                .withProperty("populationSize", 360)
                .withMaxEvaluations(100000)
                .withInstrumenter(instrumenter)
                .distributeWith(executorService)
                .run();

        executorService.shutdown();

        // jppfExecutorService.shutdown();
        // jppfClient.close();

        Accumulator accumulator = instrumenter.getLastAccumulator();
        for (int i=0; i<accumulator.size("NFE"); i++) {
            System.out.println(accumulator.get("NFE", i) + "\t" + accumulator.get("GenerationalDistance", i));
        }

        for (Solution solution : result) {
            // System.out.println(new UdinePrinter().print(solutionConverter.fromSolution(solution)));
            // System.out.printf("\n");
        }

    }
}
