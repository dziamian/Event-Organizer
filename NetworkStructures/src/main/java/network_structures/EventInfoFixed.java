package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class containing immutable information about event.
 */
public class EventInfoFixed implements Serializable {

    private final Map<ObjectId, SectorInfoFixed> sectors;

    public EventInfoFixed() {
        this.sectors = Collections.synchronizedMap(new TreeMap<>());
    }

    public Map<ObjectId, SectorInfoFixed> getSectors() {
        return sectors;
    }
}
