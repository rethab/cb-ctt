package ch.rethab.cbctt.ea.phenotype;

import blogspot.software_and_algorithms.stern_library.optimization.HungarianAlgorithm;
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

    /* everything is an array, so we need to know at which index each room is stored */
    private final Map<String, Integer> courseIdxMap = new HashMap<>();

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

        public RoomViolations fixedViolations(int newViolations) {
            return new RoomViolations(room, course, newViolations);
        }

        public RoomViolations subtract(int x) {
            return new RoomViolations(room, course, violations - x);
        }
    }

    public PeriodRoomAssignments(Specification spec) {
        this.spec = spec;
        Set<String> roomIds = spec.getRooms().stream().map(Room::getId).collect(Collectors.toSet());
        roomAssignments = new RoomViolations[roomIds.size()][roomIds.size()];
    }

    public List<CourseWithRoom> assignRooms() {

        if (nextCourseIdx == 0) {
            // no courses in this period. check required due to -1 below
            return Collections.emptyList();
        }

        RoomViolations[][] copy = null;
        try {
            copy = deepCopy(roomAssignments);
            return assignAll(roomAssignments, nextCourseIdx-1, true);
        } catch (InfeasibilityException e) {
            for (int cid = 0; cid < copy.length; cid++) {
                for (int rid = 0; rid < copy[cid].length; rid++) {
                    RoomViolations rv = copy[cid][rid];
                    System.out.printf("[%s, %s, %d] ", rv == null || rv.course == null ? null : rv.course.getId(),
                                                       rv == null || rv.room == null   ? null : rv.room.getId(),
                                                       rv == null                      ? null : rv.violations);
                }
                System.out.println();
            }
            throw new IllegalStateException("Dude, you code is fucked up");
        }
    }

    public boolean add(Course c) {

        // no more free rooms in this period
        if (nextCourseIdx >= this.spec.getRooms().size()) {
            return false;
        }

        /*
         * unless we actually increase the instance
         * variable index, the next course will just
         * override this one.
         *
         * lastIdx is inclusive.
         */
        int courseIdx = nextCourseIdx;
        roomAssignments[courseIdx] = fillRooms(c);

        // copy since the array values are updated during assignment (occupied rooms are set to null)
        RoomViolations[][] copy = deepCopy(roomAssignments);

        // the idx is required during assignment
        if (courseIdxMap.put(c.getId(), courseIdx) != null) {
            throw new Timetable.InfeasibilityException("Same course in same period. Makes no sense");
        }

        try {
            assignAll(copy, courseIdx, false);
            nextCourseIdx++;
            return true;
        } catch (InfeasibilityException e) {
            // it only had to be in there for the assignment
            courseIdxMap.remove(c.getId());

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
     * @param inclusiveLastIdx the last index of the array that is still to be
     *                         considered for feasibility
     * @param real true if we are assigning for real. false if we're just
     *             checking if the course would fit. purely for optimization
     */
    private List<CourseWithRoom> assignAll(RoomViolations[][] roomAssignments, int inclusiveLastIdx, boolean real) throws InfeasibilityException{
        // todo this algorithm muss not be used for checking if the assignment
        //      is feasible, since it always will be. the constraints would
        //      also be selected!

        // make sure nothing is negative, set constraints to high numbers
        calibrate(roomAssignments, inclusiveLastIdx);

        // hungarian algorithm
        // source: slide 13 of http://www.math.harvard.edu/archive/20_spring_05/handouts/assignment_overheads.pdf
        int[] courseIndices = hungarian(roomAssignments, inclusiveLastIdx);

        List<CourseWithRoom> assignments = new LinkedList<>();
        for (int courseIdx = 0; courseIdx < courseIndices.length; courseIdx++) {
            RoomViolations rv = roomAssignments[courseIdx][courseIndices[courseIdx]];
            assignments.add(new CourseWithRoom(rv.course, rv.room));
        }
        return assignments;
    }

    private int[] hungarian(RoomViolations[][] roomAssignments, int inclusiveLastIdx) {
        // todo do we need a square? wikihow says we can fill it with dummy-highest numbers
        double[][] weights = toDouble(roomAssignments, inclusiveLastIdx);
        HungarianAlgorithm hungarian = new HungarianAlgorithm(weights);
        return hungarian.execute();
    }

    private double[][] toDouble(RoomViolations[][] roomAssignments, int inclusiveLastIdx) {
        double[][] weights = new double[inclusiveLastIdx+1][roomAssignments[0].length];
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                weights[i][j] = roomAssignments[i][j].violations;
            }
        }
        return weights;
    }


    private void calibrate(RoomViolations[][] roomAssignments, int inclusiveLastIdx) {
        /*
         * conversion:
         * 1. all values are require to be positive. preference is as follows:
         *    0, -1, -2, .., 1, 2, ..
         *    whereas in the hungarian method, lower numbers are preferable.
         *    therefore the following formula is used to convert the numbers:
         *
         *    i <= 0 = |i|              the absolute value will make numbers
         *                              closer to zero smaller
         *
         *    i >  0 = |min_i| + i      adding the absolute value of the
         *                              smallest number to the number, makes
         *                              sure the positive numbers are coming
         *                              right after the (originally) negative
         *                              numbers.
         *
         * 2. set all room constraints to a very high number that will make
         *    it impossible for the hungarian method to select them. We
         *    set them to the following value:
         *
         *    i = (|min_i| + max_i + 1) * 10   the plus one is to make sure
         *                                     it can never be null even if
         *                                     all others are zero. the
         *                                     multiplication is just some
         *                                     constant threshold factor.
         *                                     the only thing we need to make
         *                                     sure is that it does not over-
         *                                     flow, but that should be a
         *                                     problem in the current setting
         *                                     where room violations are
         *                                     usually below 500.
         */

        // first round, collect min and max values
        int min_i = 0;
        int max_i = 0;
        for (int r = 0; r <= inclusiveLastIdx; r++) {
            for (int c = 0; c < roomAssignments[r].length-1; c++) {

                int violations = roomAssignments[r][c].violations;

                if (violations < min_i) { min_i = violations; }
                if (violations > max_i) { max_i = violations; }
            }
        }

        // second round, convert our scheme to a hungarian method compatible scheme
        int constraint_i = (Math.abs(min_i) + max_i + 1) * 10;
        for (int r = 0; r <= inclusiveLastIdx; r++) {
            for (int c = 0; c < roomAssignments[r].length - 1; c++) {
                RoomViolations rv = roomAssignments[r][c];

                if (spec.getRoomConstraints().isUnsuitable(rv.course, rv.room)) {
                    roomAssignments[r][c] = rv.fixedViolations(constraint_i);
                } else if (rv.violations <= 0) {
                    roomAssignments[r][c] = rv.fixedViolations(Math.abs(rv.violations));
                } else {
                    roomAssignments[r][c] = rv.fixedViolations(Math.abs(rv.violations) + rv.violations);
                }
            }
        }
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
        RoomViolations[] roomViolations = new RoomViolations[spec.getRooms().size()];

        int i = 0;
        for (Room r : spec.getRooms()) {

            roomIdxMap.put(r.getId(), i);

            int violations = c.getNumberOfStudents() - r.getCapacity();
            roomViolations[i] = new RoomViolations(r, c, violations);

            i++;
        }
        return roomViolations;
    }

    public void remove(Course course) {
        int courseIdx = courseIdxMap.get(course.getId());

        // move all following one back
        for (int i = courseIdx + 1; i < nextCourseIdx; i++) {
            roomAssignments[i-1] = roomAssignments[i];
        }

        // cleanup
        courseIdxMap.remove(course.getId());

        // move all courses after the one to remove
        // one index back
        courseIdxMap.keySet().forEach(cid -> {
            int cIdx = courseIdxMap.get(cid);
            if (cIdx > courseIdx) {
                courseIdxMap.put(cid, cIdx-1);
            }
        });

        // now we have one free at the end
        nextCourseIdx--;

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

