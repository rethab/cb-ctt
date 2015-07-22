package ch.rethab.cbctt.domain;

import java.util.List;
import java.util.Objects;

/**
 * @author Reto Habluetzel, 2015
 */
public class Curriculum {

    private final String id;

    private final List<Course> courses;

    public Curriculum(String id, List<Course> courses) {
        this.id = id;
        this.courses = courses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Curriculum that = (Curriculum) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(courses, that.courses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, courses);
    }
}
