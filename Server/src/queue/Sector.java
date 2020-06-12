package queue;

import network_structures.SectorInfoFixed;
import network_structures.SectorInfoUpdate;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class Sector {

    private final SectorInfoFixed infoFixed;
    private final SectorInfoUpdate infoUpdate;

    private Map<ObjectId,Room> rooms;
    private int currentSize;

    public Sector(ObjectId id, String name, String address, String description) {
        this.infoUpdate = new SectorInfoUpdate(id);
        this.infoFixed = new SectorInfoFixed(id, name, address, description, this.infoUpdate.getActiveRoomsCount());
        this.rooms = new TreeMap<>();
        this.currentSize = 0;
    }

    public SectorInfoFixed getInfoFixed() {
        return infoFixed;
    }

    public SectorInfoUpdate getInfoUpdate() { return infoUpdate; }

    public void addRoom(ObjectId key, Room room) {
        rooms.put(key,room);
        infoFixed.getRooms().put(key, room.getInfoFixed());
        ++currentSize;
        infoUpdate.getRooms().put(key, room.getInfoUpdate());
        infoUpdate.setActiveRoomsCount(infoUpdate.getActiveRoomsCount().get()+1);
    }

    public Collection<Room> getRoomsValues() {
        return rooms.values();
    }

    public Map<ObjectId, Room> getRooms() { return rooms; }

    public Room getRoom(ObjectId key) {
        return rooms.get(key);
    }

    public int getRoomsSize() {
        return rooms.size();
    }

    public int getCurrentSize() {
        return currentSize;
    }
}
