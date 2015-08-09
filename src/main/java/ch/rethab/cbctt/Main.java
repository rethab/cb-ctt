package ch.rethab.cbctt;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.initializer.Initializer;
import ch.rethab.cbctt.ea.initializer.TeacherGreedyInitializer;
import ch.rethab.cbctt.ea.op.DummyVariation;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.moea.InitializingAlgorithmFactory;
import ch.rethab.cbctt.moea.SolutionConverter;
import ch.rethab.cbctt.parser.ECTTParser;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
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

        Specification spec = new ECTTParser(new BufferedReader(new FileReader(filename))).parse();
        Initializer initializer = new TeacherGreedyInitializer();
        Formulation formulation = new UD1Formulation(spec);
        SolutionConverter solutionConverter = new SolutionConverter(formulation);
        CompoundVariation variation = new CompoundVariation(new DummyVariation()); // todo add mutation here

        NondominatedPopulation result = new Executor()
                .usingAlgorithmFactory(new InitializingAlgorithmFactory())
                .withProblemClass(CurriculumBasedTimetabling.class, spec, initializer, formulation, solutionConverter, variation)
                .withAlgorithm("NSGAIII")
                .withProperty("populationSize", 20)
                .withMaxEvaluations(100)
                .run();

        System.out.println(result.size());
    }
}
