package network_structures;

import java.io.Serializable;

public class RoomInfo implements Serializable {

    public String name;
    public String location;
    public String description;

    public RoomInfo(String name, String location, String description) {
        this.name = name;
        this.location = location;
        this.description = description;
    }
}
