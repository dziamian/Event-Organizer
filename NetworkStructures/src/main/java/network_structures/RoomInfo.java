package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;

public class RoomInfo implements Serializable {

    public ObjectId id;
    public String name;
    public String location;
    public String description;

    public RoomInfo(ObjectId id, String name, String location, String description) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
    }
}
