package network_structures;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.bson.types.ObjectId;

public class SectorInfoFixed implements Serializable {

    private ObjectId id;
    private String name;
    private String address;
    private String description;
    private Map<ObjectId, RoomInfoFixed> rooms;

    public SectorInfoFixed(ObjectId id, String name, String address, String description) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.rooms = new TreeMap<>();
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
}
