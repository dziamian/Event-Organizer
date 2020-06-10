package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class EventInfoUpdate implements Serializable {

    private final Map<ObjectId, SectorInfoUpdate> sectors;

    public EventInfoUpdate() { this.sectors = Collections.synchronizedMap(new TreeMap<>()); }

    public Map<ObjectId, SectorInfoUpdate> getSectors() {
        return sectors;
    }
}
