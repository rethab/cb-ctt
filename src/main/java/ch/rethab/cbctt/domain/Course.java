package ch.rethab.cbctt.domain;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Reto Habluetzel, 2015
 */
public class Course implements Serializable {

    private final String id;

    private List<String> curricula;

    private final String teacher;

    private final int nLectures;

    private final int nWorkingDays;

    private final int nStudents;

    private final boolean doubleLectures;

    /*
     * cache hashcode for optimization.
     * note that this is very risky business, since this class is essentially
     * modifiable. however, whenever we are interested in the hashCode properties
     * (such as equal objects result in equal hashcodes for sets etc), we only
     * really care about the id, since it identifies a course uniquely.
      */
    private final int hashCache;


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

        // curricula could be empty, but (we hope) it doesn't matter
        this.hashCache = Objects.hash(id, curricula, teacher, nLectures, nWorkingDays, nStudents, doubleLectures);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return course.getId().equals(id);
    }

    @Override
    public int hashCode() {
        return hashCache;
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
