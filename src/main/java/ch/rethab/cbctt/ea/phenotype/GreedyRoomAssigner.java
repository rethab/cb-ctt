package ch.rethab.cbctt.ea.phenotype;

import ch.rethab.cbctt.domain.Specification;

import java.util.List;
import java.util.Set;

public class GreedyRoomAssigner implements RoomAssigner {

    private final Specification spec;

    public GreedyRoomAssigner(Specification spec) {
        this.spec = spec;
    }

    @Override
    public TimetableWithRooms assignRooms(Timetable t) {
        System.out.println("GreedyRoomAssigner.assignRooms: ENTRY");
        TimetableWithRooms.Builder builder = TimetableWithRooms.Builder.newBuilder(spec);
        PeriodRoomAssignments[] periodRoomAssignments = t.getPeriodRoomAssignmentses();

        for (int slotIdx = 0; slotIdx < periodRoomAssignments.length; slotIdx++) {
            int day = Math.floorDiv(slotIdx, spec.getPeriodsPerDay());
            int period = slotIdx % spec.getPeriodsPerDay();
            PeriodRoomAssignments periodRoomAssignment = periodRoomAssignments[slotIdx];
            List<PeriodRoomAssignments.CourseWithRoom> courses = periodRoomAssignment.assignRooms();
            courses.forEach(cwr -> builder.addMeeting(cwr.course, cwr.room, day, period));
        }

        System.out.println("GreedyRoomAssigner.assignRooms: EXIT");
        return builder.build();
    }

}
