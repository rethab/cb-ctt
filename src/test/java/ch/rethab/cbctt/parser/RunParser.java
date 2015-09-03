package ch.rethab.cbctt.parser;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.ea.initializer.Initializer;
import ch.rethab.cbctt.ea.initializer.TeacherGreedyInitializer;
import ch.rethab.cbctt.formulation.Formulation;
import ch.rethab.cbctt.formulation.UD1Formulation;
import ch.rethab.cbctt.formulation.constraint.Constraint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Reto Habluetzel, 2015
 */
public class RunParser {

    public static void main(String []args) throws IOException {
        for (int i = 1; i < 1; i++) {
            System.out.println(i);
        }

        if (args.length != 1) {
            throw new IllegalArgumentException("Parameter 0 must be filename");
        }
        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        ECTTParser parser = new ECTTParser(br);
        Specification spec = parser.parse();
        System.out.println("Parsed");
        Formulation v = new UD1Formulation(spec);
        Initializer initializer = new TeacherGreedyInitializer();
        List<Timetable> ts = initializer.initialize(spec, 1);
        ts.forEach(t -> {
            boolean feasible = true;
            for (Constraint c : v.getConstraints()) {
                if (c.violations(t) != 0) {
                    feasible = false;
                }
            }
            System.out.printf("Valid? %s", feasible);
        });
    }

}
