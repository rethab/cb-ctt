package ch.rethab.cbctt.ea.phenotype;

import java.io.Serializable;

public interface RoomAssigner extends Serializable {

    TimetableWithRooms assignRooms(Timetable t);

}
