package ch.rethab.cbctt.ea.phenotype;

import ch.rethab.cbctt.domain.Course;
import ch.rethab.cbctt.domain.Room;
import ch.rethab.cbctt.domain.Specification;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the room assignments during timetable construction. This is
 * used to efficiently checking whether a room can be scheduled at a particular
 * period without violating any of the constraints taking into account the other
 * courses that are to be scheduled on the same period.
 */
public class PeriodRoomAssignments {

    private final Specification spec;

    private final RoomViolations[][] roomAssignments;

    /* everything is an array, so we need to know at which index each room is stored */
    private final Map<String, Integer> roomIdxMap = new HashMap<>();

    /* the course array is built gradually. this is the index where the next is to be
     * scheduled. Note that while checking if a course is suitable to be scheduled
     * at a certain period, a course may be added without incrementing the index after
     * is proofs to be infeasible. This means the index is used to track where in
     * the array courses may be put rather than nulls.
     */
    private int nextCourseIdx = 0;

    /**
     * Actual entries of the room assignments. This 'tuple' is required, since
     * we need access to the room and we loos the index in the procedure.
     */
    private class RoomViolations {
        /* number of violations a certain courses would have in a certain room.
         * the smaller the better. 0 means perfect fit */
        final int violations;
        final Room room;
        final Course course;
        public RoomViolations(Room room, Course c, int violations) {
            this.room = room;
            this.course = c;
            this.violations = violations;
        }
    }

    public PeriodRoomAssignments(Specification spec) {
        this.spec = spec;
        Set<String> roomIds = spec.getRooms().stream().map(Room::getId).collect(Collectors.toSet());
        // +1 because the last one counts the number of suitable rooms per course
        roomAssignments = new RoomViolations[roomIds.size()][roomIds.size()+1];
    }

    public Set<CourseWithRoom> schedule() {
        try {
            return assignAll(roomAssignments, nextCourseIdx-1);
        } catch (InfeasibilityException e) {
            throw new IllegalStateException("Dude, you code is fucked up");
        }
    }

    public boolean tryToSchedule(Course c) {
        /*
         * unless we actually increase the instance
         * variable index, the next course will just
         * override this one.
         */
        int idx = nextCourseIdx;
        roomAssignments[idx] = fillRooms(c);

        RoomViolations[][] copy = deepCopy(roomAssignments);

        try {
            assignAll(copy, idx);
            nextCourseIdx++;
            return true;
        } catch (InfeasibilityException e) {
            return false;
        }
    }

    private RoomViolations[][] deepCopy(RoomViolations[][] roomAssignments) {
        RoomViolations[][] copy = new RoomViolations[roomAssignments.length][roomAssignments[0].length];
        for (int i = 0; i < roomAssignments.length; i++) {
            System.arraycopy(roomAssignments[i], 0, copy[i], 0, roomAssignments[i].length);
        }
        return copy;
    }

    /**
     * Tries to assign all courses to room and returns true if that is
     * possible without violating any of the constraints.
     *
     * It does this by first searching the last column of the
     * roomAssignments, which is the number of suitable rooms
     * for a course. This way, the course with the most restricted
     * constraints is picket and scheduled first.
     *
     * Then that course is removed from the list and the room
     * where it is scheduled is marked as constraint for all other
     * courses. As long as a room can be found for a course, the
     * procedure is repeated until all courses are assigned a room.
     *
     * If one course cannot be assigned to a room, the method returns
     * false.
     *
     * @param lastIdx the last index of the array that is still to be
     *                considered for feasibility
     */
    private Set<CourseWithRoom> assignAll(RoomViolations[][] roomAssignments, int lastIdx) throws InfeasibilityException{
        Set<CourseWithRoom> assignments = new LinkedHashSet<>();
        Optional<RoomViolations[]> mbRoomsForCourse = findMostConstrainedCourse(roomAssignments, lastIdx);
        while (mbRoomsForCourse.isPresent()) {
            RoomViolations[] roomsForCourse = mbRoomsForCourse.get();

            Arrays.sort(roomsForCourse, violationsComparator());

            RoomViolations rv = roomsForCourse[0];
            if (rv == null) {
                throw new InfeasibilityException();
            }
            int roomIdx = roomIdxMap.get(rv.room.getId());

            Arrays.stream(roomAssignments, 0, lastIdx + 1).forEach(crViolations -> {
                crViolations[roomIdx] = null;
                // set last to null as an indicator that this course is scheduled
                crViolations[spec.getRooms().size()] = null;
            });

            assignments.add(new CourseWithRoom(rv.course, rv.room));

            mbRoomsForCourse = findMostConstrainedCourse(roomAssignments, lastIdx);
        }
        return assignments;
    }

