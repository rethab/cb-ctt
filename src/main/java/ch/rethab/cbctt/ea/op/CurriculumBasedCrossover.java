package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.phenotype.MeetingWithRoom;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import ch.rethab.cbctt.moea.SolutionConverter;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Inspired by 'New Crossover Algorithms for Timetabling with
 * Evolutionary Algorithms' (Lewis and Paechter), this crossover
 * operator takes one curriculum from the second parent and tries to
 * replicate all lessons of all courses at the same periods in a copy
 * of the first parent. That copy is the first child. The curriculum
 * is selected at random. The second child is constructed by
 * reversing the roles.
 *
 * @author Reto Habluetzel, 2015
 */
public final class CurriculumBasedCrossover extends AbstractLessonBasedCrossover {

    public CurriculumBasedCrossover(Specification spec, SolutionConverter solutionConverter, RoomAssigner roomAssigner) {
        super(spec, solutionConverter, roomAssigner);
    }

    @Override
    public String name() {
        return "curriculumBasedCrossover";
    }

    @Override
    protected Set<MeetingWithRoom> getMeetingsFromParent(TimetableWithRooms parent) {
        String randomCurriculum = getRandomCurriculum();
        return parent.getCurriculumTimetables().get(randomCurriculum).getAll().collect(Collectors.toSet());
    }

    private String getRandomCurriculum() {
        int nCurricula = spec.getCurricula().size();
        int idx = rand.nextInt(nCurricula);
        return spec.getCurricula().get(idx).getId();
    }

}
