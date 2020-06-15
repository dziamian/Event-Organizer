package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class representing mutable information about specific sector.
 */
public class SectorInfoUpdate implements Serializable {

    private final ObjectId id;

    private final Map<ObjectId, RoomInfoUpdate> rooms;
    private AtomicInteger activeRoomsCount;

    public SectorInfoUpdate(ObjectId id) {
        this.id = id;
        this.rooms = Collections.synchronizedMap(new TreeMap<>());
        this.activeRoomsCount = new AtomicInteger(0);
    }

    public ObjectId getId() {
        return this.id;
    }

    public Map<ObjectId, RoomInfoUpdate> getRooms() {
        return this.rooms;
    }

    public AtomicInteger getActiveRoomsCount() {
        return this.activeRoomsCount;
    }

    public void setActiveRoomsCount(int activeRoomsCount) {
        this.activeRoomsCount.set(activeRoomsCount);
    }
}
