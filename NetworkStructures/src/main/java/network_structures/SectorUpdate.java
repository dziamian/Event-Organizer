package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class SectorUpdate implements Serializable {

    public ObjectId id;

    public Map<ObjectId, RoomUpdate> rooms;
    public int currentActive;

    public SectorUpdate(ObjectId id) {
        this.id = id;
        this.rooms = new TreeMap<>();
        this.currentActive = 0;
    }
}
