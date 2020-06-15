package server;

import com.mongodb.client.*;
import network_structures.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.*;
import java.net.*;
import java.util.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import queue.Sector;
import queue.Room;
import queue.TourGroup;

/**
 * Class responsible for running the server, including initialization of database connection,
 * loading event data from database, setting up queue structure and launching client connection listener.
 * Individual client connections are handled by {@link Client} subclasses, notably {@link Guide}.
 */
public class Server {
    /** Main server thread task queue */
    private static final ConcurrentLinkedQueue<Task> receivedTasks = new ConcurrentLinkedQueue<>();

    /** Collection containing all active client */
    private static final Set<Client> clients = new HashSet<>();
    /** Port to activate server on */
    private static final int port = 9999;

    /** Database connection */
    private static MongoDatabase database;

    /*
     * Recognized client requests:
     * 1. ping - check if server recognizes this client
     * 2. login - log in to server providing credentials in args
     * 3. event_info - request essential information about event, such as sectors / attractions list
     * 4. view_tickets - view my tickets and their states
     * 6. add_to_queue - add my ticket to specified room queue
     * 7. remove_from_queue - remove my ticket from specific queue
     * 9. update - request update on states of rooms and queues
     * 10. grouping - answer grouping call with decision or send update with changed decision
     */
    /** Array containing all requests recognized by server */
    private static final String[] recognizedCommands = new String[] {
        "ping",
        "login",
        "view_tickets",
        "add_to_queue",
        "remove_from_queue",
        "update",
    };

    /**
     * Checks if command is recognized by server. Recognized commands should be served in
     * {@link Server.MainServerTask#handleTask(Task)} and thus should never be responded with invalid command error.
     * @param command Command to check for server recognition
     * @return True if server recognizes this command, false otherwise
     */
    public static boolean isCommandRecognizedByServer(String command) {
        for (String s : recognizedCommands) {
            if (s.equals(command))
                return true;
        }
        return false;
    }

    /** Map containing all sectors used on this event */
    private static final ConcurrentMap<ObjectId, Sector> sectors = new ConcurrentHashMap<>();
    /** Immutable event information */
    private static final EventInfoFixed eventInfoFixed = new EventInfoFixed();
    /** Mutable event information */
    private static final EventInfoUpdate eventInfoUpdate = new EventInfoUpdate();

    /** Delay defining frequency for passive server to check for activation condition */
    private static final long DATE_CHECKING_DELAY = 1000;
    /** Date given event is starting - server will be in passive state until then */
    private static long eventStartingDate;

    /**
     * Server socket thread, launching system thread (main thread) and connecting with clients.
     * @param args Ignored
     */
    public static void main(String[] args) {
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);

        database = MongoClients.create().getDatabase("guideDB");

        serverSetup();

