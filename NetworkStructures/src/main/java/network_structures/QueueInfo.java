package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;

public class QueueInfo implements Serializable {

    private final ObjectId sectorId;
    private final ObjectId roomId;
    private int positionInQueue;

    public QueueInfo(ObjectId sectorId, ObjectId roomId, int positionInQueue) {
        this.sectorId = sectorId;
        this.roomId = roomId;
        this.positionInQueue = positionInQueue;
    }

    public ObjectId getSectorId() {
        return sectorId;
    }

    public ObjectId getRoomId() {
        return roomId;
    }

    public int getPositionInQueue() {
        return positionInQueue;
    }

    public void setPositionInQueue(int positionInQueue) {
        this.positionInQueue = positionInQueue;
    }
}
