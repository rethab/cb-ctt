package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.phenotype.MeetingWithRoom;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import ch.rethab.cbctt.moea.SolutionConverter;

import java.util.Set;

/**
 * Inspired by 'New Crossover Algorithms for Timetabling with
 * Evolutionary Algorithms' (Lewis and Paechter), this crossover
 * operator takes one course from the second parent and tries to
 * assignRooms it at the same periods in a copy of the first parent.
 * That copy is the first child. The course is selected at random.
 * The second child is constructed by reversing the roles.
 *
 * @author Reto Habluetzel, 2015
 */
public final class CourseBasedCrossover extends AbstractLessonBasedCrossover {

    public CourseBasedCrossover(SolutionConverter solutionConverter, RoomAssigner roomAssigner, Specification spec) {
        super(solutionConverter, spec, roomAssigner);
    }

    @Override
    protected Set<MeetingWithRoom> getMeetingsFromParent(TimetableWithRooms parent) {
        Course course = getRandomCourse(parent);
        return parent.getMeetingsByCourse(course);
    }

    private Course getRandomCourse(TimetableWithRooms t) {
        MeetingWithRoom[] meetings = t.getMeetings().toArray(new MeetingWithRoom[t.getMeetings().size()]);
        int idx = rand.nextInt(meetings.length);
        return meetings[idx].getCourse();
    }

}
