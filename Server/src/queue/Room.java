package queue;

import network_structures.RoomInfoFixed;
import network_structures.RoomInfoUpdate;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

public class Room {

    private Sector parentSector;

    private RoomInfoFixed infoFixed;
    private RoomInfoUpdate infoUpdate;

    public enum State {
        OPEN,
        RESERVED,
        TAKEN
    }

    private State state;
    private Date lastStateChange;

    private server.Presenter presenter;
    private ArrayList<TourGroup> currentVisitors;
    private ArrayList<Reservation> currentReservations;
    private int maxSlots;

    private static final int RESERVATIONS_UPDATE_CHECK_DELAY = 1000;
    protected RoomQueue queue;

    public Room(ObjectId id, String name, String location, String description, int maxSlots, Sector parentSector) {
        this.infoUpdate = new RoomInfoUpdate(id);
        changeState(State.OPEN);
        this.infoFixed = new RoomInfoFixed(id, parentSector.getInfoFixed().getId(), name, location, description, this.infoUpdate.getState(), this.infoUpdate.getQueueSize());
        this.maxSlots = maxSlots;
        this.queue = new RoomQueue(this);
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

    private void changeState(State state) {
        this.state = state;
        this.infoUpdate.setState(this.state.toString());
        this.lastStateChange = new Date();

        // if room's state is inactive then --parentSector.roomsActive;
    }

    private Reservation createReservation(TourGroup group) {
        if (group != null) {
            Reservation reservation = new Reservation(this,group);
            currentReservations.add(reservation);
            return reservation;
        }
        return null;
    }

    private void updateReservations() {
        for (Room.Reservation reservation : currentReservations) {
            if (!reservation.isActive() && reservation.expirationDate.getTime() < new Date().getTime()) {
                currentReservations.remove(reservation);
                reservation.getGroup().removeReservation(reservation);
            }
        }
    }

    private boolean areReservationsValid() {
        for (Room.Reservation reservation : currentReservations) {
            if (!reservation.isActive())
                return true;
        }
        return false;
    }

    private void launchReservationTimer() {
        while (true) {
            if (!areReservationsValid()) {
                break;
            }
            updateReservations();

            try { Thread.sleep(RESERVATIONS_UPDATE_CHECK_DELAY); } catch (InterruptedException e) { System.out.println(e.getMessage()); }
        }

        if (currentReservations.size() > 0) {
            changeState(State.TAKEN);
            for (Room.Reservation reservation : currentReservations) {
                currentVisitors.add(reservation.group);
                reservation.group.setCurrentRoom(this);
            }
        } else {
            changeState(State.OPEN);
            if (queue.requestedGrouping)
                queue.tryGrouping();
        }
    }

    private boolean isEmpty() {
        return currentVisitors.size() == 0;
    }

    public void removeVisitingGroup(TourGroup group) {
        if (group != null) {
            for (TourGroup visitor : currentVisitors) {
                if (visitor == group) {
                    currentVisitors.remove(visitor);
                    visitor.setCurrentRoomNull();
                }
            }
        }

        if (isEmpty()) {
            changeState(State.OPEN);
            if (queue.requestedGrouping)
                queue.tryGrouping();
        }
    }

    public boolean addGroupToQueue(TourGroup group) {
        TourGroup.QueueTicket queueTicket = group.createTicket(this);
        if (queueTicket != null) {
            this.queue.enqueue(queueTicket);
            this.infoUpdate.setQueueSize(infoUpdate.getQueueSize().get()+1);
            return true;
        } else return false;
    }

    /**
     * Removes given group from this room's queue
     * @param group Group to remove
     */
    public void removeGroupFromQueue(TourGroup group) {

    }

    //INNER CLASSES-------------------------------------------------------------------------------------------------

    public class Reservation {
        private final Room reservedRoom;
        private final TourGroup group;
        private final Date expirationDate;
        private final static long duration = 5 * 60 * 1000;
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
    }

    protected class RoomQueue extends BasicQueue<TourGroup.QueueTicket> {

        private final Room owner;
        protected final static int maxAsksForTicket = 3;

        // DO SFORMATOWANIA KODZIK
        private boolean requestedGrouping;
        private boolean isDuringGrouping;
        private boolean isFullyGrouped;
        //

        private RoomQueue(Room owner) {
            this.owner = owner;
            this.isDuringGrouping = false;
            this.isFullyGrouped = false;
            this.requestedGrouping = false;
        }

        protected void updateFullyGroupedStatus() {
            this.isFullyGrouped = isFullyGrouped();
        }

        @Override
        public void enqueue(TourGroup.QueueTicket ticket) {
            super.enqueue(ticket);
            if (isDuringGrouping) {
                getTail().sendNotificationAboutGrouping();
                return;
            }
            tryGrouping();
        }

        private void sendGroupingStateInformation() {
            /// wyslij stany ich grupowania do wszystkich ktorzy biora udzial w grupowaniu
        }

        protected boolean isFullyGrouped() {
            int maxSlots = owner.maxSlots;
            int currentAccepted = 0;
            for (BasicQueue<TourGroup.QueueTicket>.Iterator iter = this.getIterator(); iter.isValid(); iter = iter.getNext()) {
                if (currentAccepted < maxSlots) {
                    int response = iter.getData().getGroupingResponse();
                    if (response == 0)
                        return false;
                    if (response == 1)
                        ++currentAccepted;
                }
                if (currentAccepted == maxSlots)
                    return true;
            }
            return false;
        }

        private boolean shouldDelete(TourGroup.QueueTicket ticket) {
            return ticket.getTimesAsked() == Room.RoomQueue.maxAsksForTicket;
        }

        private ArrayList<TourGroup> pullGroups() {
            int maxSlots = owner.maxSlots;
            int size = 0;
            ArrayList<TourGroup> groups = new ArrayList<>();
            for (BasicQueue<TourGroup.QueueTicket>.Iterator iter = this.getIterator(); iter.isValid(); iter = iter.getNext()) {
                if (size < maxSlots) {
                    TourGroup.QueueTicket ticket = iter.getData();
                    int response = ticket.getGroupingResponse();
                    if (response == -1 || response == 0) {
                        ticket.increaseTimesAsked();
                        if (shouldDelete(ticket))
                            removeFirstOccurrence(ticket); ///ZMIENIC NA REMOVETICKET!
                    }
                    else if (response == 1) {
                        groups.add(ticket.getOwner());
                        removeFirstOccurrence(ticket); ///ZMIENIC NA REMOVETICKET!
                        ++size;
                    }
                }
                if (size == maxSlots)
                    return groups;
            }
            return groups;
        }

        private void tryGrouping() {
            if (isDuringGrouping)
                return;
            if (owner.state == State.OPEN) {
                //////////////////////////////////////////////////////////// ustaw rozpoczecie grupowania (w nowym watku np)
                //new Thread(this::launchGrouping).start();
            } else {
                requestedGrouping = true;
            }
        }

        private void launchGrouping() {
            isDuringGrouping = true;

            for (BasicQueue<TourGroup.QueueTicket>.Iterator iter = this.getIterator(); iter.isValid(); iter = iter.getNext()) {
                TourGroup.QueueTicket ticket = iter.getData();
                if (ticket.getOwner().canAddReservation()) {
                    ticket.sendNotificationAboutGrouping();
                } else {
                    ticket.setNoParticipation();
                }
            }
            Date startedGroupingDate = new Date();
            long finish = startedGroupingDate.getTime() + 10 * 1000;

            /// czekaj na zakonczenie grupowania (zakonczenie czasu albo max liczba grup)
            while (finish > new Date().getTime() && !isFullyGrouped) {
                /// opoznienie co sekunde
                sendGroupingStateInformation();
            }

            requestedGrouping = false;
            isDuringGrouping = false;
            isFullyGrouped = false;

            /// stworz ewentualne rezerwacje dla tych co sie zgodzili
            ArrayList<TourGroup> groups = pullGroups();
            if (groups.size() > 0) {
                changeState(State.RESERVED);
                for (TourGroup group : groups) {
                    // co jesli bral udzial w innym grupowaniu i tez sie zgodzil 'w tym samym czasie'? (nie moze dostac dwoch rezerwacji!)
                    group.addReservation(createReservation(group));
                }
                owner.launchReservationTimer();
            }
        }
    }
}