    private Optional<RoomViolations[]> findMostConstrainedCourse(RoomViolations[][] roomAssignments, int lastIdx) {

        // +1 --> exclusive index
        return Arrays.stream(roomAssignments, 0, lastIdx+1)

            // find course that is hardest to schedule (least amount of suitable rooms)
            .sorted(Comparator.comparingInt(roomViolations -> roomViolations[spec.getRooms().size()].violations))

            // course already assigned a room
            .filter(crViolations -> crViolations[spec.getRooms().size()] != null)
            .findFirst();
    }

    /**
     * Comparator such that the most preferable number of violations
     * would be first in an ascending ordered list. I.e. perfect would
     * be 0 violations. After that, negative violations are preferred,
     * with the closer to 0 the better (negative means the room is too big).
     * After that, we take the positive violations (room is too small) in
     * ascending order.
     *
     * E.g: 0, -1, -2, 1, 2
     *
     * For that, negative numbers are transformed like the following so they
     * will be between 0 and 1:
     *
     *      1
     * 1 - ----
     *     |i|
     *
     * Null entries indicate a constraint. By assigning them positive
     * infinity, we can make sure they get last in the sort.
     *
     */
    private Comparator<RoomViolations> violationsComparator() {
        return Comparator.comparingDouble(rv -> {
            if (rv == null) {
                return Double.POSITIVE_INFINITY;
            } else if (rv.violations < 0) {
                return 1 - (1/(Math.abs(rv.violations)));
            } else {
                return rv.violations;
            }
        });
    }

    /**
     * Creates one row for a course with indices per room.
     * Each cell holds the number of violations, i.e. the
     * amount of students, by which the room capacity is
     * exceeded.
     *
     * The last column hold the number of suitable
     * rooms per course, ie. the number of rooms that are
     * not constrained by room constraints from the spec.
     * This number will also be used during the actual
     * assignment, where it is decreased every time some
     * other course is scheduled in a room that would also
     * have been suitable for a particular course. Therefore,
     * this number may be used as an indicator for the 'most
     * constrained' course, ie. which course will be hardest
     * to a assign to a room.
     */
    private RoomViolations[] fillRooms(Course c) {
        RoomViolations[] roomViolations = new RoomViolations[spec.getRooms().size() + 1];
        int suitable = 0;
        int i = 0;
        for (Room r : spec.getRooms()) {

            roomIdxMap.put(r.getId(), i);

            if (spec.getRoomConstraints().isUnsuitable(c, r)) {
                roomViolations[i] = null;
            } else {
                int violations = c.getNumberOfStudents() - r.getCapacity();
                roomViolations[i] = new RoomViolations(r, c, violations);
                suitable++;
            }
            i++;
        }
        roomViolations[spec.getRooms().size()] = new RoomViolations(null, null, suitable);
        return roomViolations;
    }

    private static class InfeasibilityException extends Exception { }

    public class CourseWithRoom {
        final Course course;
        final Room room;

        private CourseWithRoom(Course course, Room room) {
            this.course = course;
            this.room = room;
        }
    }
}

