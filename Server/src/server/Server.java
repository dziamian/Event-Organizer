package server;

import com.mongodb.client.*;
import network_structures.BaseMessage;
import network_structures.EventInfoFixed;
import network_structures.EventInfoUpdate;
import network_structures.NetworkMessage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.*;
import java.net.*;
import java.util.*;

// Structure for task queues
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.logging.Level;
import java.util.logging.Logger;

import queue.Sector;
import queue.Room;
import queue.TourGroup;

public class Server {
    /** Main server thread task queue */
    private static final ConcurrentLinkedQueue<Task> receivedTasks = new ConcurrentLinkedQueue<>();

    /** Collection containing all active client */
    private static final Set<Client> clients = new HashSet<>();
    /** Port to activate server on */
    private static final int port = 9999;

    /** Database connection */
    private static MongoDatabase database;

    // private static MongoClient mongoClient;

    private final static Map<ObjectId, Sector> sectors = new TreeMap<>();
    //
    private static final EventInfoFixed eventInfoFixed = new EventInfoFixed();
    //
    private static final EventInfoUpdate eventInfoUpdate = new EventInfoUpdate();

    /// Delay defining frequency for passive server to check for activation condition
    private static final long DATE_CHECKING_DELAY = 1000;
    /// Date given event is starting - server will be in passive state until then
    private static long eventStartingDate;
    /// Server state flag
    private static boolean eventHasStarted = false;

    /**
     * Server socket thread, launching system thread (main thread) and connecting with clients
     * @param args Unused currently
     */
    public static void main(String[] args) {

        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);

        database = MongoClients.create().getDatabase("guideDB");

        serverSetup();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port " + serverSocket.getLocalPort() + "...\n");

            new Thread(new MainServerTask()).start();

