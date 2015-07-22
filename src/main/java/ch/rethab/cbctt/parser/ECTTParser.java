package ch.rethab.cbctt.parser;

import ch.rethab.cbctt.domain.*;
import ch.rethab.cbctt.domain.constraint.RoomConstraint;
import ch.rethab.cbctt.domain.constraint.UnavailabilityConstraint;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the 'Extended Curriculum Timetabling' Format
 *
 * @see 'http://satt.diegm.uniud.it/ctt/index.php?page=format'
 *
 * @author Reto Habluetzel, 2015
 */
public final class ECTTParser {

    private final Pattern stringValue = Pattern.compile("[^\\s]+: ([^\\s]+)");
    private final Pattern intValue = Pattern.compile("[^\\s]+: (\\d+)");
    private final Pattern twoIntValues = Pattern.compile("[^\\s]+: (\\d+) (\\d+)");
    private final Pattern course = Pattern.compile("(\\w+) (\\w+) (\\d+) (\\d+) (\\d+) (\\d+)");
    private final Pattern room = Pattern.compile("(\\w+) (\\d+) (\\d+)");
    private final Pattern curriculum = Pattern.compile("(\\w+) (\\d+) (.+)");
    private final Pattern unavailabilityConstraint = Pattern.compile("(\\w+) (\\d+) (\\d+)");
    private final Pattern roomConstraint = Pattern.compile("(\\w+) (\\w+)");

    private BufferedReader reader;

    public ECTTParser(BufferedReader reader) {
        this.reader = reader;
    }

    public ECTT parse() throws IOException {

        Matcher matcher;

        String name;
        int ncourses, nrooms, daysPerWeek, periodsPerDay, ncurricula, minLectures, maxLectures;
        int nunavailabilityConstraints, nroomConstraints;
        List<Course> courses = new LinkedList<>();
        List<Room> rooms = new LinkedList<>();
        List<Curriculum> curricula = new LinkedList<>();
        List<UnavailabilityConstraint> unavailabilityConstraints = new LinkedList<>();
        List<RoomConstraint> roomConstraints = new LinkedList<>();

        matcher = stringValue.matcher(reader.readLine());
        if (matcher.matches()) { name = matcher.group(1); }
        else { throw new IOException("Expected name"); }

        matcher = intValue.matcher(reader.readLine());
        if (matcher.matches()) { ncourses = Integer.parseInt(matcher.group(1)); }
        else { throw new IOException("Expected ncourses"); }

        matcher = intValue.matcher(reader.readLine());
        if (matcher.matches()) { nrooms = Integer.parseInt(matcher.group(1)); }
        else { throw new IOException("Expected nrooms"); }

        matcher = intValue.matcher(reader.readLine());
        if (matcher.matches()) { daysPerWeek = Integer.parseInt(matcher.group(1)); }
        else { throw new IOException("Expected ndays"); }

        matcher = intValue.matcher(reader.readLine());
        if (matcher.matches()) { periodsPerDay = Integer.parseInt(matcher.group(1)); }
        else { throw new IOException("Expected periodPerDay"); }

        matcher = intValue.matcher(reader.readLine());
        if (matcher.matches()) { ncurricula = Integer.parseInt(matcher.group(1)); }
        else { throw new IOException("Expected ncurricula"); }

        matcher = twoIntValues.matcher(reader.readLine());
        if (matcher.matches()) { minLectures = Integer.parseInt(matcher.group(1));
                                 maxLectures = Integer.parseInt(matcher.group(2)); }
        else { throw new IOException("Expected min/maxLectures"); }

        matcher = intValue.matcher(reader.readLine());
        if (matcher.matches()) { nunavailabilityConstraints = Integer.parseInt(matcher.group(1)); }
        else { throw new IOException("Expected nunavailabilityConstraints"); }

        matcher = intValue.matcher(reader.readLine());
        if (matcher.matches()) { nroomConstraints = Integer.parseInt(matcher.group(1)); }
        else { throw new IOException("Expected nroomConstraints"); }

        reader.readLine(); // empty line
        reader.readLine(); // COURSES
        parseCourses(ncourses, courses);

        reader.readLine(); // empty line
        reader.readLine(); // ROOMS
        parseRooms(nrooms, rooms);

        reader.readLine(); // empty line
        reader.readLine(); // CURRICULA
        parseCurricula(ncourses, ncurricula, courses, curricula);

        reader.readLine(); // empty line
        reader.readLine(); // UNAVAILABILITY_CONSTRAINTS:
        parseUnavailabilityConstraints(nunavailabilityConstraints, unavailabilityConstraints, courses);

        reader.readLine(); // empty line
        reader.readLine(); // ROOM_CONSTRAINTS:
        parseRoomConstraints(nroomConstraints, roomConstraints, courses, rooms);

        return new ECTT(name, daysPerWeek, periodsPerDay, minLectures, maxLectures, courses, rooms, curricula, unavailabilityConstraints, roomConstraints);

        }

