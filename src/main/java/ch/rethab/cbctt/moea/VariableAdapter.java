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
        Timetable copy = new Timetable();
        this.timetable.getMeetings().forEach(copy::addMeeting);
        return new VariableAdapter(copy);
    }

    public Timetable getTimetable() {
        return this.timetable;
    }
}
