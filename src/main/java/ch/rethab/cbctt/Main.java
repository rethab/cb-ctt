package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.CbcttRunner;
import ch.rethab.cbctt.ea.CbcttStaticParameters;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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
        int populationSize = 100;
        int archiveSize = 30;
        int k = 1;

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
        VariationFactory variationFactory = new VariationFactory(spec, solutionConverter, roomAssigner);

        List<CbcttVariation> crossovers = Arrays.asList(variationFactory.getCrossoverOperator(0, sectorSize), variationFactory.getCrossoverOperator(1, sectorSize), variationFactory.getCrossoverOperator(2, sectorSize));
        List<CbcttVariation> mutators = Collections.singletonList(variationFactory.getMutationOperator(0, mutationProbability));
        ParametrizationPhenotype params = new ParametrizationPhenotype(crossovers, mutators, populationSize, archiveSize, k);
        TimetableInitializationFactory timetableInitializationFactory = new TimetableInitializationFactory(spec, formulation, roomAssigner);
        CbcttStaticParameters cbcttStaticParameters = new CbcttStaticParameters(formulation, evaluator, timetableInitializationFactory, variationFactory);

        CbcttRunner cbcttRunner = new CbcttRunner(executorService, cbcttStaticParameters, params);
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
