package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class representing mutable information about specific room.
 */
public class RoomInfoUpdate implements Serializable {

    private final ObjectId id;
    private String state;
    private final AtomicInteger queueSize;

    public RoomInfoUpdate(ObjectId id) {
        this.id = id;
        this.state = "";
        this.queueSize = new AtomicInteger(0);
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

    public AtomicInteger getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize.set(queueSize);
    }
}
