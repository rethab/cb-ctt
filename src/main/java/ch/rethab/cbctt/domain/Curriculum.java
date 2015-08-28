package ch.rethab.cbctt.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Reto Habluetzel, 2015
 */
public class Curriculum implements Serializable {

    private final String id;

    private List<Course> courses;

    public Curriculum(String id) {
        this(id, Collections.<Course>emptyList());
    }

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Curriculum{");
        sb.append("id='").append(id).append('\'');
        sb.append(", courses=").append(courses);
        sb.append('}');
        return sb.toString();
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public String getId() {
        return this.id;
    }

    public List<Course> getCourses() {
        return courses;
    }
}
