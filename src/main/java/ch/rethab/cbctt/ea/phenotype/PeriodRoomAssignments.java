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
 *
 */
public class PeriodRoomAssignments {

    private final Specification spec;

    /**
     * These are the courses matched up with all available rooms for
     * this period. The rows are the courses (indices are mapped with
     * courseIdxMap) and the columns are the rooms (indices are mapped
     * with roomIdxMap). While the rooms are known in advance, the
     * courses for this period are added gradually during construction
     * of the timetable.
     *
     * A special last element in each row denotes the constrainedness
     * for the order of assignment. Ie. that column is used solely to
     * determine the order, in which the rooms are assigned. Note that
     * this means we are misusing the RoomViolations class, in that
     * in the last element in each column, the room and course properties
     * will not be set and while the violations is used to denote the
     * number of student violations per course and room, it is used in
     * the last column as the number of rooms that are suitable for this
     * course (due to room constraints).
     */
    private final RoomViolations[][] roomAssignments;

    /* everything is an array, so we need to know at which index each room is stored */
    private final Map<String, RoomWithCompetition> roomIdxMap = new HashMap<>();

    /* everything is an array, so we need to know at which index each room is stored */
    private final Map<String, Integer> courseIdxMap = new HashMap<>();

    /* the course array is built gradually. this is the index where the next is to be
     * scheduled. Note that while checking if a course is suitable to be scheduled
     * at a certain period, a course may be added without incrementing the index after
     * is proofs to be infeasible. This means the index is used to track where in
     * the array courses may be put rather than nulls.
     */
    private int nextCourseIdx = 0;

    /** final assignment can only be done once, because the room indices
     * are updated in place */
    private boolean assignmentDone = false;

    /**
     * Actual entries of the room assignments. This 'tuple' is required, since
     * we need access to the room and we loos the index in the procedure.
     */
    private class RoomViolations {
        /* number of feasibleRooms a certain courses would have in a certain room.
         * the smaller the better. 0 means perfect fit */
        final int feasibleRooms;
        final Room room;
        final Course course;
        public RoomViolations(Room room, Course c, int feasibleRooms) {
            this.room = room;
            this.course = c;
            this.feasibleRooms = feasibleRooms;
        }

        public RoomViolations reduceSuitability() {
            if (room != null || course != null) {
                throw new IllegalStateException("must only be used in last column");
            }
            // in this case, the naming 'feasibleRooms' is a little unfortunate
            int suitabilityIdx = feasibleRooms -1;
            return new RoomViolations(null, null, suitabilityIdx);
        }
    }

    /**
     * For each course, we also track the number of courses that could
     * also potentially be scheduled in this room. This metric is used
     * during assignment, because rooms with fewer competing rooms
     * need to be assigned first.
     *
     * Consider the following scenario, where s means the room is
     * suitable and o means it is occupied. They all have the same
     * number of feasible rooms, which means determining which
     * course is scheduled first may be arbitrary. However, if
     * r1 was assigned to c1, then only r2 would be left for
     * both c2 and c3, which is why in this case c1 must
     * be scheduled in r3. This means r3 has the least number
     * of competing courses - only 1.
     *
     *      r1  r2  r3  feasibleRooms
     * c1 = s   o   s   2
     * c2 = s   s   o   2
     * c3 = s   s   o   2
     */
    private class RoomWithCompetition {
        // index in the array (column of roomAssignments)
        final int idx;
        // number of courses that could be scheduled here as well
        int competingCourses;
        public RoomWithCompetition(int idx, int competingCourses) {
            this.idx = idx;
            this.competingCourses = competingCourses;
        }
    }

    public PeriodRoomAssignments(Specification spec) {
        this.spec = spec;
        Set<String> roomIds = spec.getRooms().stream().map(Room::getId).collect(Collectors.toSet());
        // +1 because the last one counts the number of suitable rooms per course
        roomAssignments = new RoomViolations[roomIds.size()][roomIds.size()+1];
    }

