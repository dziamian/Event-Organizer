package queue;

import network_structures.NetworkMessage;
import network_structures.RoomInfoFixed;
import network_structures.RoomInfoUpdate;
import org.bson.types.ObjectId;
import server.Server;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Room {

    private Sector parentSector;

    private RoomInfoFixed infoFixed;
    private RoomInfoUpdate infoUpdate;

    public enum State {
        OPEN("OPEN"),
        RESERVED("RESERVED"),
        TAKEN("TAKEN");

        private final String name;

        State(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private State state;
    private Date lastStateChange;

    private server.Presenter presenter;
    private ArrayList<TourGroup> currentVisitors;
    private ArrayList<Reservation> currentReservations;
    private int maxSlots;

    private static final int RESERVATIONS_UPDATE_CHECK_DELAY = 1000;
    protected ConcurrentRoomQueue queue;

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

    public RoomInfoFixed getInfoFixed() { return infoFixed; }

    public RoomInfoUpdate getInfoUpdate() {
        return infoUpdate;
    }

    public State getState() {
        return state;
    }

    public int getMaxSlots() {
        return maxSlots;
    }

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

    private void changeState(State state) {
        this.state = state;
        this.infoUpdate.setState(this.state.toString());
        this.lastStateChange = new Date();

        // if room's state is inactive then --parentSector.roomsActive;
    }

    private Reservation createReservation(TourGroup group) {
        if (group != null) {
            Reservation reservation = new Reservation(this, group);
            currentReservations.add(reservation);
            return reservation;
        }
        return null;
    }

    // fixme
    public void giveReservationsToAll() {
        if (queue.size() >= maxSlots) {
            for (int i = 0; i < maxSlots; ++i) {
                TourGroup group = queue.poll().getOwner();
                Reservation reservation = createReservation(group);
                group.addReservation(reservation);
                group.sendToAllGuides(new NetworkMessage(
                        "reservation",
                        new String[] { infoFixed.getSectorId().toString(), infoFixed.getSectorId().toString() },
                        reservation.getExpirationDate(),
                        0
                ));
                infoUpdate.setQueueSize(queue.size());
                changeState(State.RESERVED);
            }
        }
    }

    public Reservation[] updateReservationStatus() {
        ArrayList<Reservation> removedReservations = new ArrayList<>();
        for (Room.Reservation reservation : currentReservations) {
            if (!reservation.isActive() && reservation.expirationDate.getTime() < new Date().getTime()) {
                currentReservations.remove(reservation);
                reservation.getGroup().removeReservation(reservation);
                removedReservations.add(reservation);
            }
        }
        if (currentReservations.size() == 0 && state == State.RESERVED)
            changeState(State.OPEN);
        return (Reservation[])removedReservations.toArray();
    }

//    public void updateReservations() {
//        for (Room.Reservation reservation : currentReservations) {
//            if (!reservation.isActive() && reservation.expirationDate.getTime() < new Date().getTime()) {
//                currentReservations.remove(reservation);
//                reservation.getGroup().removeReservation(reservation);
//            }
//        }
//        // fixme
//        if (currentReservations.size() == 0 && state == State.RESERVED)
//            changeState(State.OPEN);
//    }
//
//    private boolean areReservationsValid() {
//        for (Room.Reservation reservation : currentReservations) {
//            if (!reservation.isActive())
//                return true;
//        }
//        return false;
//    }
//
//    private void launchReservationTimer() {
//        while (true) {
//            if (!areReservationsValid()) {
//                break;
//            }
//            updateReservations();
//
//            try { Thread.sleep(RESERVATIONS_UPDATE_CHECK_DELAY); } catch (InterruptedException e) { System.out.println(e.getMessage()); }
//        }
//
//        if (currentReservations.size() > 0) {
//            changeState(State.TAKEN);
//            for (Room.Reservation reservation : currentReservations) {
//                currentVisitors.add(reservation.group);
//                reservation.group.setCurrentRoom(this);
//            }
//        } else {
//            changeState(State.OPEN);
//            if (queue.requestedGrouping)
//                queue.tryGrouping();
//        }
//    }

    private boolean isEmpty() {
        return currentVisitors.size() == 0;
    }

//    public void removeVisitingGroup(TourGroup group) {
//        if (group != null) {
//            for (TourGroup visitor : currentVisitors) {
//                if (visitor == group) {
//                    currentVisitors.remove(visitor);
//                    visitor.setCurrentRoomNull();
//                }
//            }
//        }
//
//        if (isEmpty()) {
//            changeState(State.OPEN);
//            if (queue.requestedGrouping)
//                queue.tryGrouping();
//        }
//    }

    public int addGroupToQueue(TourGroup group) {
        TourGroup.QueueTicket queueTicket = group.createTicket(this);
        if (queueTicket != null) {
            this.queue.enqueue(queueTicket);
            this.infoUpdate.setQueueSize(infoUpdate.getQueueSize().get()+1);
            return this.queue.size();
        }
        return 0;
    }

    /**
     * Removes given group from this room's queue
     * @param group Group to remove
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

    public class Reservation {
        private final Room reservedRoom;
        private final TourGroup group;
        private final Date expirationDate;
        private final static long duration = 15 * 1000;// DEFAULT (5 min) : 5 * 60 * 1000;
        private boolean active;

        Reservation(Room reservedRoom, TourGroup group) {
            this.reservedRoom = reservedRoom;
            this.group = group;
            this.expirationDate = new Date(System.currentTimeMillis() + duration);
            this.active = false;
        }

        public TourGroup getGroup() {
            return group;
        }

        public boolean isActive() {
            return active;
        }

        public void activate() {
            active = true;
        }

        public Date getExpirationDate() {
            return expirationDate;
        }

        public RoomInfoFixed getReservedRoomInfoFixed() {
            return reservedRoom.getInfoFixed();
        }
    }

    class ConcurrentRoomQueue extends ConcurrentLinkedDeque<TourGroup.QueueTicket> {
        private static final int maxQuestions = 3;

        private final Room owner;

        private boolean requestedGrouping;
        private boolean isDuringGrouping;
        private boolean isFullyGrouped;

        protected ConcurrentRoomQueue(Room owner) {
            this.owner = owner;
            this.requestedGrouping = false;
            this.isDuringGrouping = false;
            this.isFullyGrouped = false;
        }

        public boolean enqueue(TourGroup.QueueTicket ticket) {
            if (isDuringGrouping && this.peekLast() != null) {
                this.peekLast().sendNotificationAboutGrouping();
            }
            return super.offer(ticket);
        }
    }
}
