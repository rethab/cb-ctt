package ch.rethab.cbctt.parser;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.ea.initializer.Initializer;
import ch.rethab.cbctt.ea.initializer.TeacherGreedyInitializer;
import ch.rethab.cbctt.validator.UD1Validator;
import ch.rethab.cbctt.validator.Validator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

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
        Validator v = new UD1Validator(spec);
        Initializer initializer = new TeacherGreedyInitializer();
        List<Timetable> ts = initializer.initialize(spec, 1);
        ts.forEach(t -> System.out.printf("Valid? %s", v.isFeasible(t)));
    }

}