    public List<CourseWithRoom> assignRooms() {

        if (assignmentDone) {
            throw new IllegalStateException("final assignment may only be done once");
        }

        if (nextCourseIdx == 0) {
            // no courses in this period. check required due to -1 below
            return Collections.emptyList();
        }

        RoomViolations[][] copy = null;
        try {
            copy = deepCopy(roomAssignments);
            List<CourseWithRoom> assignments = assignAll(roomAssignments, nextCourseIdx-1, true);
            assignmentDone = true;
            return assignments;
        } catch (InfeasibilityException e) {
            // todo remove this debugging stuff again
            for (int cid = 0; cid < copy.length; cid++) {
                for (int rid = 0; rid < copy[cid].length; rid++) {
                    RoomViolations rv = copy[cid][rid];
                    System.out.printf("[%s, %s, %d] ", rv == null || rv.course == null ? null : rv.course.getId(),
                                                       rv == null || rv.room == null   ? null : rv.room.getId(),
                                                       rv == null                      ? null : rv.feasibleRooms);
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

            reduceCompetitiveness(c);

            return false;
        }
    }

    private void reduceCompetitiveness(Course c) {
        roomIdxMap.entrySet().forEach(kv -> {
            String roomId = kv.getKey();
            RoomWithCompetition rwc = kv.getValue();

            // reduce those that could have been scheduled in this room
            if (!spec.getRoomConstraints().isUnsuitable(c, roomId)) {
                rwc.competingCourses--;
            }
        });
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
     * @param real true if we are assigning for real. false if we're just
     *             checking if the course would fit. purely for optimization
     */
    private List<CourseWithRoom> assignAll(RoomViolations[][] roomAssignments, int lastIdx, boolean real) throws InfeasibilityException{
        List<CourseWithRoom> assignments = new ArrayList<>(lastIdx+1);
        Optional<RoomViolations[]> mbRoomsForCourse = findMostConstrainedCourse(roomAssignments, lastIdx);
        while (mbRoomsForCourse.isPresent()) {
            RoomViolations[] roomsForCourse = mbRoomsForCourse.get();

            // only do the computationally intensive stuff if we're assigning for real
            RoomViolations rv = real ? roomWithLeastViolations(roomsForCourse) : greedyFirstRoom(roomsForCourse);

            if (rv == null) {
                throw new InfeasibilityException();
            }

            RoomWithCompetition rwc = roomIdxMap.get(rv.room.getId());

            // update room and constrainedness for other courses
            int exclusiveLastIdx = lastIdx + 1;
            Arrays.stream(roomAssignments, 0, exclusiveLastIdx).forEach(crViolations -> {
                int constrainednessIdx = spec.getRooms().size();
                // course has not yet been assigned and room was free before
                if (crViolations[rwc.idx] != null && crViolations[constrainednessIdx] != null) {
                    crViolations[constrainednessIdx] = crViolations[constrainednessIdx].reduceSuitability();
                    // room is no longer available
                    crViolations[rwc.idx] = null;
                }
            });

            // set last element in course row to null as an indicator that this course is scheduled
            int courseIdx = courseIdxMap.get(rv.course.getId());
            roomAssignments[courseIdx][spec.getRooms().size()] = null;

            assignments.add(new CourseWithRoom(rv.course, rv.room));

            mbRoomsForCourse = findMostConstrainedCourse(roomAssignments, lastIdx);
        }
        return assignments;
    }

    private RoomViolations roomWithLeastViolations(RoomViolations[] roomsForCourse) {
        Arrays.sort(roomsForCourse, violationsComparator());
        return roomsForCourse[0];
    }

    private RoomViolations greedyFirstRoom(RoomViolations[] roomsForCourse) {
        for (RoomViolations aRoomsForCourse : roomsForCourse) {
            if (aRoomsForCourse != null && aRoomsForCourse.room != null) {
                return aRoomsForCourse;
            }
        }
        return null;
    }

    private Optional<RoomViolations[]> findMostConstrainedCourse(RoomViolations[][] roomAssignments, int lastIdx) {

        // lastIdx = 0 means we also want the 0 to be considered, but Arrays.stream uses an exclusive upper index
        int exclusiveLastIdx = lastIdx + 1;
        return Arrays.stream(roomAssignments, 0, exclusiveLastIdx)

            // course already assigned a room
            .filter(crViolations -> crViolations[spec.getRooms().size()] != null)

            // find course that is hardest to assignRooms (least amount of suitable rooms)
            .sorted(Comparator.comparingInt(crViolations -> crViolations[spec.getRooms().size()].feasibleRooms))

            .findFirst();
    }

    /**
     * Comparator such that the most preferable number of feasibleRooms
     * would be first in an ascending ordered list. I.e. perfect would
     * be 0 feasibleRooms. After that, negative feasibleRooms are preferred,
     * with the closer to 0 the better (negative means the room is too big).
     * After that, we take the positive feasibleRooms (room is too small) in
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
            if (rv == null) { // constraint or already assigned
                return Double.POSITIVE_INFINITY;
            } else if (rv.room == null) { // last column: constrainedness
                return Double.POSITIVE_INFINITY;
            } else if (rv.feasibleRooms < 0) {
                return 1 - (1 / (Math.abs(rv.feasibleRooms)));
            } else {
                return rv.feasibleRooms;
            }
        });
    }

    /**
     * Creates one row for a course with indices per room.
     * Each cell holds the number of feasibleRooms, i.e. the
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

        // number of rooms this course could be scheduled
        int suitable = 0;

        int i = 0;
        for (Room r : spec.getRooms()) {

            RoomWithCompetition rwc = roomIdxMap.get(r.getId());
            if (rwc == null) {
                rwc = new RoomWithCompetition(i, 0);
                roomIdxMap.put(r.getId(), rwc);
            }

            if (spec.getRoomConstraints().isUnsuitable(c, r)) {
                roomViolations[i] = null;
            } else {
                int violations = c.getNumberOfStudents() - r.getCapacity();
                roomViolations[i] = new RoomViolations(r, c, violations);
                suitable++;

                // there is now one more course that could be assigned to this room
                rwc.competingCourses++;
            }

            i++;
        }

        roomViolations[spec.getRooms().size()] = new RoomViolations(null, null, suitable);
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

        public CourseWithRoom(Course course, Room room) {
            this.course = course;
            this.room = room;
        }
    }
}