            while (true) {
                new Thread(new ClientHandler(serverSocket.accept())).start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
        }
    }

    /**
     * Getter for event information
     * @return Event information
     */
    public static EventInfoFixed getEventInfoFixed() {
        return eventInfoFixed;
    }

    public static EventInfoUpdate getEventInfoUpdate() {
        return eventInfoUpdate;
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    /**
     * Enqueues task for completion by system thread
     * @param task Task for main thread
     */
    public static void enqueueTask(Task task) {
        receivedTasks.offer(task);
    }

    /**
     * Initializes server
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
     * Main server task running on separate thread, responsible for systeme management
     */
    private static class MainServerTask implements Runnable {

        /**
         * Completes given task if possible, handles errors otherwise
         * @param task Task to complete
         */
        private void handleTask(Task task) {
            /*
             * Recognized client requests:
             * 1. ping - check if server recognizes this client
             * 2. login - log in to server providing credentials in args
             * 3. eventInfo - request essential information about event, such as sectors / attractions list
             * 4. viewTickets - view my tickets and their states
             * 5. viewReservations - view my active reservation(s)
             * 6. addTicket - add my ticket to specified room queue
             * 7. removeTicket - remove specific one of my tickets
             * 8. abandonReservation - abandon one of my reservations (will result in penalty)
             * 9. update - request update on states of rooms and queues
             * 10. details - request detailed information about specific room
             * 11. grouping - answer grouping call with decision or send update with changed decision
             */
            switch (task.getCommand()) {
                case "ping": {
                    task.getResponseInterface().respond(new NetworkMessage(
                        "ping",
                        null,
                        null,
                        task.getCommunicationIdentifier()
                    ));
                } break;
                case "eventInfo": {
                    task.getResponseInterface().respond(new NetworkMessage(
                        "eventInfo",
                        null,
                        Server.getEventInfoFixed(),
                        task.getCommunicationIdentifier()
                    ));
                } break;
                case "viewTickets": {
                    task.getResponseInterface().respond(new NetworkMessage(
                        "viewTickets",
                        null,
                        null,
                        task.getCommunicationIdentifier()
                    ));
                } break;
                case "viewReservations": {
                    task.getResponseInterface().respond(new NetworkMessage(
                        "viewReservations",
                        null,
                        null, // todo
                        task.getCommunicationIdentifier()
                    ));
                } break;
                case "addTicket": {
                    Room room = Server.sectors.get(task.getArgs()[0])
                            .getRoomsMapping().get(task.getArgs()[1]);
                    if (room != null) {
                        task.getResponseInterface().respond(new NetworkMessage(
                                "addTicket",
                                new String[] { "success" },
                                null,
                                task.getCommunicationIdentifier()
                        ));
                    }
                    else {
                        task.getResponseInterface().respond(new NetworkMessage(
                                "error",
                                new String[]{"invalidRoomIdentifier"},
                                null,
                                task.getCommunicationIdentifier()
                        ));
                    }
                } break;
                case "removeTicket": {
                    Room room = sectors.get(new ObjectId(task.getArgs()[0]))
                            .getRoomsMapping().get(new ObjectId(task.getArgs()[1]));
                    if (room != null) {
                        room.removeGroupFromQueue(((TourGroup.QueueTicket)task.getData()).getOwner());
                        task.getResponseInterface().respond(new NetworkMessage(
                           "removeTicket",
                            new String[] { "success" },
                            null,
                            task.getCommunicationIdentifier()
                        ));
                    }
                    else {
                        task.getResponseInterface().respond(new NetworkMessage(
                           "error",
                           new String[] { "ticketNotFound" },
                           null,
                           task.getCommunicationIdentifier()
                        ));
                    }
                } break;
                case "abandonReservation": {
                    Room room = sectors.get(new ObjectId(task.getArgs()[0]))
                            .getRoomsMapping().get(new ObjectId(task.getArgs()[1]));
                    room.removeVisitingGroup(((TourGroup.QueueTicket)task.getData()).getOwner());
                    ((TourGroup.QueueTicket)task.getData()).getOwner().increasePenalty();
                } break;
                case "update": {
                    task.getResponseInterface().respond(new NetworkMessage(
                            "update",
                            null,
                            sectors.get(new ObjectId(task.getArgs()[0])).getInfoFixed(),
                            task.getCommunicationIdentifier()
                    ));
                } break;
                case "details": {
                    task.getResponseInterface().respond(new NetworkMessage(
                        "details",
                            null,
                            sectors
                                    .get(new ObjectId(task.getArgs()[0]))
                                    .getRoomsMapping()
                                    .get(new ObjectId(task.getArgs()[1]))
                                    .getState(),
                            task.getCommunicationIdentifier()
                    ));
                } break;
                case "grouping": {
                    
                } break;
                default: {
                    task.getResponseInterface().respond(new NetworkMessage(
                            "error",
                            new String[] { "invalidCommand" },
                            task.getCommand(),
                            task.getCommunicationIdentifier()
                    ));
                }
            }
        }

        /**
         * Main loop
         */
        @Override
        public void run() {
            
            while (new Date().getTime() < eventStartingDate) {
                try { Thread.sleep(DATE_CHECKING_DELAY); } catch (InterruptedException e) { System.out.println(e.getMessage()); }
            }

            eventHasStarted = true;
            System.out.println("Event has started! All queues are open!");

            // Checking if every sector and room has been loaded properly //
            for (Sector sector : sectors.values()) {
                System.out.println(sector.getInfoFixed().getName() + ":");
                for (Room room : sector.getRooms()) {
                    System.out.println("\tRoom " + room.getInfoFixed().getName());
                    System.out.println("\t\tRoom State: " + room.getState());
                }
            }

            // Main server task queue
            /*while (true) {
                Task task = receivedTasks.poll();
                if (task != null) {
                    handleTask(task);
                }
            }*/
        }
    }

    /**
     * Main thread task structure
     */
    public static class Task extends BaseMessage {

        /** Client to respond */
        private final ClientHandler.RespondToClientInterface messageResponseInterface;

        /**
         * @param message Base message to copy content from
         * @param messageResponseInterface Client interface to respond to
         */
        public Task(BaseMessage message, ClientHandler.RespondToClientInterface messageResponseInterface) {
            super(message.getCommand(), message.getArgs(), message.getData(), message.getCommunicationIdentifier());
            this.messageResponseInterface = messageResponseInterface;
        }

        /**
         * Getter for client response interface
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
        /// TODO
        public static final int TIMEOUT_MS = 15 * 60 * 1000;
        /** Server responses queue */
        //ConcurrentLinkedQueue<NetworkMessage> clientMessageQueue;
        /** Socket connected to client */
        private final Socket socket;

        /**
         * @param socket Socket connected to client
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
            //this.clientMessageQueue = new ConcurrentLinkedQueue<>();
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
         * Main loop
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
                    client.addMessage(new NetworkMessage("time_out", null, null, 0));
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
            } catch (IOException ex) {
                System.err.println("[ClientHandler]: IOException - " + ex.getMessage());
            } finally {
                if (client != null) {
                    clients.remove(client);
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
         * Interface for responding to client who provided task to server
         */
        public interface RespondToClientInterface {
            void respond(NetworkMessage message);
        }
    }
}
