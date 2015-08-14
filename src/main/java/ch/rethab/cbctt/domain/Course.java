package ch.rethab.cbctt.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Reto Habluetzel, 2015
 */
public class Course {

    private final String id;

    private List<String> curricula;

    private final String teacher;

    private final int nLectures;

    private final int nWorkingDays;

    private final int nStudents;

    private final boolean doubleLectures;

    public Course(String id, String teacher, int nLectures, int nWorkingDays, int nStudents, boolean doubleLectures) {
        this(id, null, teacher, nLectures, nWorkingDays, nStudents, doubleLectures);
        this.curricula = new LinkedList<>();
    }

    private Course(String id, List<String> curricula, String teacher, int nLectures, int nWorkingDays, int nStudents, boolean doubleLectures) {
        this.id = id;
        this.curricula = curricula;
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
                Objects.equals(curricula, course.curricula) &&
                Objects.equals(nStudents, course.nStudents) &&
                Objects.equals(doubleLectures, course.doubleLectures) &&
                Objects.equals(id, course.id) &&
                Objects.equals(teacher, course.teacher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, curricula, teacher, nLectures, nWorkingDays, nStudents, doubleLectures);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Course{");
        sb.append("id='").append(id).append('\'');
        sb.append(", curricula='").append(curricula).append('\'');
        sb.append(", teacher='").append(teacher).append('\'');
        sb.append(", nLectures=").append(nLectures);
        sb.append(", nWorkingDays=").append(nWorkingDays);
        sb.append(", nStudents=").append(nStudents);
        sb.append(", doubleLectures=").append(doubleLectures);
        sb.append('}');
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public int getNumberOfLectures() {
        return nLectures;
    }

    public String getTeacher() {
        return teacher;
    }

    public List<String> getCurricula() {
        return curricula;
    }

    public void setCurricula(List<String> curricula) {
        this.curricula = curricula;
    }

    public int getNumberOfStudents() {
        return nStudents;
    }

    public void addCurriculum(String curriculumID) {
        this.curricula.add(curriculumID);
    }

    public int getMinWorkingDays() {
        return nWorkingDays;
    }

    public static class Builder {
        private String id;
        private List<String> curricula = new LinkedList<>();
        private String teacher;
        private int nLectures;
        private int nWorkingDays;
        private int nStudents;
        private boolean doubleLectures;

        public static Builder id(String id) {
            Builder b = new Builder();
            b.id = id;
            return b;
        }

        public Course build() {
            if (id == null || curricula.isEmpty() || teacher == null) {
                throw new IllegalArgumentException("Mandatory argument missing (id, curricula or teacher)");
            }
            return new Course(id, curricula, teacher, nLectures, nWorkingDays, nStudents, doubleLectures);
        }

        public Builder curriculum(Curriculum curriculum) {
            this.curricula.add(curriculum.getId());
            return this;
        }

        public Builder teacher(String teacher) {
            this.teacher = teacher;
            return this;
        }

        public Builder nlectures(int nLectures) {
            this.nLectures = nLectures;
            return this;
        }

        public Builder nWorkingDays(int nWorkingDays) {
            this.nWorkingDays = nWorkingDays;
            return this;
        }

        public Builder nStudents(int nStudents) {
            this.nStudents = nStudents;
            return this;
        }

        public Builder doubleLectures(boolean doubleLectures) {
            this.doubleLectures = doubleLectures;
            return this;
        }
    }
}
