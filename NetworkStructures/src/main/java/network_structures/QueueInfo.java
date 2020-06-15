package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * Structure representing information about specific ticket (place within room queue).
 */
public class QueueInfo implements Serializable {
    /** Room's parent Sector identifier */
    private final ObjectId sectorId;
    /** Owner of queue containing this ticket */
    private final ObjectId roomId;
    /** Position of this ticket in the queue */
    private final int positionInQueue;

    /**
     * Creates basic queue information structure
     * @param sectorId Parent sector id
     * @param roomId Queue owner id
     * @param positionInQueue Position within queue
     */
    public QueueInfo(ObjectId sectorId, ObjectId roomId, int positionInQueue) {
        this.sectorId = sectorId;
        this.roomId = roomId;
        this.positionInQueue = positionInQueue;
    }

    /**
     * Getter for parent sector identifier.
     * @return parent sector identifier
     */
    public ObjectId getSectorId() {
        return sectorId;
    }

    /**
     * Getter for queue owner identifier.
     * @return Queue owner identifier
     */
    public ObjectId getRoomId() {
        return roomId;
    }

    /**
     * Getter for ticket placement within queue.
     * @return Position of this ticket within given room's queue at the time of creation
     */
    public int getPositionInQueue() {
        return positionInQueue;
    }
}
