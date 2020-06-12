package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SectorInfoUpdate implements Serializable {

    private final ObjectId id;

    private final Map<ObjectId, RoomInfoUpdate> rooms;
    private AtomicInteger activeRooms;

    public SectorInfoUpdate(ObjectId id) {
        this.id = id;
        this.rooms = new TreeMap<>();
        this.activeRooms = new AtomicInteger(0);
    }

    public ObjectId getId() {
        return this.id;
    }

    public Map<ObjectId, RoomInfoUpdate> getRooms() {
        return this.rooms;
    }

    public AtomicInteger getActiveRooms() {
        return this.activeRooms;
    }

    public void setActiveRooms(int activeRooms) {
        this.activeRooms.set(activeRooms);
    }
}
