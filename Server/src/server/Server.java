package server;

import com.mongodb.client.*;
import network_structures.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.*;
import java.net.*;
import java.util.*;

// Structure for task queues
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    /// TODO
    private static final EventInfoFixed eventInfoFixed = new EventInfoFixed();
    /// TODO
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
            System.err.println("Server exception: " + ex.getMessage());
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
     * Main server task running on separate thread, responsible for system management
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
             * 3. event_info - request essential information about event, such as sectors / attractions list
             * 4. view_tickets - view my tickets and their states
             * 5. view_reservations - view my active reservation(s)
             * 6. add_to_queue - add my ticket to specified room queue
             * 7. remove_from_queue - remove my ticket from specific queue
             * 8. abandon_reservation - abandon one of my reservations (will result in penalty)
             * 9. update - request update on states of rooms and queues
             * 10. grouping - answer grouping call with decision or send update with changed decision
             */
            switch (task.getCommand()) {
                case "add_to_queue": {
                    Room room = sectors.get(
                            new ObjectId(task.getArgs()[0])
                    ).getRoom(
                            new ObjectId(task.getArgs()[1])
                    );
                    task.getResponseInterface().respond(new NetworkMessage(
                            "add_to_queue",
                            new String[] { String.valueOf(room.addGroupToQueue((TourGroup)task.getData())) },
                            null,
                            task.getCommunicationIdentifier())
                    );
                } break;
                case "remove_from_queue": {
                    Room room = sectors.get(
                            new ObjectId(task.getArgs()[0])
                    ).getRoom(
                            new ObjectId(task.getArgs()[1])
                    );
                    task.getResponseInterface().respond(new NetworkMessage(
                            "remove_from_queue",
                            new String[] { "" + room.removeGroupFromQueue((TourGroup)task.getData()) },
                            null,
                            task.getCommunicationIdentifier()
                    ));
                } break;
                case "view_tickets": {
                    Room[] rooms = ((TourGroup)task.getData()).getTicketRooms();
                    QueueInfo[] queueInfo = new QueueInfo[rooms.length];
                    System.out.println(rooms.length);
                    for (int i = 0; i < rooms.length; ++i) {
                        queueInfo[i] = new QueueInfo(
                            rooms[i].getInfoFixed().getSectorId(),
                            rooms[i].getInfoFixed().getId(),
                            rooms[i].positionOf((TourGroup)task.getData())
                        );
                    }
                    task.getResponseInterface().respond(new NetworkMessage(
                            "view_tickets",
                            null,
                            queueInfo,
                            task.getCommunicationIdentifier()
                    ));
                } break;
                default : {
                    task.getResponseInterface().respond(
                            new NetworkMessage(
                                    "error",
                                    new String[] { "invalid_command" },
                                    null,
                                    task.getCommunicationIdentifier()
                            )
                    );
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
                for (Room room : sector.getRoomsValues()) {
                    System.out.println("\tRoom " + room.getInfoFixed().getName());
                    System.out.println("\t\tRoom State: " + room.getState());
                }
            }


            // Main server task queue
            while (true) {
                Task task = receivedTasks.poll();
                if (task != null)
                    handleTask(task);
            }

            /////////////////////////////////////////////////////////////////////////////////////////////////////////////// TEST
//            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//            executor.scheduleAtFixedRate(() -> sectors.values().forEach(
//                    (sector -> {
//                        sector.getInfoUpdate().setActiveRoomsCount(sector.getInfoUpdate().getActiveRoomsCount().get()+1);
//                        sector.getInfoUpdate().getRooms().values().forEach((roomInfoUpdate -> roomInfoUpdate.setQueueSize(roomInfoUpdate.getQueueSize().get()+1)));
//                    })),
//                    0, 1000, TimeUnit.MILLISECONDS);
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

        public Task(String command, String[] args, Object data, long communicationIdentifier, ClientHandler.RespondToClientInterface messageResponseInterface) {
            super(command, args, data, communicationIdentifier);
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
        // ConcurrentLinkedQueue<NetworkMessage> clientMessageQueue;
        /** Socket connected to client */
        private final Socket socket;

        /**
         * @param socket Socket connected to client
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
            // this.clientMessageQueue = new ConcurrentLinkedQueue<>();
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
