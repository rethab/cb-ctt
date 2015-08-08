package ch.rethab.cbctt;

import ch.rethab.cbctt.ea.initializer.TeacherGreedyInitializer;
import ch.rethab.cbctt.moea.InitializingAlgorithmFactory;
import org.moeaframework.Executor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Reto Habluetzel, 2015
 */
public class Main {

    public static void main(String args[]) throws IOException {
        if (args.length != 1 || !new File(args[0]).exists()) {
            throw new FileNotFoundException("First parameter must be file that exists!");
        }

        String filename = args[0];

        new Executor()
            .usingAlgorithmFactory(new InitializingAlgorithmFactory())
            .withProblemClass(CurriculumBasedTimetabling.class, filename, new TeacherGreedyInitializer())
            .withAlgorithm("NSGAIII")
            .withMaxEvaluations(10)
            .run();
    }
}
