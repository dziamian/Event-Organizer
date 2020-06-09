package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;

public class RoomInfoFixed implements Serializable {

    private ObjectId id;
    private String name;
    private String location;
    private String description;

    public RoomInfoFixed(ObjectId id, String name, String location, String description) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }
}
