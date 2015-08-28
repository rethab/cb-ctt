package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.ea.Timetable;
import ch.rethab.cbctt.formulation.Formulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.util.distributed.PublicFutureSolution;

import java.io.Serializable;

/**
 *
 * Converts a Timetable from the ct-ctt domain to a solution as used in
 * the MOEA fraemwork.
 * This essentially does the encoding / genotype conversion.
 *
 * @author Reto Habluetzel, 2015
 */
public class SolutionConverter implements Serializable {

    private final Formulation formulation;

    public SolutionConverter(Formulation formulation) {
        this.formulation = formulation;
    }

    public Solution toSolution(Timetable t) {
        Solution s = new Solution(
            this.formulation.getNumberOfVariables(),
            this.formulation.getNumberOfObjectives(),
            this.formulation.getNumberOfConstraints()
        );

        s.setVariable(0, new VariableAdapter(t));

        return new PublicFutureSolution(s);
    }

    public Timetable fromSolution(Solution solution) {
        Variable v = solution.getVariable(0);
        return ((VariableAdapter)v).getTimetable();
    }
}
