package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import org.moeaframework.core.Variable;

/**
 * @author Reto Habluetzel, 2015
 */
public class VariableAdapter implements Variable {

    private TimetableWithRooms timetable;

    public VariableAdapter(TimetableWithRooms timetable) {
        this.timetable = timetable;
    }

    @Override
    public Variable copy() {
        // copying the timetable is not required since it is unmodifiable
        return new VariableAdapter(timetable);
    }

    public TimetableWithRooms getTimetable() {
        return this.timetable;
    }
}
