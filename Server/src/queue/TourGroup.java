package queue;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class TourGroup {

    private ArrayList<QueueTicket> tickets;
    private final static int maxTickets = 3;
    private ArrayList<Room.Reservation> reservations;
    private final static int maxReservations = 1;

    private Room currentRoom;

    private ArrayList<server.Guide> guides;
    private final static int maxGuides = 2;

    public TourGroup() {
        this.tickets = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.currentRoom = null;
        this.guides = new ArrayList<>();
    }

    protected boolean canAddReservation() {
        return reservations.size() < maxReservations;
    }

    private boolean canAddTicket() {
        return tickets.size() < maxTickets;
    }

    private boolean hasTicketFor(Room room) {
        for (var ticket : tickets) {
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

    //INNER CLASSES-------------------------------------------------------------------------------------------------

    public class QueueTicket {
        private final TourGroup owner;
        private final Room destination;
        private int timesAsked;

        private int groupingResponse; // (-2) - BRAK UDZIALU, (-1) - NIE, (0) - OCZEKIWANIE, (1) - TAK

        public QueueTicket(TourGroup owner, Room destination) {
            this.owner = owner;
            this.destination = destination;
            this.timesAsked = 0;
        }

        protected TourGroup getOwner() {
            return owner;
        }

        protected int getGroupingResponse() {
            return groupingResponse;
        }

        public int getTimesAsked() {
            return timesAsked;
        }

        protected void increaseTimesAsked() {
            ++timesAsked;
        }

        protected void sendNotificationAboutGrouping() {
            /*for (var guide : owner.guides) {
                guide.out.writeObject(...);
            }*/
            ///send notification about grouping to room
            groupingResponse = 0; //poki co brak odpowiedzi
        }

        protected void setNoParticipation() {
            groupingResponse = -2;
        }

        public void respondAboutGrouping(int response) {
            groupingResponse = response;
            destination.queue.updateFullyGroupedStatus();
        }
    }
}
