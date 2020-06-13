package queue;

import network_structures.NetworkMessage;
import server.Guide;

import java.util.ArrayList;

public class TourGroup {

    private final static int maxTickets = 3;
    private final static int maxReservations = 1;
    private static final int maxPenaltyLevel = 2;
    private final ArrayList<QueueTicket> tickets;
    private final ArrayList<Room.Reservation> reservations;
    private int penaltyLevel = 0;

    private Room currentRoom;

    private final ArrayList<server.Guide> guides;
    private final static int maxGuides = 2;

    //private final ArrayList<SimpleGrouping> activeGroupings = new ArrayList<>();

    public TourGroup() {
        this.tickets = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.currentRoom = null;
        this.guides = new ArrayList<>();
    }

    /**
     * Increments current level of penalty induced for abandoning reservation for this group
     */
    public void increasePenaltyLevel() {
        if (penaltyLevel < maxPenaltyLevel)
            ++penaltyLevel;
    }

    public void decreasePenaltyLevel() {
        if (penaltyLevel > 0)
            --penaltyLevel;
    }

    /**
     * @return Current penalty level of this group
     */
    public int getCurrentPenaltyLevel() {
        return penaltyLevel;
    }

    /**
     * @return Array of Rooms for which this group has ticket
     */
    public Room[] getTicketRooms() {
        Room[] rooms = new Room[tickets.size()];
        for (int i = 0; i < rooms.length; ++i) {
            rooms[i] = tickets.get(i).getDestination();
        }
        return rooms;
    }

    public QueueTicket getTicketForRoom(Room room) {
        for (var ticket : tickets) {
            if (ticket.getDestination() == room) {
                return ticket;
            }
        }
        return null;
    }

    public boolean removeTicket(QueueTicket ticket) {
        if (ticket != null) {
            boolean ticketIsValid = false;
            for (QueueTicket t : tickets)
                if (ticket.equals(t)) {
                    ticketIsValid = true;
                    break;
                }
            if (ticketIsValid) {
                return tickets.remove(ticket);
            }
        }
        return false;
    }

    public int removeFromAllQueues() {
        int removedTickets = tickets.size();
        for (QueueTicket t : tickets) {
            t.getDestination().removeGroupFromQueue(t.getOwner());
        }
        return removedTickets;
    }

    /**
     * Adds guide to this group's guide list
     * @param guide Guide to add
     * @return true if guide has been added, false otherwise
     */
    public boolean addGuide(Guide guide) {
        if (guides.size() < maxGuides && guide != null) {
            try {
                guides.add(guide);
                return true;
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return false;
    }

    public boolean removeGuide(Guide guide) {
        try {
            return guides.remove(guide);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public void sendToAllGuides(NetworkMessage networkMessage) {
        for (Guide g : guides)
            g.addOutgoingMessage(networkMessage);
    }

    protected boolean canAddReservation() {
        return reservations.size() < maxReservations;
    }

    private boolean canAddTicket() {
        return tickets.size() < maxTickets - penaltyLevel;
    }

    public boolean hasTicketFor(Room room) {
        for (TourGroup.QueueTicket ticket : tickets) {
            if (ticket.destination == room)
                return true;
        }
        return false;
    }

    protected void addReservation(Room.Reservation reservation) {
        if (reservation != null)
            reservations.add(reservation);
    }

    protected void removeReservation(Room.Reservation reservation) {
        if (reservation != null)
            reservations.remove(reservation);
    }

    protected QueueTicket createTicket(Room destination) {
        if (destination != null) {
            QueueTicket ticket = new QueueTicket(this,destination);
            if (canAddTicket() && !hasTicketFor(destination) && tickets.add(ticket)) {
                return ticket;
            }
        }
        return null;
    }

    protected void setCurrentRoom(Room currentRoom) {
        if (currentRoom != null)
            this.currentRoom = currentRoom;
    }

    protected void setCurrentRoomNull() {
        this.currentRoom = null;
    }

    private enum GroupingResponses {
        DECLINED(-1),
        ACCEPTED(1),
        PENDING(0),
        UNAFFECTED(-2);

        private final int code;

        GroupingResponses(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

        public static GroupingResponses getValue(int code) {
            GroupingResponses response = UNAFFECTED;
            switch (code) {
                case -1:
                    response = DECLINED;
                    break;
                case 0:
                    response = PENDING;
                    break;
                case 1:
                    response = ACCEPTED;
                    break;
            }
            return response;
        }
    }

    //INNER CLASSES-------------------------------------------------------------------------------------------------

    public class QueueTicket {
        private final TourGroup owner;
        private final Room destination;
        private int timesAsked;
        private GroupingResponses groupingResponse;

        //private int groupingResponse; // (-2) - BRAK UDZIALU, (-1) - NIE, (0) - OCZEKIWANIE, (1) - TAK

        public QueueTicket(TourGroup owner, Room destination) {
            this.owner = owner;
            this.destination = destination;
            this.timesAsked = 0;
        }

        public TourGroup getOwner() {
            return owner;
        }

        protected GroupingResponses getGroupingResponse() {
            return groupingResponse;
        }

        public int getTimesAsked() {
            return timesAsked;
        }

        /**
         * Temporary, for removal
         * @return Name of this ticket's destination room
         */
        public String getRoomIdentifier() {
            return destination.getInfoFixed().getName();
        }

        public Room getDestination() {
            return this.destination;
        }

        protected void increaseTimesAsked() {
            ++timesAsked;
        }

        protected void sendNotificationAboutGrouping() {
            /*for (var guide : owner.guides) {
                guide.out.writeObject(...);
            }*/
            ///send notification about grouping to room
            groupingResponse = GroupingResponses.getValue(0); //poki co brak odpowiedzi
        }

        protected void setNoParticipation() {
            groupingResponse = GroupingResponses.getValue(-2);
        }

//        public void respondAboutGrouping(int response) {
//            groupingResponse = response;
//            destination.queue.updateFullyGroupedStatus();
//        }
    }
}
