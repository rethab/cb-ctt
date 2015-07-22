package ch.rethab.cbctt.domain;

import java.util.Objects;

/**
 * @author Reto Habluetzel, 2015
 */
public class Course {

    private final String id;

    private final String teacher;

    private final int nLectures;

    private final int nWorkingDays;

    private final int nStudents;

    private final boolean doubleLectures;

    public Course(String id, String teacher, int nLectures, int nWorkingDays, int nStudents, boolean doubleLectures) {
        this.id = id;
        this.teacher = teacher;
        this.nLectures = nLectures;
        this.nWorkingDays = nWorkingDays;
        this.nStudents = nStudents;
        this.doubleLectures = doubleLectures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(nLectures, course.nLectures) &&
                Objects.equals(nWorkingDays, course.nWorkingDays) &&
                Objects.equals(nStudents, course.nStudents) &&
                Objects.equals(doubleLectures, course.doubleLectures) &&
                Objects.equals(id, course.id) &&
                Objects.equals(teacher, course.teacher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, teacher, nLectures, nWorkingDays, nStudents, doubleLectures);
    }

    public String getId() {
        return id;
    }
}
