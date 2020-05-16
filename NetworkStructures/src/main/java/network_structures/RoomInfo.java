package network_structures;

import java.io.Serializable;

public class RoomInfo implements Serializable {

    public String name;
    public String description;

    public RoomInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
