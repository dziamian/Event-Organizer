package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;

public class ReservationInfo implements Serializable {

    private final ObjectId sectorId;
    private final ObjectId roomId;
    private final Date expirationDate;

    public ReservationInfo(ObjectId sectorId, ObjectId roomId, Date expirationDate) {
        this.sectorId = sectorId;
        this.roomId = roomId;
        this.expirationDate = expirationDate;
    }

    public ObjectId getSectorId() {
        return sectorId;
    }

    public ObjectId getRoomId() {
        return roomId;
    }

    public Date getExpirationDate() { return expirationDate; }
}
