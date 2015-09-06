package ch.rethab.cbctt.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Rooms constraints restrictions on courses that
 * shall not take place in a certain room, because
 * the room is unsuitable (eg. necessary equipment
 * is missing).
 *
 * @author Reto Habluetzel, 2015
 */
public class RoomConstraints implements Serializable {

    private final Map<Course, Set<String>> constraints = new HashMap<>();

    public void addRoomConstraint(Course c, Room r) {
        Set<String> roomIds = constraints.get(c);
        if (roomIds == null) {
            roomIds = new LinkedHashSet<>();
            constraints.put(c, roomIds);
        }
        roomIds.add(r.getId());
    }

    public boolean isUnsuitable(Course c, Room r) {
        return this.isUnsuitable(c, r.getId());
    }

    public boolean isUnsuitable(Course c, String roomId) {
        Set<String> roomIds = constraints.get(c);
        return roomIds != null && roomIds.contains(roomId);
    }
}
