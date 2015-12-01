package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.CbcttRunner;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.ea.CurriculumBasedTimetabling;
import ch.rethab.cbctt.ea.op.CbcttVariation;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import ch.rethab.cbctt.ea.printer.PrettyTextPrinter;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.moea.TimetableInitializationFactory;
import ch.rethab.cbctt.moea.VariationFactory;
import ch.rethab.cbctt.parser.ECTTParser;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;

import java.io.*;
import java.util.Arrays;
import java.util.List;
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

        double mutationProbability = 0.95;
        // int sectorSize = 3;
        int populationSize = 30;
        int archiveSize = 30; // if archive size is too small, we spend a lot of time truncating
        int k = 3;
        int generations = 10;
        Logger.Level progressListenerLevel = Logger.Level.TRACE;

        Logger.configuredLevel = Logger.Level.GIBBER;

        ExecutorService executorService = Executors.newFixedThreadPool(7);
        // JPPFClient jppfClient = new JPPFClient();
        // JPPFExecutorService jppfExecutorService = new JPPFExecutorService(jppfClient);
        // jppfExecutorService.setBatchSize(100);
        // jppfExecutorService.setBatchTimeout(100);


        Specification spec = new ECTTParser(new BufferedReader(new FileReader(filename))).parse();
        RoomAssigner roomAssigner = new GreedyRoomAssigner(spec);
        Formulation formulation = new UD1Formulation(spec);
        SolutionConverter solutionConverter = new SolutionConverter(formulation);
        Evaluator evaluator = new Evaluator(formulation, solutionConverter);
        VariationFactory variationFactory = new VariationFactory(spec, solutionConverter, roomAssigner);

        CbcttVariation courseX = variationFactory.getCrossoverOperator(0, -1);
        // CbcttVariation currX = variationFactory.getCrossoverOperator(1, -1);
        // CbcttVariation sectorX = variationFactory.getCrossoverOperator(2, sectorSize);

        List<CbcttVariation> variators = Arrays.asList(
                courseX, /* currX, sectorX, */ variationFactory.getMutationOperator(0, mutationProbability)
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
        NondominatedPopulation run = null;
        try {
            run = cbcttRunner.run(instrumenter);
        } finally {
            executorService.shutdown();
        }

        PrettyTextPrinter printer = new PrettyTextPrinter(spec);
        run.forEach(sol -> printer.print(solutionConverter.fromSolution(sol)));

        // jppfExecutorService.shutdown();
        // jppfClient.close();


    }
}
