package ch.rethab.cbctt.domain;

import java.util.Objects;

/**
 * @author Reto Habluetzel, 2015
 */
public class Room {

    private final String id;

    private final int capacity;

    private final int site;

    public Room(String id, int capacity, int site) {
        this.id = id;
        this.capacity = capacity;
        this.site = site;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(capacity, room.capacity) &&
                Objects.equals(site, room.site) &&
                Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, capacity, site);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Room{");
        sb.append("id='").append(id).append('\'');
        sb.append(", capacity=").append(capacity);
        sb.append(", site=").append(site);
        sb.append('}');
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }
}
