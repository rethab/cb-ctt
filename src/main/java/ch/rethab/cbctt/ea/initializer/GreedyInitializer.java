package ch.rethab.cbctt.ea.initializer;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.ea.Meeting;
import ch.rethab.cbctt.ea.Timetable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Reto Habluetzel, 2015
 */
public class GreedyInitializer implements Initializer {

    @Override
    public List<Timetable> initialize(Specification spec, int size) {
        return IntStream.range(0, size).mapToObj(i -> createTimetable(spec)).collect(Collectors.toList());
    }

    private Timetable createTimetable(Specification spec) {

        // randomize input
        Collections.shuffle(spec.getCourses());
        Collections.shuffle(spec.getRooms());

        Timetable t = new Timetable();
        for (Course course : spec.getCourses()) {
            for (int i = 0; i < course.getNumberOfLectures(); i++) {
                scheduleLecture(course, spec, t);
            }
        }
        return t;
    }

    private void scheduleLecture(Course course, Specification spec, Timetable t) {
        for (Room room : spec.getRooms()) {
            // todo randomize days
            for (int day = 0; day < spec.getNumberOfDaysPerWeek(); day++) {
                for (int period = 0; period < spec.getPeriodsPerDay(); period++){
                    boolean teacherAvailable = spec.getUnavailabilityConstraints().checkAvailability(course, day, period);
                    if (t.isFree(room, day, period) && teacherAvailable){
                        t.addMeeting(new Meeting(course, room, day, period));
                        return;
                    }
                }
            }
        }
    }
}
