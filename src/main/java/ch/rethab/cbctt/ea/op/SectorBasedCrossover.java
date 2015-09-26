package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.domain.Specification;
import ch.rethab.cbctt.ea.phenotype.MeetingWithRoom;
import ch.rethab.cbctt.ea.phenotype.RoomAssigner;
import ch.rethab.cbctt.ea.phenotype.TimetableWithRooms;
import ch.rethab.cbctt.moea.SolutionConverter;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Inspired by 'New Crossover Algorithms for Timetabling with
 * Evolutionary Algorithms' (Lewis and Paechter), this crossover
 * operator takes a sector from the second parent and tries to
 * replicate all lessons at the same periods in a copy
 * of the first parent. That copy is the first child. The start
 * of the sector is selected at random, while the size may
 * be parametrized. The sector does not necessarily need to
 * be within one day but may start in the evening of the
 * first day and end in at noon on the second. A sector
 * is chosen from one curriculum, which is also selected
 * randomly. The second child is constructed by reversing
 * the roles.
 *
 * @author Reto Habluetzel, 2015
 */
public final class SectorBasedCrossover extends AbstractLessonBasedCrossover {

    /** number of lessons in one sector */
    private final int sectorSize;

    public SectorBasedCrossover(Specification spec, SolutionConverter solutionConverter, RoomAssigner roomAssigner, int sectorSize) {
        super(spec, solutionConverter, roomAssigner);
        this.sectorSize = sectorSize;
    }

    @Override
    public String name() {
        return String.format("SectorBasedCrossover(sectorSize=%d)", sectorSize);
    }

    public int getSectorSize() {
        return sectorSize;
    }

    @Override
    protected Set<MeetingWithRoom> getMeetingsFromParent(TimetableWithRooms parent) {
        List<MeetingWithRoom> meetings = getSortedMeetings(parent);
        int maxStartIdx = meetings.size() - sectorSize;
        int startIdx = maxStartIdx <= 0 ? 0 : rand.nextInt(maxStartIdx);
        int endIdx = startIdx + sectorSize;
        if (endIdx > meetings.size()) {
            endIdx = meetings.size();
        }

        return meetings.subList(startIdx, endIdx).stream().collect(Collectors.toSet());
    }

    private List<MeetingWithRoom> getSortedMeetings(TimetableWithRooms parent) {
        // need to sort so we can create proper sectors
        int nCurricula = spec.getCurricula().size();
        int idx = rand.nextInt(nCurricula);
        String currId = spec.getCurricula().get(idx).getId();
        return parent.getCurriculumTimetables().get(currId).getAll()
                .sorted(Comparator.comparing(mwr -> toSlotIdx(mwr.getDay(), mwr.getPeriod())))
                .collect(Collectors.toList());
    }

    private int toSlotIdx(int day, int period) {
        return spec.getPeriodsPerDay() * day + period;
    }
}
