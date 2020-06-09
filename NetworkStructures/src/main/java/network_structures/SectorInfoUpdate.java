package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class SectorInfoUpdate implements Serializable {

    private final ObjectId id;

    private final Map<ObjectId, RoomInfoUpdate> rooms;
    private int activeRooms;

    public SectorInfoUpdate(ObjectId id) {
        assert id != null : "ObjectId cannot be null";
        this.id = id;
        this.rooms = new TreeMap<>();
        this.activeRooms = 0;
    }

    public ObjectId getId() {
        return id;
    }

    public Map<ObjectId, RoomInfoUpdate> getRooms() {
        return rooms;
    }

    public int getActiveRooms() {
        return activeRooms;
    }

    public void setActiveRooms(int activeRooms) {
        this.activeRooms = activeRooms;
    }
}
