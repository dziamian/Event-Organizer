package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Map;

public class EventData  implements Serializable {
    public Map<ObjectId, SectorInfo> sectors;
}
