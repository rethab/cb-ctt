package ch.rethab.cbctt.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Reto Habluetzel, 2015
 */
public class UnavailabilityConstraints {

    private Map<Course, boolean[][]> unavailabilities = new HashMap<>();

    private final int daysPerWeek;
    private final int periodsPerDay;

    public UnavailabilityConstraints(int daysPerWeeks, int periodsPerDay) {
        this.daysPerWeek = daysPerWeeks;
        this.periodsPerDay = periodsPerDay;
    }

    public void addUnavailability(Course c, int day, int period) {
        if (!unavailabilities.containsKey(c)) {
            unavailabilities.put(c, new boolean[daysPerWeek][periodsPerDay]);
        }
        boolean[][] unavailability = unavailabilities.get(c);
        unavailability[day][period] = true;
    }

    public boolean checkAvailability(Course c, int day, int period) {
        boolean[][] unavailability = unavailabilities.get(c);
        if (unavailability == null) {
            return true;
        } else {
            return !unavailability[day][period];
        }
    }

    public int countByTeacher(String teacher) {
        int counter = 0;
        for (Map.Entry<Course, boolean[][]> courseEntry : unavailabilities.entrySet()) {
            Course c = courseEntry.getKey();
            if (teacher.equals(c.getTeacher())) {
                boolean[][] unavailabilities = courseEntry.getValue();
                for (int day = 0; day < unavailabilities.length; day++) {
                    for (int period = 0; period < unavailabilities[day].length; period++) {
                        if (unavailabilities[day][period]) {
                            counter++;
                        }
                    }
                }
            }
        }
        return counter;
    }
}
