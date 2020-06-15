package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class containing immutable information about specific room.
 */
public class RoomInfoFixed implements Serializable {

    private ObjectId sectorId;
    private ObjectId id;
    private String name;
    private String location;
    private String description;
    private String state;
    private AtomicInteger queueSize;

    public RoomInfoFixed(ObjectId id, ObjectId sectorId, String name, String location, String description, String state, AtomicInteger queueSize) {
        this.id = id;
        this.sectorId = sectorId;
        this.name = name;
        this.location = location;
        this.description = description;
        this.state = state;
        this.queueSize = queueSize;
    }

    public ObjectId getId() {
        return id;
    }

    /**
     * @return {@link RoomInfoFixed#id}
     */
    public ObjectId getSectorId() { return sectorId; }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public AtomicInteger getQueueSize() { return queueSize; }
}
