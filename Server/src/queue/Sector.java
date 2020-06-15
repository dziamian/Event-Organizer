package queue;

import network_structures.SectorInfoFixed;
import network_structures.SectorInfoUpdate;
import org.bson.types.ObjectId;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class representing single sector at given event
 */
public class Sector {
    /** Immutable information about this sector */
    private final SectorInfoFixed infoFixed;
    /** Mutable information about this sector */
    private final SectorInfoUpdate infoUpdate;
    /** Map containing all rooms of this sector */
    private ConcurrentMap<ObjectId, Room> rooms;
    /** Total mount of rooms present within this sector */
    private int currentSize;

    /**
     * Creates sector with given parameters
     * @param id Database id of this sector
     * @param name Name of this sector
     * @param address Real address of this sector
     * @param description Description of this sector
     */
    public Sector(ObjectId id, String name, String address, String description) {
        this.infoUpdate = new SectorInfoUpdate(id);
        this.infoFixed = new SectorInfoFixed(id, name, address, description, this.infoUpdate.getActiveRoomsCount());
        this.rooms = new ConcurrentHashMap<>();
        this.currentSize = 0;
    }

    /**
     * Getter for immutable information about this sector
     * @return immutable information about this sector
     */
    public SectorInfoFixed getInfoFixed() {
        return infoFixed;
    }

    /**
     * Getter for mutable information about this sector
     * @return mutable information about this sector
     */
    public SectorInfoUpdate getInfoUpdate() { return infoUpdate; }

    /**
     * Adds given room to this sector
     * @param key Room's identifier
     * @param room Room to add
     */
    public void addRoom(ObjectId key, Room room) {
        rooms.put(key,room);
        infoFixed.getRooms().put(key, room.getInfoFixed());
        ++currentSize;
        infoUpdate.getRooms().put(key, room.getInfoUpdate());
        infoUpdate.setActiveRoomsCount(infoUpdate.getActiveRoomsCount().get()+1);
    }

    /**
     * Returns list of rooms contained within this sector
     * @return Collection of any type containing rooms from this sector
     */
    public Collection<Room> getRoomsValues() {
        return rooms.values();
    }

    /**
     * Getter for room map of this sector
     * @return Map containing all rooms of this sector
     */
    public Map<ObjectId, Room> getRooms() { return rooms; }

    /**
     * Shortcut for retrieving specific room from this sector's map
     * @param key ObjectId of desired room
     * @return Room with assigned ObjectId, or null if none found
     */
    public Room getRoom(ObjectId key) {
        return rooms.get(key);
    }
}
