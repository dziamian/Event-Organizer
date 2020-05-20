package network_structures;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Map;

public class UpdateData implements Serializable {
    public Map<ObjectId, SectorUpdate> sectors;
}
