package ch.rethab.cbctt.domain;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Reto Habluetzel, 2015
 */
public class Specification {

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

    public Specification(String name, int numberOfDaysPerWeek, int periodsPerDay, int minLectures, int maxLectures,
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
        return courses.stream().map(c -> c.getTeacher()).collect(Collectors.toSet());
    }
}


