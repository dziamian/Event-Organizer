package queue;

import network_structures.NetworkMessage;
import network_structures.ReservationInfo;
import network_structures.RoomInfoFixed;
import network_structures.RoomInfoUpdate;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Class representing single room (or other type of attraction) on the event, capable of providing
 * entertainment for visiting groups. Rooms cannot be accessed by groups directly, instead forcing
 * them to rely on queue system to reserve a slot for next show.
 */
public class Room {
    /** Sector this room belongs to */
    private Sector parentSector;
    /** Immutable information about this room */
    private RoomInfoFixed infoFixed;
    /** Mutable information about this room */
    private RoomInfoUpdate infoUpdate;

    /**
     * Enum describing possible states of this room
     */
    public enum State {
        /** The room is open and can accept visitors anytime */
        OPEN("OPEN"),
        /** The room has been reserved and will only allow groups with reservations to come in */
        RESERVED("RESERVED"),
        /** The room is occupied and cannot be entered */
        TAKEN("TAKEN");

        private final String name;

        State(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    /** Current state of this room */
    private State state;
    /** Date of last room state change */
    private Date lastStateChange;

    /** Presenter of this room */
    private server.Presenter presenter;
    /** Current visitors of this room */
    private ArrayList<TourGroup> currentVisitors;
    /** Currently active reservations for this room */
    private ArrayList<Reservation> currentReservations;
    /** Maximum amount of groups this room can contain at once */
    private int maxSlots;

    /** Ticket queue for this room */
    protected ConcurrentRoomQueue queue;

    /**
     * Creates new room with given parameters
     * @param id Room identifier within database
     * @param name Name of this room
     * @param location Location of this room
     * @param description Description of this room
     * @param maxSlots Maximum amount of groups this room can hold
     * @param parentSector Sector this room belongs to
     */
    public Room(ObjectId id, String name, String location, String description, int maxSlots, Sector parentSector) {
        this.infoUpdate = new RoomInfoUpdate(id);
        changeState(State.OPEN);
        this.infoFixed = new RoomInfoFixed(id, parentSector.getInfoFixed().getId(), name, location, description, this.infoUpdate.getState(), this.infoUpdate.getQueueSize());
        this.maxSlots = maxSlots;
        this.queue = new ConcurrentRoomQueue(this);
        this.currentVisitors = new ArrayList<>();
        this.currentReservations = new ArrayList<>();
        this.parentSector = parentSector;
    }

    /**
     * Getter for immutable information about this room, see {@link RoomInfoFixed}.
     * @return Immutable information about this room
     */
    public RoomInfoFixed getInfoFixed() { return infoFixed; }

    /**
     * Getter for mutable information about this room, see {@link RoomInfoUpdate}.
     * @return Mutable information about this room
     */
    public RoomInfoUpdate getInfoUpdate() {
        return infoUpdate;
    }

    /**
     * Getter for this room's state, see {@link Room.State}.
     * @return Current state of this room
     */
    public State getState() {
        return state;
    }

    /**
     * Getter for maximum slot amount for this room.
     * @return Maximum slots
     */
    public int getMaxSlots() {
        return maxSlots;
    }

    /**
     * Checks specified group's position within this room's queue. The group must possess {@link TourGroup.QueueTicket} for this room.
     * @param group Group to check
     * @return Position of this group within this room's queue, or -1 if given group is not enqueued for this room
     */
    public int positionOf(TourGroup group) {
        if (group != null) {
            int position = 0;
            for (Iterator<TourGroup.QueueTicket> it = queue.iterator(); it.hasNext();) {
                if (it.next().getOwner() == group)
                    return position;
                ++position;
            }
        }
        return -1;
    }

    /**
     * Updates this room's state to specified one
     * @param state New state of this room, as in {@link Room.State}
     */
    private void changeState(State state) {
        this.state = state;
        this.infoUpdate.setState(this.state.toString());
        this.lastStateChange = new Date();
    }

    /**
     * Creates reservation to this room for given group
     * @param group Group to create reservation for
     * @return New reservation
     */
    private Reservation createReservation(TourGroup group) {
        if (group != null) {
            Reservation reservation = new Reservation(this, group);
            currentReservations.add(reservation);
            return reservation;
        }
        return null;
    }

    /**
     * Handles reservations amount to groups from queue equal to amount of available slots within this room.
     * This method will be called, but do nothing if there are not enough groups in the queue to fill the room.
     */
    public void giveReservationsToAll() {
        if (queue.size() >= maxSlots) {
            int givenReservations = 0;
            for (Iterator<TourGroup.QueueTicket> iter = queue.iterator(); iter.hasNext();) {
                if (givenReservations == maxSlots) {
                    break;
                }
                TourGroup.QueueTicket ticket = iter.next();
                TourGroup group = ticket.getOwner();
                if (group.canAddReservation()) {
                    iter.remove();
                    Reservation reservation = createReservation(group);
                    group.addReservation(reservation);
                    group.sendToAllGuides(new NetworkMessage(
                            "reservation",
                            new String[0],
                            new ReservationInfo(
                                    infoFixed.getSectorId(),
                                    infoFixed.getId(),
                                    reservation.getExpirationDate()
                            ),
                            0
                    ));
                    infoUpdate.setQueueSize(queue.size());
                    changeState(State.RESERVED);
                    group.removeTicket(group.getTicketForRoom(this));
                    ++givenReservations;
                }
            }
        }
    }

    /**
     * Updates this room's reservations, removing outdated ones.
     * @return Array containing all removed reservations - the size can be 0 if none were removed
     */
    public Reservation[] updateReservationStatus() {
        ArrayList<Reservation> removedReservations = new ArrayList<>();
        for (Iterator<Reservation> it = currentReservations.iterator(); it.hasNext();) {
            Reservation reservation = it.next();
            if (!reservation.isActive() && reservation.expirationDate.getTime() < new Date().getTime()) {
                it.remove();
                reservation.getGroup().removeReservation(reservation);
                removedReservations.add(reservation);
            }
        }
        if (currentReservations.size() == 0 && state == State.RESERVED) {
            changeState(State.OPEN);
        }
        return removedReservations.toArray(new Reservation[0]);
    }

    /**
     * Checks whether room has any visitors.
     * @return True if there are groups within this room, false otherwise
     */
    private boolean isEmpty() {
        return currentVisitors.size() == 0;
    }

    /**
     * Adds given group to this room's queue.
     * @param group Group added to queue
     * @return Group's position within the queue, or zero if it could not be added
     */
    public int addGroupToQueue(TourGroup group) {
        if (!group.hasReservationFor(this)) {
            TourGroup.QueueTicket queueTicket = group.createTicket(this);
            if (queueTicket != null) {
                this.queue.enqueue(queueTicket);
                this.infoUpdate.setQueueSize(infoUpdate.getQueueSize().get() + 1);
                return this.queue.size();
            }
        }
        return 0;
    }

    /**
     * Removes given group from this room's queue.
     * @param group Group to remove
     * @return True if the group has been removed successfully, false otherwise
     */
    public boolean removeGroupFromQueue(TourGroup group) {
        if (group != null) {
            TourGroup.QueueTicket ticket = group.getTicketForRoom(this);
            if (ticket != null) {
                group.removeTicket(ticket);
                this.infoUpdate.setQueueSize(this.infoUpdate.getQueueSize().get() - 1);
                return queue.removeFirstOccurrence(ticket);
            }
        }
        return false;
    }

    //INNER CLASSES-------------------------------------------------------------------------------------------------

    /**
     * Class representing group's ownership of slot for next show within this room.
     */
    public class Reservation {
        private final Room reservedRoom;
        private final TourGroup group;
        private final Date expirationDate;
        private final static long DURATION = 30 * 1000;// DEFAULT (5 min) : 5 * 60 * 1000;
        private boolean active;

        /**
         * Creates reservation to given room for specified group.
         * @param reservedRoom Room reservation is for
         * @param group Owner of this reservation
         */
        Reservation(Room reservedRoom, TourGroup group) {
            this.reservedRoom = reservedRoom;
            this.group = group;
            this.expirationDate = new Date(System.currentTimeMillis() + DURATION);
            this.active = false;
        }

        /**
         * Getter for owning group of this reservation, see {@link TourGroup}.
         * @return Owner
         */
        public TourGroup getGroup() {
            return group;
        }

        /**
         * Checks whether or not the reservation is active. Reservation can be considered active
         * if it's expiration date has not come yet.
         * @return Status of this reservation
         */
        public boolean isActive() {
            return active;
        }

        /**
         * Reactivates this reservation, regardless of it's expiration date.
         */
        public void activate() {
            active = true;
        }

        /**
         * Getter for expiration date of this reservation.
         * @return Expiration date
         */
        public Date getExpirationDate() {
            return expirationDate;
        }

        /**
         * Getter for room reserved by this reservation, see {@link Room}.
         * @return Room reserved
         */
        public Room getReservedRoom() { return reservedRoom; }

        /**
         * Shortcut for getting reserved room's immutable, see {@link RoomInfoFixed}.
         * @return Reserved room immutable information
         */
        public RoomInfoFixed getReservedRoomInfoFixed() {
            return reservedRoom.getInfoFixed();
        }
    }

    /**
     * Concurrent queue containing tickets connecting this room with groups willing to reserve a slot.
     */
    class ConcurrentRoomQueue extends ConcurrentLinkedDeque<TourGroup.QueueTicket> {
        /** Room owning this queue */
        private final Room owner;

        /**
         * Creates new queue for given room.
         * @param owner Owner of this queue
         */
        protected ConcurrentRoomQueue(Room owner) {
            this.owner = owner;
        }

        /**
         * Enqueue given ticket into this queue.
         * The ticket has to be valid construct returned by {@link TourGroup#createTicket(Room room)}, where room is owner of this queue.
         * @param ticket Ticket to enqueue
         * @return True if successfully enqueued, false otherwise
         */
        public boolean enqueue(TourGroup.QueueTicket ticket) {
            if (ticket != null && ticket.getOwner() != null && ticket.getDestination() == this.owner)
                return super.offer(ticket);
            return false;
        }
    }
}
