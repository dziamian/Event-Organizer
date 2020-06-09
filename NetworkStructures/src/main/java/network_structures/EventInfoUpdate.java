package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class EventInfoUpdate implements Serializable {

    private final Map<ObjectId, SectorInfoUpdate> sectors;

    public EventInfoUpdate() { this.sectors = new TreeMap<>(); }

    public Map<ObjectId, SectorInfoUpdate> getSectors() {
        return sectors;
    }
}