    private void parseRoomConstraints(int nroomConstraints, List<RoomConstraint> roomConstraints, List<Course> courses, List<Room> rooms) throws IOException {
        for (int i = 0; i < nroomConstraints; i++) {
            Matcher matcher = roomConstraint.matcher(reader.readLine());
            if (matcher.matches()) {
                String courseID = matcher.group(1);
                String roomID = matcher.group(2);
                Course course = getCourse(courseID, courses);
                Room room = getRoom(roomID, rooms);
                roomConstraints.add(new RoomConstraint(course, room));
            } else {
                throw new IOException("Expected room constraint");
            }
        }
    }

    private void parseUnavailabilityConstraints(int nunavailabilityConstraints, List<UnavailabilityConstraint> unavailabilityConstraints, List<Course> courses) throws IOException {
        for (int i = 0; i < nunavailabilityConstraints; i++) {
            Matcher matcher = unavailabilityConstraint.matcher(reader.readLine());
            if (matcher.matches()) {
                String courseID = matcher.group(1);
                int day = Integer.parseInt(matcher.group(2));
                int period = Integer.parseInt(matcher.group(3));
                List<Course> course = getCourses(courseID, courses);
                if (course.size() != 1) {
                    throw new IOException("Expected 1 course but got " + course.size());
                }
                unavailabilityConstraints.add(new UnavailabilityConstraint(course.get(0), day, period));
            } else {
                throw new IOException("Expected unavailability constraint");
            }
        }
    }

    private void parseCurricula(int ncourses, int ncurricula, List<Course> courses, List<Curriculum> curricula) throws IOException {
        for (int i = 0; i < ncurricula; i++) {
            Matcher matcher = curriculum.matcher(reader.readLine());
            if (matcher.matches()) {
                String id = matcher.group(1);
                int nCurses = Integer.parseInt(matcher.group(2));
                String coursesString = matcher.group(3);
                List<Course> curriculumCurses = getCourses(coursesString, courses);
                if (curriculumCurses.size() != nCurses) {
                    throw new IOException("Expected " + ncourses + " but got " + curriculumCurses.size());
                }
                curricula.add(new Curriculum(id, curriculumCurses));
            } else {
                throw new IOException("Expected curriculum");
            }
        }
    }

    private void parseRooms(int nrooms, List<Room> rooms) throws IOException {
        for (int i = 0; i < nrooms; i++) {
            Matcher matcher = room.matcher(reader.readLine());
            if (matcher.matches()) {
                String id = matcher.group(1);
                int capacity = Integer.parseInt(matcher.group(2));
                int site = Integer.parseInt(matcher.group(3));

                rooms.add(new Room(id, capacity, site));
            } else {
                throw new IOException("Expected room");
            }
        }
    }

    private void parseCourses(int ncourses, List<Course> courses) throws IOException {
        for (int i = 0; i < ncourses; i++) {
            Matcher matcher = course.matcher(reader.readLine());
            if (matcher.matches()) {
                String id = matcher.group(1);
                String teacher = matcher.group(2);
                int nLectures = Integer.parseInt(matcher.group(3));
                int minWorkingDays = Integer.parseInt(matcher.group(4));
                int nStudents = Integer.parseInt(matcher.group(5));
                boolean doubleLectures = Integer.parseInt(matcher.group(6)) == 1;

                courses.add(new Course(id, teacher, nLectures, minWorkingDays, nStudents, doubleLectures));
            } else {
                throw new IOException("Expected course");
            }
        }
    }

    private List<Course> getCourses(String coursesString, List<Course> courses) throws IOException {
        List<Course> curriculaCourses = new LinkedList<>();
        for (String courseID : coursesString.split(" ")) {
            curriculaCourses.add(getCourse(courseID, courses));
        }
        return curriculaCourses;
    }

    private Course getCourse(String courseID, List<Course> courses) throws IOException {
        for (Course c : courses) {
            if (c.getId().equals(courseID)) {
                return c;
            }
        }
        throw new IOException("Missing course " + courseID);
    }

    private Room getRoom(String roomID, List<Room> rooms) throws IOException {
        for (Room r : rooms) {
            if (r.getId().equals(roomID)) {
                return r;
            }
        }
        throw new IOException("Missing room " + roomID);
    }

}
