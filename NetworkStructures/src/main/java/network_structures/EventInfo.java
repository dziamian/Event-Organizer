package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class EventInfo implements Serializable {

    private Map<ObjectId, SectorInfo> sectors;

    public EventInfo() {
        this.sectors = new TreeMap<>();
    }

    public Map<ObjectId, SectorInfo> getSectors() {
        return sectors;
    }
}
