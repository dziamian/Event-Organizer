package queue;

import network_structures.NetworkMessage;
import server.Guide;
import java.util.ArrayList;

/**
 * Class representing specific event touring group, used as a basic queue system resolution unit.
 */
public class TourGroup {

    /** Maximum amount of tickets a group can have at once */
    private final static int maxTickets = 3;
    /** Maximum amount of reservations a group can have at once */
    private final static int maxReservations = 1;
    /** Tickets possessed by this group */
    private final ArrayList<QueueTicket> tickets;
    /** Reservations possessed by this group */
    private final ArrayList<Room.Reservation> reservations;
    /** List of guides currently assigned to this group */
    private final ArrayList<server.Guide> guides;
    /** Maximum amount of guides a group can have at once */
    private final static int maxGuides = 2;

    /**
     * Creates default group with no members
     */
    public TourGroup() {
        this.tickets = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.guides = new ArrayList<>();
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

    /**
     * Returns this group's ticket for given room
     * @param room Room to get ticket for
     * @return Ticket for given room, or null if none was found
     */
    public QueueTicket getTicketForRoom(Room room) {
        for (QueueTicket ticket : tickets) {
            if (ticket.getDestination() == room) {
                return ticket;
            }
        }
        return null;
    }

    /**
     * Safely removes provided ticket from this group's ticket list
     * @param ticket Ticket to remove
     * @return True if ticket has been successfully removed, false otherwise
     */
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

    /**
     * Safely removes this group from all their active queues
     * @return Amount of queues the group has been removed from
     */
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
        if (guides.size() < maxGuides && guide != null && !hasThisGuide(guide)) {
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

    /**
     * Checks if specified guide is member of this touring group's guides
     * @param guide Guide to check
     * @return True if guide is on this group's guide list, false otherwise
     */
    public boolean hasThisGuide(Guide guide) {
        if (guide != null) {
            for (Guide g : guides) {
                if (guide.equals(g))
                    return true;
            }
        }
        return false;
    }

    /**
     * Safely removes specified guide from this group's guide list
     * @param guide Guide to remove
     * @return True if guide has been successfully removed, false otherwise
     */
    public boolean removeGuide(Guide guide) {
        try {
            return guides.remove(guide);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    /**
     * Sends given message to all gudes of this group
     * @param networkMessage Message to send
     */
    public void sendToAllGuides(NetworkMessage networkMessage) {
        for (Guide g : guides)
            g.addOutgoingMessage(networkMessage);
    }

    /**
     * Checks if this group can accept another reservation
     * @return True if group can accept reservation, false otherwise
     */
    protected boolean canAddReservation() {
        return reservations.size() < maxReservations;
    }

    /**
     * Checks if this group can request another ticket
     * @return True if group can request ticket, false otherwise
     */
    private boolean canAddTicket() {
        return tickets.size() < maxTickets;
    }

    /**
     * Checks if this group has ticket, and thus is waiting in queue, for given room
     * @param room Room to check
     * @return True if group has ticket for this room, false otherwise
     */
    public boolean hasTicketFor(Room room) {
        for (TourGroup.QueueTicket ticket : tickets) {
            if (ticket.destination == room)
                return true;
        }
        return false;
    }

    /**
     * Checks if this group has reservation for given room
     * @param room Room to check
     * @return True if group has reservation for this room, false otherwise
     */
    public boolean hasReservationFor(Room room) {
        for (Room.Reservation reservation : reservations) {
            if (reservation.getReservedRoom() == room) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds reservation for this group
     * @param reservation Reservation to add
     */
    protected void addReservation(Room.Reservation reservation) {
        if (reservation != null)
            reservations.add(reservation);
    }

    /**
     * Removes this group's reservation
     * @param reservation Reservation to remove
     */
    protected void removeReservation(Room.Reservation reservation) {
        if (reservation != null)
            reservations.remove(reservation);
    }

    /**
     * Creates ticket to specified room for this group
     * @param destination Ticket destination room
     * @return New ticket for given room and this group
     */
    protected QueueTicket createTicket(Room destination) {
        if (destination != null) {
            QueueTicket ticket = new QueueTicket(this,destination);
            if (canAddTicket() && !hasTicketFor(destination) && tickets.add(ticket)) {
                return ticket;
            }
        }
        return null;
    }

    /**
     * Class representing single ticket (a place in room's queue) of given touring group
     */
    public class QueueTicket {
        /** Group owning this ticket */
        private final TourGroup owner;
        /** Room this ticket is for */
        private final Room destination;

        /**
         * Creates ticket for this group bound to given room
         * @param owner Group owning this ticket
         * @param destination Room this ticket is for
         */
        public QueueTicket(TourGroup owner, Room destination) {
            this.owner = owner;
            this.destination = destination;
        }

        /**
         * Getter for owner of this ticket
         * @return Owner of this ticket
         */
        public TourGroup getOwner() {
            return owner;
        }

        /**
         * Getter for destination of this ticket
         * @return Destination room of this ticket
         */
        public Room getDestination() {
            return this.destination;
        }
    }
}
