package queue;

import network_structures.SectorInfo;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class Sector {

    private SectorInfo informations;

    private Map<ObjectId,Room> rooms;
    private int currentSize;

    public Sector(String name, String address, String description) {
        this.informations = new SectorInfo(name, address, description);
        this.rooms = new TreeMap<>();
        this.currentSize = 0;
    }

    public SectorInfo getInformations() {
        return informations;
    }

    public void addRoom(ObjectId key, Room room) {
        rooms.put(key,room);
        ++currentSize;
    }

    public Collection<Room> getRooms() {
        return rooms.values();
    }

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
