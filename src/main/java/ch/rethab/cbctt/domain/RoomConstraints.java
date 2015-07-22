package ch.rethab.cbctt.domain;

import java.util.*;

/**
 * @author Reto Habluetzel, 2015
 */
public class RoomConstraints {

    private Map<Course, List<Room>> constraints = new HashMap<>();

    public void addRoomConstraint(Course c, Room r) {
        List<Room> rooms = constraints.get(c);
        if (rooms == null) {
            rooms = new LinkedList<>();
            constraints.put(c, rooms);
        }
        rooms.add(r);
    }

    public boolean isUnsuitable(Course c, Room r) {
        List<Room> rooms = constraints.get(c);
        if (rooms == null) {
            return false;
        } else {
            return rooms.contains(r);
        }
    }
}
