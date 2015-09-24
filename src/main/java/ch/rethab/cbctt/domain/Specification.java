package ch.rethab.cbctt.domain;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Reto Habluetzel, 2015
 */
public class Specification implements Serializable {

    private final String name;

    private final int numberOfDaysPerWeek;

    private final int periodsPerDay;

    private final int minLectures;

    private final int maxLectures;

    private final List<Course> courses;

    private final List<Room> rooms;

    private final List<Curriculum> curricula;

    private final UnavailabilityConstraints unavailabilityConstraints;

    private final RoomConstraints roomConstraints;

    private final Map<String, Set<Curriculum>> curriculaByCourseCache;

    private Specification(String name, int numberOfDaysPerWeek, int periodsPerDay, int minLectures, int maxLectures,
                         List<Course> courses, List<Room> rooms, List<Curriculum> curricula,
                         UnavailabilityConstraints unavailabilityConstraints, RoomConstraints roomConstraints) {
        this.name = name;
        this.numberOfDaysPerWeek = numberOfDaysPerWeek;
        this.periodsPerDay = periodsPerDay;
        this.minLectures = minLectures;
        this.maxLectures = maxLectures;
        this.courses = courses;
        this.rooms = rooms;
        this.curricula = curricula;
        this.unavailabilityConstraints = unavailabilityConstraints;
        this.roomConstraints = roomConstraints;

        // build cache
        curriculaByCourseCache = new HashMap<>();
        courses.forEach(c ->
            curriculaByCourseCache.put(c.getId(), curricula.stream().filter(eachCourse -> eachCourse.getCourses().contains(c)).collect(Collectors.toSet()))
        );
    }

    public UnavailabilityConstraints getUnavailabilityConstraints() {
        return unavailabilityConstraints;
    }

    public RoomConstraints getRoomConstraints() {
        return roomConstraints;
    }

    public List<Curriculum> getCurricula() {
        return curricula;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public int getPeriodsPerDay() {
        return periodsPerDay;
    }

    public int getNumberOfDaysPerWeek() {
        return numberOfDaysPerWeek;
    }

    public String getName() {
        return name;
    }

    public int getMinLectures() {
        return minLectures;
    }

    public int getMaxLectures() {
        return maxLectures;
    }

    public Set<String> getTeachers() {
        return courses.stream().map(Course::getTeacher).collect(Collectors.toSet());
    }

    public Set<Curriculum> getByCourse(Course course) {
        return curriculaByCourseCache.get(course.getId());
    }

    public static class Builder {

        private String name;
        private Integer numberOfDaysPerWeek;
        private Integer periodsPerDay;
        private Integer minLectures;
        private Integer maxLectures;
        private List<Course> courses = new LinkedList<>();
        private List<Room> rooms = new LinkedList<>();
        private List<Curriculum> curricula = new LinkedList<>();
        private UnavailabilityConstraints unavailabilityConstraints;
        private RoomConstraints roomConstraints;

        public static Builder name(String name) {
            Builder b = new Builder();
            b.name = name;
            return b;
        }

        public Specification build() {
            if (name == null || numberOfDaysPerWeek == null || periodsPerDay == null || minLectures == null || maxLectures == null || courses.isEmpty() ||
                    rooms.isEmpty() || curricula.isEmpty() || unavailabilityConstraints == null || roomConstraints == null) {
                throw new IllegalArgumentException("Nothing can be null here..");
            }
            return new Specification(name, numberOfDaysPerWeek, periodsPerDay, minLectures,
                    maxLectures, courses, rooms, curricula, unavailabilityConstraints, roomConstraints);
        }

        public Builder days(int numberOfDaysPerWeek) {
            this.numberOfDaysPerWeek = numberOfDaysPerWeek;
            return this;
        }

        public Builder periodsPerDay(int periodsPerDay) {
            this.periodsPerDay = periodsPerDay;
            return this;
        }

        public Builder minLectures(int minLectures) {
            this.minLectures = minLectures;
            return this;
        }

        public Builder maxLectures(int maxLectures) {
            this.maxLectures = maxLectures;
            return this;
        }

        public Builder course(Course course) {
            this.courses.add(course);
            return this;
        }

        public Builder room(Room room) {
            this.rooms.add(room);
            return this;
        }

        public Builder curriculum(Curriculum curriculum) {
            this.curricula.add(curriculum);
            return this;
        }
        public Builder unavailabilityConstraints(UnavailabilityConstraints unavailabilityConstraints) {
            this.unavailabilityConstraints = unavailabilityConstraints;
            return this;
        }
        public Builder roomConstraints(RoomConstraints roomConstraints) {
            this.roomConstraints = roomConstraints;
            return this;
        }
    }
}


