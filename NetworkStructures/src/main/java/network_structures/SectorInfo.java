package network_structures;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.bson.types.ObjectId;

public class SectorInfo implements Serializable {

    public ObjectId id;
    public String name;
    public String address;
    public String description;
    public Map<ObjectId, RoomInfo> rooms;

    public SectorInfo(ObjectId id, String name, String address, String description) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.rooms = new TreeMap<>();
    }
}
