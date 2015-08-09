package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.ea.Timetable;
import org.moeaframework.core.Variable;

/**
 * @author Reto Habluetzel, 2015
 */
public class VariableAdapter implements Variable {

    private Timetable timetable;

    public VariableAdapter(Timetable timetable) {
        this.timetable = timetable;
    }

    @Override
    public Variable copy() {
        return new VariableAdapter(timetable.copy());
    }

    public Timetable getTimetable() {
        return this.timetable;
    }
}
