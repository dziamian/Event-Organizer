package queue;

import java.util.ArrayList;
import java.util.Date;

public class Room {

    private String name;
    private String description;

    public enum State {
        OPEN,
        RESERVED,
        TAKEN
    }

    private State state;
    private Date stateChanged;

    private server.Presenter presenter;
    private ArrayList<TourGroup> currentVisitors;
    private ArrayList<Reservation> currentReservations;
    private int maxSlots;

    protected RoomQueue queue;

    public Room(String name, String description, int maxSlots) {
        this.name = name;
        this.description = description;
        this.maxSlots = maxSlots;
        this.queue = new RoomQueue(this);
        setState(State.OPEN);
        this.currentVisitors = new ArrayList<>();
        this.currentReservations = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        this.state = state;
        stateChanged = new Date();
    }

    private Reservation createReservation(TourGroup group) {
        if (group != null) {
            Reservation reservation = new Reservation(this,group);
            currentReservations.add(reservation);
            return reservation;
        }
        return null;
    }

    private void checkReservations() {
        for (var reservation : currentReservations) {
            if (!reservation.hasBeenUsed() && reservation.expirationDate.getTime() < new Date().getTime()) {
                currentReservations.remove(reservation);
                reservation.getGroup().removeReservation(reservation);
            }
        }
    }

    private boolean areReservationsValid() {
        checkReservations();
        for (var reservation : currentReservations) {
            if (!reservation.hasBeenUsed())
                return true;
        }
        return false;
    }

    private void reservationsTimer() {
        while (areReservationsValid()) {
            //////////////////// opoznienie sekunde moze?
        }

        if (currentReservations.size() > 0) {
            setState(State.TAKEN);
            for (var reservation : currentReservations) {
                currentVisitors.add(reservation.group);
                reservation.group.setCurrentRoom(this);
            }
        } else {
            setState(State.OPEN);
            if (queue.requestedGrouping)
                queue.tryStartGrouping();
        }
    }

    private boolean checkIfRoomIsEmpty() {
        return currentVisitors.size() == 0;
    }

    public void groupHasLeft(TourGroup group) {
        if (group != null) {
            for (var visitor : currentVisitors) {
                if (visitor == group) {
                    currentVisitors.remove(visitor);
                    visitor.setCurrentRoomNull();
                }
            }
        }
        if (checkIfRoomIsEmpty()) {
            setState(State.OPEN);
            if (queue.requestedGrouping)
                queue.tryStartGrouping();
        }
    }

    public void addToQueue(TourGroup group) {
        TourGroup.QueueTicket queueTicket = group.createTicket(this);
        if (queueTicket != null)
            queue.enqueue(queueTicket);
    }

    //INNER CLASSES-------------------------------------------------------------------------------------------------

    public class Reservation {
        private final Room reservedRoom;
        private final TourGroup group;
        private final Date expirationDate;
        private final static long duration = 5 * 60 * 1000;
        private boolean hasBeenUsed;

        Reservation(Room reservedRoom, TourGroup group) {
            this.reservedRoom = reservedRoom;
            this.group = group;
            this.expirationDate = new Date(System.currentTimeMillis() + duration);
            this.hasBeenUsed = false;
        }

        public TourGroup getGroup() {
            return group;
        }

        public boolean hasBeenUsed() {
            return hasBeenUsed;
        }

        public void groupHasJoined() {
            hasBeenUsed = true;
        }
    }

    protected class RoomQueue extends BasicQueue<TourGroup.QueueTicket> {

        private final Room owner;
        protected final static int maxAsksForTicket = 3;
        private boolean requestedGrouping;
        private boolean isDuringGrouping;
        private boolean isFullyGrouped;

        private RoomQueue(Room owner) {
            this.owner = owner;
            this.isDuringGrouping = false;
            this.isFullyGrouped = false;
            this.requestedGrouping = false;
        }

        protected void setFullyGrouped() {
            this.isFullyGrouped = isFullyGrouped();
        }

        @Override
        public void enqueue(TourGroup.QueueTicket ticket) {
            super.enqueue(ticket);
            if (!isDuringGrouping)
                tryStartGrouping();
            else
                getTail().sendNotificationAboutGrouping();
        }

        private void stateChanged() {
            if (!isDuringGrouping)
                tryStartGrouping();
        }

        private void sendGroupingData() {
            /// wyslij stany odpowiedzi do wszystkich ktorzy biora udzial w grupowaniu
        }

        protected boolean isFullyGrouped() {
            int maxSlots = owner.maxSlots;
            int currentAccepted = 0;
            for (var iter = this.getIterator(); iter.isValid(); iter = iter.getNext()) {
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

        private ArrayList<TourGroup> pollGroups() {
            int maxSlots = owner.maxSlots;
            int size = 0;
            ArrayList<TourGroup> groups = new ArrayList<>();
            for (var iter = this.getIterator(); iter.isValid(); iter = iter.getNext()) {
                if (size < maxSlots) {
                    TourGroup.QueueTicket ticket = iter.getData();
                    int response = ticket.getGroupingResponse();
                    if (response == -1 || response == 0) {
                        ticket.increaseTimesAsked();
                        if (ticket.shouldDelete())
                            removeFirstOccurrence(ticket); ///ZMIENIC NA REMOVETICKET!
                    }
                    else if (response == 1) {
                        groups.add(ticket.getOwner());
                        removeFirstOccurrence(ticket); ///ZMIENIC NA REMOVETICKET!
                    }
                }
                if (size == maxSlots)
                    return groups;
            }
            return groups;
        }

        private void tryStartGrouping() {
            if (owner.state == State.OPEN) {
                //////////////////////////////////////////////////////////// ustaw rozpoczecie grupowania (w nowym watku np)
                new Thread(this::startGrouping).start();
            } else {
                requestedGrouping = true;
            }
        }

        private void startGrouping() {
            isDuringGrouping = true;

            for (var iter = this.getIterator(); iter.isValid(); iter = iter.getNext()) {
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
                sendGroupingData();
            }

            requestedGrouping = false;
            isDuringGrouping = false;
            isFullyGrouped = false;

            /// stworz ewentualne rezerwacje dla tych co sie zgodzili
            ArrayList<TourGroup> groups = pollGroups();
            if (groups.size() > 0) {
                setState(State.RESERVED);
                for (var group : groups) {
                    group.addReservation(createReservation(group));
                }
                owner.reservationsTimer();
            }
        }
    }
}
