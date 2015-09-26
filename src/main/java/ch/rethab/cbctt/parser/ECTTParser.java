package ch.rethab.cbctt.parser;

import ch.rethab.cbctt.domain.*;

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
    private final Pattern course = Pattern.compile("([\\w\\-]+) ([\\w\\-]+) (\\d+) (\\d+) (\\d+) (\\d+)");
    private final Pattern room = Pattern.compile("([\\w\\-]+) (\\d+) (\\d+)");
    private final Pattern curriculum = Pattern.compile("([\\w\\-]+) (\\d+) (.+)");
    private final Pattern unavailabilityConstraint = Pattern.compile("([\\w\\-]+) (\\d+) (\\d+).*");
    private final Pattern roomConstraint = Pattern.compile("([\\w\\-]+) ([\\w\\-]+)");

    private BufferedReader reader;

    public ECTTParser(BufferedReader reader) {
        this.reader = reader;
    }

    public Specification parse() throws IOException {

        Matcher matcher;

        Specification.Builder builder;

        int ncourses, nrooms, daysPerWeek, periodsPerDay, ncurricula, minLectures, maxLectures;
        int nunavailabilityConstraints, nroomConstraints;
        List<Course> courses = new LinkedList<>();
        List<Room> rooms = new LinkedList<>();
        List<Curriculum> curricula = new LinkedList<>();

        matcher = stringValue.matcher(reader.readLine());
        if (matcher.matches()) { builder = Specification.Builder.name(matcher.group(1)); }
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
        builder.days(daysPerWeek);

        matcher = intValue.matcher(reader.readLine());
        if (matcher.matches()) { periodsPerDay = Integer.parseInt(matcher.group(1)); }
        else { throw new IOException("Expected periodPerDay"); }
        builder.periodsPerDay(periodsPerDay);

        matcher = intValue.matcher(reader.readLine());
        if (matcher.matches()) { ncurricula = Integer.parseInt(matcher.group(1)); }
        else { throw new IOException("Expected ncurricula"); }

        matcher = twoIntValues.matcher(reader.readLine());
        if (matcher.matches()) { minLectures = Integer.parseInt(matcher.group(1));
                                 maxLectures = Integer.parseInt(matcher.group(2)); }
        else { throw new IOException("Expected min/maxLectures"); }
        builder.minLectures(minLectures).maxLectures(maxLectures);

        matcher = intValue.matcher(reader.readLine());
        if (matcher.matches()) { nunavailabilityConstraints = Integer.parseInt(matcher.group(1)); }
        else { throw new IOException("Expected nunavailabilityConstraints"); }

        matcher = intValue.matcher(reader.readLine());
        if (matcher.matches()) { nroomConstraints = Integer.parseInt(matcher.group(1)); }
        else { throw new IOException("Expected nroomConstraints"); }


        reader.readLine(); // empty line
        reader.readLine(); // COURSES
        parseCourses(ncourses, courses);
        courses.forEach(builder::course);

        reader.readLine(); // empty line
        reader.readLine(); // ROOMS
        parseRooms(nrooms, rooms);
        rooms.forEach(builder::room);

        reader.readLine(); // empty line
        reader.readLine(); // CURRICULA
        parseCurricula(ncourses, ncurricula, courses, curricula);
        curricula.forEach(builder::curriculum);

        reader.readLine(); // empty line
        reader.readLine(); // UNAVAILABILITY_CONSTRAINTS:
        builder.unavailabilityConstraints(parseUnavailabilityConstraints(nunavailabilityConstraints, courses, daysPerWeek, periodsPerDay));

        reader.readLine(); // empty line
        reader.readLine(); // ROOM_CONSTRAINTS:
        builder.roomConstraints(parseRoomConstraints(nroomConstraints, courses, rooms));

        return builder.build();
    }

    private RoomConstraints parseRoomConstraints(int nroomConstraints, List<Course> courses, List<Room> rooms) throws IOException {
        RoomConstraints roomConstraints = new RoomConstraints();
        for (int i = 0; i < nroomConstraints; i++) {
            Matcher matcher = roomConstraint.matcher(reader.readLine());
            if (matcher.matches()) {
                String courseID = matcher.group(1);
                String roomID = matcher.group(2);
                Course course = getCourse(courseID, courses);
                Room room = getRoom(roomID, rooms);
                roomConstraints.addRoomConstraint(course, room);
            } else {
                throw new IOException("Expected room constraint");
            }
        }
        return roomConstraints;
    }

    private UnavailabilityConstraints parseUnavailabilityConstraints(int nunavailabilityConstraints, List<Course> courses, int daysPerWeek, int periodsPerDay) throws IOException {
        UnavailabilityConstraints unavailabilityConstraints = new UnavailabilityConstraints(daysPerWeek, periodsPerDay);
        for (int i = 0; i < nunavailabilityConstraints; i++) {
            String line = reader.readLine();
            Matcher matcher = unavailabilityConstraint.matcher(line);
            if (matcher.matches()) {
                String courseID = matcher.group(1);
                int day = Integer.parseInt(matcher.group(2));
                int period = Integer.parseInt(matcher.group(3));
                Course course = getCourse(courseID, courses);
                unavailabilityConstraints.addUnavailability(course, day, period);
            } else {
                throw new IOException("Expected unavailability constraint, but got '" + line + "'");
            }
        }
        return unavailabilityConstraints;
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
                    throw new IOException("Expected " + ncourses + " courses but got " + curriculumCurses.size());
                }
                Curriculum c = new Curriculum(id, curriculumCurses);

                curriculumCurses.forEach(course -> course.addCurriculum(c.getId()));
                curricula.add(c);
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
            String line = reader.readLine();
            Matcher matcher = course.matcher(line);
            if (matcher.matches()) {
                String id = matcher.group(1);
                String teacher = matcher.group(2);
                int nLectures = Integer.parseInt(matcher.group(3));
                int minWorkingDays = Integer.parseInt(matcher.group(4));
                int nStudents = Integer.parseInt(matcher.group(5));
                boolean doubleLectures = Integer.parseInt(matcher.group(6)) == 1;

                courses.add(new Course(id, teacher, nLectures, minWorkingDays, nStudents, doubleLectures));
            } else {
                throw new IOException("Expected course but got '"+line+"'");
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
