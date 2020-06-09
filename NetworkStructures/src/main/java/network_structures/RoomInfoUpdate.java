package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;

public class RoomInfoUpdate implements Serializable {

    private final ObjectId id;
    private String state;
    private int queueSize;

    public RoomInfoUpdate(ObjectId id) {
        this.id = id;
        this.state = "";
        this.queueSize = 0;
    }

    public ObjectId getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }
}
