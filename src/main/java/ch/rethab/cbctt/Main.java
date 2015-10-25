package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.CbcttRunner;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.ea.CurriculumBasedTimetabling;
import ch.rethab.cbctt.ea.op.CbcttVariation;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.ea.printer.PrettyTextPrinter;
import ch.rethab.cbctt.ea.printer.Printer;
import ch.rethab.cbctt.ea.printer.UdinePrinter;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.moea.TimetableInitializationFactory;
import ch.rethab.cbctt.moea.VariationFactory;
import ch.rethab.cbctt.parser.ECTTParser;
import org.jppf.client.JPPFClient;
import org.jppf.client.concurrent.JPPFExecutorService;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.util.distributed.PublicFutureSolution;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Reto Habluetzel, 2015
 */
public class Main {

    public static void main(String args[]) throws IOException, InterruptedException {
        if (args.length != 1) {
            throw new FileNotFoundException("First parameter must be file that exists!");
        }
        String filename = args[0];

        double mutationProbability = 0.995;
        int populationSize = 250;
        int archiveSize = 100;
        int k = 4;
        int generations = 150;
        Logger.Level progressListenerLevel = Logger.Level.TRACE;

        Logger.configuredLevel = Logger.Level.GIBBER;

        ExecutorService executorService = Executors.newFixedThreadPool(7);


        Specification spec = new ECTTParser(new BufferedReader(new FileReader(filename))).parse();
        RoomAssigner roomAssigner = new GreedyRoomAssigner(spec);
        Formulation formulation = new UD1Formulation(spec);
        SolutionConverter solutionConverter = new SolutionConverter(formulation);
        Evaluator evaluator = new Evaluator(formulation, solutionConverter);
        VariationFactory variationFactory = new VariationFactory(spec, solutionConverter, roomAssigner);

        CbcttVariation courseX = variationFactory.getCrossoverOperator(0, -1);

        List<CbcttVariation> variators = Arrays.asList(
                 courseX, variationFactory.getMutationOperator(0, mutationProbability)
        );

        ParametrizationPhenotype params = new ParametrizationPhenotype(variators, populationSize, archiveSize, k);
        TimetableInitializationFactory timetableInitializationFactory = new TimetableInitializationFactory(spec, formulation, roomAssigner);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(generations, progressListenerLevel,
                formulation, evaluator, timetableInitializationFactory, variationFactory);

        Instrumenter instrumenter = new Instrumenter()
                .withProblemClass(CurriculumBasedTimetabling.class, formulation, evaluator)
                .withFrequency(populationSize)
                .addAllowedPackage("ch.rethab.cbctt.moea")
                .withReferenceSet(new File("src/test/resources/reference-set-comp01"))
                .attachAll();

        CbcttRunner cbcttRunner = new CbcttRunner(cbcttStaticParameters, params);
        try {
            NondominatedPopulation result = cbcttRunner.run(instrumenter);
            Printer udine = new UdinePrinter();
            Printer pretty = new PrettyTextPrinter(spec);
            result.forEach(sol -> {
                System.out.println("Timetable: ");
                System.out.println(udine.print(solutionConverter.fromSolution(sol)));
                System.out.println(pretty.print(solutionConverter.fromSolution(sol)));
            });
        } finally {
            executorService.shutdown();
        }

        Accumulator accumulator = instrumenter.getLastAccumulator();
        int totalNfe = accumulator.size("NFE");
        System.out.println("AdditiveEpsilonIndicator");
        for (int i=0; i<totalNfe; i++) {
            System.out.printf("%d = %4.0f:\n", i, accumulator.get("AdditiveEpsilonIndicator", i));
        }

        System.out.println("RoomCapacity, MinWorkingDays");
        for (int i=0; i<totalNfe; i++) {
            double percentage = 1.0 - ((double)(totalNfe-i)) / (double) totalNfe;
            System.out.printf("%d\n", i);
            List<? extends Solution> approxSet = (List<PublicFutureSolution>) accumulator.get("Approximation Set", i);
            approxSet = reduceDimension(approxSet, 0, 1);
            NondominatedPopulation pop = new NondominatedPopulation(approxSet);
            for (Solution solution : pop) {
                System.out.printf("%s %2.3f\n", showObjectives(solution), percentage);
            }
        }
        System.out.println("RoomCapacity, IsolatedLectures");
        for (int i=0; i<totalNfe; i++) {
            double percentage = 1.0 - ((double)(totalNfe-i)) / (double) totalNfe;
            System.out.printf("%d\n", i);
            List<? extends Solution> approxSet = (List<PublicFutureSolution>) accumulator.get("Approximation Set", i);
            approxSet = reduceDimension(approxSet, 0, 2);
            NondominatedPopulation pop = new NondominatedPopulation(approxSet);
            for (Solution solution : pop) {
                System.out.printf("%s %2.3f\n", showObjectives(solution), percentage);
            }
        }
        System.out.println("MinWorkingDays, IsolatedLectures");
        for (int i=0; i<totalNfe; i++) {
            double percentage = 1.0 - ((double)(totalNfe-i)) / (double) totalNfe;
            System.out.printf("%d\n", i);
            List<? extends Solution> approxSet = (List<PublicFutureSolution>) accumulator.get("Approximation Set", i);
            approxSet = reduceDimension(approxSet, 1, 2);
            NondominatedPopulation pop = new NondominatedPopulation(approxSet);
            for (Solution solution : pop) {
                System.out.printf("%s %2.3f\n", showObjectives(solution), percentage);
            }
        }

    }

    private static List<Solution> reduceDimension(List<? extends Solution> solutions, int obj1, int obj2) {
        return solutions.stream()
                .map(fs -> new Solution(new double[]{fs.getObjective(obj1), fs.getObjective(obj2)}))
                .collect(Collectors.toList());
    }

    public static String showObjectives(Solution s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.getNumberOfObjectives(); i++) {
            sb.append(String.format("%4.0f ", s.getObjective(i)));
        }
        return sb.toString();
    }
}
