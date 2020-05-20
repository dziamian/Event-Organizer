package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;

public class RoomUpdate implements Serializable {

    public ObjectId id;
    public String state;
    public int queueSize;

    public RoomUpdate(ObjectId id) {
        this.id = id;
        this.state = "";
        this.queueSize = 0;
    }
}
