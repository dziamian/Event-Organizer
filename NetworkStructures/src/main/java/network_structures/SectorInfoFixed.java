package network_structures;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.types.ObjectId;

/**
 * Class representing immutable information about specific sector.
 */
public class SectorInfoFixed implements Serializable {

    private ObjectId id;
    private String name;
    private String address;
    private String description;
    private Map<ObjectId, RoomInfoFixed> rooms;
    private AtomicInteger activeRooms;

    public SectorInfoFixed(ObjectId id, String name, String address, String description, AtomicInteger activeRooms) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.rooms = Collections.synchronizedMap(new TreeMap<>());
        this.activeRooms = activeRooms;
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public Map<ObjectId, RoomInfoFixed> getRooms() {
        return rooms;
    }

    public AtomicInteger getActiveRooms() { return activeRooms; }
}
