package ch.rethab.cbctt.domain;

import ch.rethab.cbctt.domain.constraint.RoomConstraint;
import ch.rethab.cbctt.domain.constraint.UnavailabilityConstraint;

import java.util.List;

/**
 * @author Reto Habluetzel, 2015
 */
public class ECTT {

    private final String name;

    private final int numberOfDaysPerWeek;

    private final int periodsPerDay;

    private final int minLectures;

    private final int maxLectures;

    private final List<Course> courses;

    private final List<Room> rooms;

    private final List<Curriculum> curricula;

    private final List<UnavailabilityConstraint> unavailabilityConstraints;

    private final List<RoomConstraint> roomConstraints;

    public ECTT(String name, int numberOfDaysPerWeek, int periodsPerDay, int minLectures, int maxLectures,
                List<Course> courses, List<Room> rooms, List<Curriculum> curricula,
                List<UnavailabilityConstraint> unavailabilityConstraints, List<RoomConstraint> roomConstraints) {
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

    public List<UnavailabilityConstraint> getUnavailabilityConstraints() {
        return unavailabilityConstraints;
    }

    public List<RoomConstraint> getRoomConstraints() {
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
}