        File errorLogFile = new File("serverlog.txt");
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));


        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port " + serverSocket.getLocalPort() + "...\n");

            new Thread(new MainServerTask()).start();

            while (true) {
                new Thread(new ClientHandler(serverSocket.accept())).start();
            }

        } catch (IOException ex) {
            System.err.println("Server exception: " + ex.getMessage());
        }
    }

    /**
     * Getter for event information.
     * @return Event information
     */
    public static EventInfoFixed getEventInfoFixed() {
        return eventInfoFixed;
    }

    /**
     * Getter for mutable event information.
     * @return Mutable event information
     */
    public static EventInfoUpdate getEventInfoUpdate() {
        return eventInfoUpdate;
    }

    /**
     * Getter for database handle.
     * @return Database handle
     */
    public static MongoDatabase getDatabase() {
        return database;
    }

    /**
     * Enqueues task for completion by system thread.
     * @param task Task for main thread
     */
    public static void enqueueTask(Task task) {
        receivedTasks.offer(task);
    }

    /**
     * Method responsible for initializing critical server structures from database.
     */
    private static void serverSetup() {

        FindIterable<Document> sectorsIterator = database.getCollection("sectors").find();
        for (Document sectorIterator : sectorsIterator) {
            ObjectId sectorId = sectorIterator.getObjectId("_id");
            String sectorName = sectorIterator.getString("name");
            String sectorAddress = sectorIterator.getString("address");
            String sectorDescription = sectorIterator.getString("description");
            FindIterable<Document> roomsOfSector = database.getCollection("sector" + sectorId.toString()).find();
            Sector sector = new Sector(sectorId, sectorName, sectorAddress, sectorDescription);
            sectors.put(sectorId, sector);
            eventInfoFixed.getSectors().put(sectorId, sector.getInfoFixed());
            eventInfoUpdate.getSectors().put(sectorId, sector.getInfoUpdate());
            for (Document room : roomsOfSector) {
                ObjectId roomId = room.getObjectId("_id");
                String roomName = room.getString("name");
                String roomLocation = room.getString("location");
                String roomDescription = room.getString("description");
                Room newRoom = new Room(roomId, roomName, roomLocation, roomDescription, 1, sector);
                sector.addRoom(roomId, newRoom);
            }
        }

        MongoCollection<Document> serverVariablesCollection = database.getCollection("serverVariables");
        Document serverVariables = serverVariablesCollection.find().first();
        eventStartingDate = Objects.requireNonNull(serverVariables).getDate("serverOpenDate").getTime();
    }

    /**
     * Main server task running on separate thread, responsible for system management.
     */
    private static class MainServerTask implements Runnable {

        /**
         * Completes given task if possible, handles errors otherwise.
         * List of recognized commands can be found in {@link Server#recognizedCommands}.
         * @param task Task to complete
         */
        private void handleTask(Task task) {
            switch (task.getCommand()) {
                case "add_to_queue": {
                    clientRequestAddToQueue(task);
                } break;
                case "remove_from_queue": {
                    clientRequestRemoveFromQueue(task);
                } break;
                case "view_tickets": {
                    clientRequestViewTickets(task);
                } break;
                default : {
                    clientInvalidRequest(task);
                }
            }
        }

        /**
         * Method responsible for handling addition to room queue requests.
         * @param task {@link Task} containing request details
         */
        private static void clientRequestAddToQueue(Task task) {
            Room room = sectors.get(
                    new ObjectId(task.getArgs()[0])
            ).getRoom(
                    new ObjectId(task.getArgs()[1])
            );
            task.getResponseInterface().respond(new NetworkMessage(
                    "add_to_queue",
                    new String[] {
                            String.valueOf(room.addGroupToQueue((TourGroup)task.getData()))
                    },
                    null,
                    task.getCommunicationIdentifier())
            );
        }

        /**
         * Method responsible for handling queue removal requests.
         * @param task {@link Task} containing request details
         */
        private static void clientRequestRemoveFromQueue(Task task) {
            Room room = sectors.get(
                    new ObjectId(task.getArgs()[0])
            ).getRoom(
                    new ObjectId(task.getArgs()[1])
            );
            task.getResponseInterface().respond(new NetworkMessage(
                    "remove_from_queue",
                    new String[] {
                            "" + room.removeGroupFromQueue((TourGroup)task.getData())
                    },
                    null,
                    task.getCommunicationIdentifier()
            ));
        }

        /**
         * Method responsible for handling requests to view group's tickets.
         * @param task {@link Task} containing request details
         */
        private static void clientRequestViewTickets(Task task) {
            Room[] rooms = ((TourGroup)task.getData()).getTicketRooms();
            QueueInfo[] queueInfo = new QueueInfo[rooms.length];
            for (int i = 0; i < rooms.length; ++i) {
                queueInfo[i] = new QueueInfo(
                        rooms[i].getInfoFixed().getSectorId(),
                        rooms[i].getInfoFixed().getId(),
                        rooms[i].positionOf((TourGroup)task.getData()) + 1
                );
            }
            task.getResponseInterface().respond(new NetworkMessage(
                    "view_tickets",
                    null,
                    queueInfo,
                    task.getCommunicationIdentifier()
            ));
        }

        /**
         * Method responsible for handling invalid client requests.
         * @param task {@link Task} containing request details
         */
        private static void clientInvalidRequest(Task task) {
            task.getResponseInterface().respond(
                    new NetworkMessage(
                            "error",
                            new String[] { "invalid_command", task.getCommand() },
                            null,
                            task.getCommunicationIdentifier()
                    )
            );
        }

        /**
         * Main loop, launches if {@link Server#eventStartingDate} has been passed
         * and handles received tasks in infinite loop.
         */
        @Override
        public void run() {
            
            while (new Date().getTime() < eventStartingDate) {
                try { Thread.sleep(DATE_CHECKING_DELAY); } catch (InterruptedException e) { System.out.println(e.getMessage()); }
            }

            System.out.println("Event has started! All queues are open!");

            // Checking if every sector and room has been loaded properly
            for (Sector sector : sectors.values()) {
                System.out.println(sector.getInfoFixed().getName() + ":");
                for (Room room : sector.getRoomsValues()) {
                    System.out.println("\tRoom " + room.getInfoFixed().getName());
                    System.out.println("\t\tRoom State: " + room.getState());
                }
            }

            ReservationHandler reservationHandler = new ReservationHandler(sectors);
            new Thread(reservationHandler).start();

            // Main server task handling queue
            while (true) {
                Task task = receivedTasks.poll();
                if (task != null)
                    handleTask(task);
            }
        }
    }

    /**
     * Structure representing task given to main server thread, containing all the data necessary
     * to handle the task and respond to requester.
     */
    public static class Task extends BaseMessage {

        /** Interface representing client (or middleman) the response should be sent to */
        private final ClientHandler.RespondToClientInterface messageResponseInterface;

        /**
         * Constructs task based on given {@link BaseMessage}.
         * @param message Base message to copy content from
         * @param messageResponseInterface Client interface to respond to
         */
        public Task(BaseMessage message, ClientHandler.RespondToClientInterface messageResponseInterface) {
            super(message.getCommand(), message.getArgs(), message.getData(), message.getCommunicationIdentifier());
            this.messageResponseInterface = messageResponseInterface;
        }

        /**
         * Constructs task given exact parameters.
         * @param command Command for execution
         * @param args Command execution arguments (modifiers)
         * @param data Data needed to execute command
         * @param communicationIdentifier Communication
         * @param messageResponseInterface Interface for sending response(s) to
         */
        public Task(String command, String[] args, Object data, long communicationIdentifier, ClientHandler.RespondToClientInterface messageResponseInterface) {
            super(command, args, data, communicationIdentifier);
            this.messageResponseInterface = messageResponseInterface;
        }

        /**
         * Getter for client response interface.
         * @return Client response interface
         */
        public ClientHandler.RespondToClientInterface getResponseInterface() {
            return messageResponseInterface;
        }
    }

    /**
     * Client handler task running on separate thread, responsible for communication between client and main thread
     */
    private static class ClientHandler implements Runnable {
        /** Maximum time before client connection is aborted by server in milliseconds */
        public static final int TIMEOUT_MS = 15 * 60 * 1000;

        /** Socket connected to client */
        private final Socket socket;

        /**
         * @param socket Socket connected to client
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Information about specific client's socket for use by admin
         * @return String containing socket address, host name and port used
         */
        @Override
        public String toString() {
            return socket.getInetAddress().getHostName() + ":" + socket.getPort();
        }

        /**
         * Main loop responsible for assuring that IO routines for this client have been started
         */
        @Override
        public void run() {
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            Client client = null;
            Thread outputThread = null;
            try {
                System.out.println("Client (" + this.toString() + ") connected!");

                this.socket.setSoTimeout(TIMEOUT_MS);
                out = new ObjectOutputStream(this.socket.getOutputStream());
                in = new ObjectInputStream(this.socket.getInputStream());

                while (client == null) {
                    client = Client.createSpecifiedClient(out, in);
                }
                client.sendStartingData();
                clients.add(client);
                (outputThread = new Thread(client::handlingOutput)).start();
                client.handlingInput();
            } catch (SocketTimeoutException ex) {
                System.err.println("Client (" + this.toString() + ") is not responding!");
                if (client != null && outputThread != null) {
                    client.addOutgoingMessage(new NetworkMessage("timeout", null, null, 0));
                    client.stopOutputThread();
                    try {
                        outputThread.join();
                    } catch (InterruptedException e) {
                        System.err.println("[ClientHandler-SocketTimeoutException]: InterruptedException - " + ex.getMessage());
                    }
                } else if (out != null) {
                    try {
                        out.writeObject(new NetworkMessage("time_out", null, null, 0));
                    } catch (IOException e) {
                        System.err.println("[ClientHandler-SocketTimeoutException]: IOException - " + ex.getMessage());
                    }
                }
            } catch (EOFException ex) {
                System.err.println("Connection with client (" + this.toString() + ") has been lost!");
                if (client != null && outputThread != null) {
                    client.stopOutputThread();
                    try {
                        outputThread.join();
                    } catch (InterruptedException e) {
                        System.err.println("[ClientHandler-EOFException]: InterruptedException - " + ex.getMessage());
                    }
                }
            } catch (IOException ex) {
                System.err.println("[ClientHandler]: IOException - " + ex.getMessage());
            } finally {
                if (client != null) {
                    clients.remove(client);
                    client.removeFromSystem();
                }
                try {
                    socket.close();
                } catch(IOException ex) {
                    System.err.println("[ClientHandler-socket.close()]: IOException - " + ex.getMessage());
                }
                System.out.println("Connection with (" + this.toString() + ") has been closed!");
            }
        }

        /**
         * Interface for responding to client who provided this task to server
         */
        public interface RespondToClientInterface {
            void respond(NetworkMessage message);
        }
    }

    /**
     * Engine object responsible for assigning reservations to eligible touring groups during server operation.
     * It should be given access to server's sectors mapping and launched on separate thread <i>before</i> server begins
     * handling incoming requests.
     */
    private static class ReservationHandler implements Runnable {
        private final Map<ObjectId, Sector> sectors;
        private final AtomicBoolean continueRunning;

        /**
         * Creates new reservation handler for specified area.
         * @param sectors Sectors reservation handler will be responsible for
         */
        public ReservationHandler(Map<ObjectId, Sector> sectors) {
            continueRunning = new AtomicBoolean(true);
            this.sectors = sectors;
        }

        /**
         * Stops this reservation handler from running.
         */
        public void stop() {
            continueRunning.set(false);
        }

        /**
         * Main loop assigning reservations to eligible groups and updating information about rooms.
         */
        @Override
        public void run() {
            if (sectors == null)
                return;
            while (continueRunning.get()) {
                for (Sector s : sectors.values()) {
                    for (Room r : s.getRoomsValues()) {
                        switch (r.getState()) {
                            case OPEN: {
                                r.giveReservationsToAll();
                            } break;
                            case RESERVED: {
                                Room.Reservation[] expiredReservations = r.updateReservationStatus();
                                for (Room.Reservation reservation : expiredReservations)
                                    reservation.getGroup().sendToAllGuides(new NetworkMessage(
                                            "reservation_expired",
                                            new String[] {
                                                    reservation.getReservedRoomInfoFixed().getSectorId().toString(),
                                                    reservation.getReservedRoomInfoFixed().getId().toString()
                                            },
                                            null,
                                            0
                                    ));
                            } break;
                            case TAKEN: {

                            } break;
                        }
                    }
                }
            }
        }
    }
}
