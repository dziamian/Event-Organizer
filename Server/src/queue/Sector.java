package queue;

import network_structures.SectorInfo;
import network_structures.SectorUpdate;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class Sector {

    public SectorInfo informations;

    private Map<ObjectId, Room> rooms;
    private int currentSize;

    private SectorUpdate update;

    public Sector(ObjectId id, String name, String address, String description) {
        this.informations = new SectorInfo(id, name, address, description);
        this.update = new SectorUpdate(id);
        this.rooms = new TreeMap<>();
        this.currentSize = 0;
    }

    public SectorInfo getInformations() {
        return informations;
    }

    public SectorUpdate getUpdate() { return update; }

    public void addRoom(ObjectId key, Room room) {
        rooms.put(key,room);
        ++currentSize;
        // JEŻELI STATE OZNACZA AKTYWNY TO ++
        ++update.currentActive;
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
