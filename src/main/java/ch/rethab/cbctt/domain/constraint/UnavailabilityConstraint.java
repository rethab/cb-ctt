package ch.rethab.cbctt.domain.constraint;

import ch.rethab.cbctt.domain.Course;

import java.util.Objects;

/**
 * @author Reto Habluetzel, 2015
 */
public class UnavailabilityConstraint implements Constraint {

    private final Course course;
    private final int day;
    private final int period;

    public UnavailabilityConstraint(Course course, int day, int period) {
        this.course = course;
        this.day = day;
        this.period = period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnavailabilityConstraint that = (UnavailabilityConstraint) o;
        return Objects.equals(day, that.day) &&
                Objects.equals(period, that.period) &&
                Objects.equals(course, that.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, day, period);
    }

    public Course getCourse() {
        return course;
    }

    public int getDay() {
        return day;
    }

    public int getPeriod() {
        return period;
    }
}
