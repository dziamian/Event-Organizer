package network_structures;

import java.io.Serializable;

public class RoomInfo implements Serializable {

    private String name;
    private String location;
    private String description;

    public RoomInfo(String name, String location, String description) {
        this.name = name;
        this.location = location;
        this.description = description;
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
