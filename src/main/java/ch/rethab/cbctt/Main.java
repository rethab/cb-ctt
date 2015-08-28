package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.initializer.Initializer;
import ch.rethab.cbctt.ea.initializer.TeacherGreedyInitializer;
import ch.rethab.cbctt.ea.op.CourseBasedCrossover;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.moea.InitializationFactory;
import ch.rethab.cbctt.moea.InitializingAlgorithmFactory;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.parser.ECTTParser;
import org.jppf.client.JPPFClient;
import org.jppf.client.concurrent.JPPFExecutorService;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.operator.CompoundVariation;

import java.io.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class Main {

    public static void main(String args[]) throws IOException {
        if (args.length != 1) {
            throw new FileNotFoundException("First parameter must be file that exists!");
        }
        String filename = args[0];

        JPPFClient jppfClient = new JPPFClient();
        JPPFExecutorService jppfExecutorService = new JPPFExecutorService(jppfClient);
        jppfExecutorService.setBatchSize(100);
        jppfExecutorService.setBatchTimeout(100);

        Specification spec = new ECTTParser(new BufferedReader(new FileReader(filename))).parse();
        Initializer initializer = new TeacherGreedyInitializer();
        Formulation formulation = new UD1Formulation(spec);
        SolutionConverter solutionConverter = new SolutionConverter(formulation);
        CourseBasedCrossover cbc = new CourseBasedCrossover(solutionConverter, spec);
        CompoundVariation variation = new CompoundVariation(cbc);
        Evaluator evaluator = new Evaluator(formulation, solutionConverter);
        InitializationFactory initializationFactory = new InitializationFactory(formulation, initializer, spec);

        Instrumenter instrumenter = new Instrumenter()
                .withProblemClass(CurriculumBasedTimetabling.class, formulation, evaluator)
                // every 100 evaluations
                .withFrequency(100)
                .withReferenceSet(new File("src/test/resources/reference-set-comp01"))
                .attachAll();

        NondominatedPopulation result = new Executor()
                .usingAlgorithmFactory(new InitializingAlgorithmFactory(initializationFactory, variation))
                .withProblemClass(CurriculumBasedTimetabling.class, formulation, evaluator)
                .withAlgorithm("NSGAIII")
                .withProperty("populationSize", 40)
                .withMaxEvaluations(10000)
                .withInstrumenter(instrumenter)
                .distributeWith(jppfExecutorService)
                .run();

        jppfExecutorService.shutdown();
        jppfClient.close();

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
