package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.CbcttRunner;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
import ch.rethab.cbctt.ea.CurriculumBasedTimetabling;
import ch.rethab.cbctt.ea.op.CbcttVariation;
import ch.rethab.cbctt.ea.op.Evaluator;
import ch.rethab.cbctt.ea.phenotype.GreedyRoomAssigner;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.moea.TimetableInitializationFactory;
import ch.rethab.cbctt.moea.VariationFactory;
import ch.rethab.cbctt.parser.ECTTParser;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;

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

        double mutationProbability = 0.2;
        int sectorSize = 3;
        int populationSize = 50;
        int archiveSize = 50; // if archive size is too small, we spend a lot of time truncating
        int k = 1;

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

        List<CbcttVariation> variators = Arrays.asList(
                //variationFactory.getCrossoverOperator(0, sectorSize),
                //variationFactory.getCrossoverOperator(1, sectorSize),
                variationFactory.getCrossoverOperator(2, sectorSize),
                variationFactory.getMutationOperator(0, mutationProbability)
        );

        ParametrizationPhenotype params = new ParametrizationPhenotype(variators, populationSize, archiveSize, k);
        TimetableInitializationFactory timetableInitializationFactory = new TimetableInitializationFactory(spec, formulation, roomAssigner);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(formulation, evaluator, timetableInitializationFactory, variationFactory);

        Instrumenter instrumenter = new Instrumenter()
                .withProblemClass(CurriculumBasedTimetabling.class, formulation, evaluator)
                .withFrequency(100) // every 100 evaluations
                .withReferenceSet(new File("src/test/resources/reference-set-comp01"))
                .attachAll();

        CbcttRunner cbcttRunner = new CbcttRunner(executorService, cbcttStaticParameters, params);
        try {
            cbcttRunner.run(instrumenter);
        } finally {
            executorService.shutdown();
        }


        // jppfExecutorService.shutdown();
        // jppfClient.close();

        Accumulator accumulator = instrumenter.getLastAccumulator();
        for (int i=0; i<accumulator.size("NFE"); i++) {
            System.out.printf("%d: %s\n", i, accumulator.get("AdditiveEpsilonIndicator", i));
        }

    }
}
