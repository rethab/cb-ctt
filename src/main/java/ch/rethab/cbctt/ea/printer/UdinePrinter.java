package ch.rethab.cbctt.ea.printer;

import ch.rethab.cbctt.ea.Timetable;

/**
 * Prints the timetable in the format that is accepted by the Udine validator
 *
 * http://satt.diegm.uniud.it/ctt/index.php?page=valid
 *
 * @author Reto Habluetzel, 2015
 */
public class UdinePrinter implements Printer {

    @Override
    public String print(Timetable t) {
        StringBuilder sb = new StringBuilder();
        t.getMeetings().forEach(m -> {
            sb.append(m.getCourse().getId());
            sb.append(" ");
            sb.append(m.getRoom().getId());
            sb.append(" ");
            sb.append(m.getDay());
            sb.append(" ");
            sb.append(m.getPeriod());
            sb.append("\n");
        });
        return sb.toString();
    }
}
