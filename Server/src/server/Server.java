package server;

import com.mongodb.client.*;
import network_structures.BaseMessage;
import network_structures.EventInfo;
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

import javax.print.Doc;

/*
 * Client messages recognized by server:
 * 1. error
 * 2. eventInfo
 * 3. login
 * 4. ping
 */
public class Server {
    /** Main server thread task queue */
    private static final ConcurrentLinkedQueue<Task> taskManager = new ConcurrentLinkedQueue<>();

    /** Collection containing all active client */
    private static final Set<Client> clients = new HashSet<>();
    /** Port to activate server on */
    private static final int port = 9999;

    /** Database connection */
    private static MongoDatabase database;

    // private static MongoClient mongoClient;

    private static Map<ObjectId, Sector> sectors = new TreeMap<>();
    //private static Map<ObjectId, Room.RoomQueue>
    private static EventInfo startupData = new EventInfo();
    private static int sectorsSize = 0;

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
    public static EventInfo getStartupData() {
        return startupData;
    }

    /**
     * Enqueues task for completion by system thread
     * @param task Task for main thread
     */
    public static void enqueueTask(Task task) {
        taskManager.offer(task);
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
            startupData.getSectors().put(sectorId, sector.getInformations());
            ++sectorsSize;
            for (Document room : roomsOfSector) {
                ObjectId roomId = room.getObjectId("_id");
                String roomName = room.getString("name");
                String roomLocation = room.getString("location");
                String roomDescription = room.getString("description");
                Room newRoom = new Room(roomName, roomLocation, roomDescription, 1);
                sector.addRoom(roomId, newRoom);
                sector.getInformations().rooms.put(roomId, newRoom.getInformations());
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
                System.out.println(sector.getInformations().name + ":");
                for (Room room : sector.getRooms()) {
                    System.out.println("\tRoom " + room.getInformations().getName());
                    System.out.println("\t\tRoom State: " + room.getState());
                }
            }

            // Main server task queue
            while (true) {
                Task task = taskManager.poll();
                if (task != null) {
                    handleTask(task);
                }
            }
        }
    }

    /**
     * Main thread task structure
     */
    public static class Task extends BaseMessage {

        /** Client to respond */
        private final ClientHandler.ClientTaskQueueInterface taskQueueInterface;

        /**
         * @param message Base message to copy content from
         * @param taskQueueInterface Client interface to respond to
         */
        public Task(BaseMessage message, ClientHandler.ClientTaskQueueInterface taskQueueInterface) {
            super(message.getCommand(), message.getArgs(), message.getData());
            this.taskQueueInterface = taskQueueInterface;
        }

        /**
         * Getter for client response interface
         * @return Client response interface
         */
        public ClientHandler.ClientTaskQueueInterface getTaskQueueInterface() {
            return taskQueueInterface;
        }
    }

    /**
     * Client handler task running on separate thread, responsible for communication between client and main thread
     */
    private static class ClientHandler implements Runnable {
        /** Server responses queue */
        ConcurrentLinkedQueue<BaseMessage> clientTaskQueue;
        /** Client handled */
        Client client;

        /**
         * @param socket Socket connected to client
         */
        public ClientHandler(Socket socket) {
            this.client = new Client(socket);
            this.clientTaskQueue = new ConcurrentLinkedQueue<>();
        }

        /**
         * Main loop
         */
        @Override
        public void run() {
            try {
                System.out.println("Client (" + client.getSocketInfo() + ") connected!");

                client.setConnectionSettings(15 * 60 * 1000);

                client = Client.loginToServer(client, database);
                client.sendStartingData();

                clients.add(client);

                client.handlingRequests(clientTaskQueue);

            } catch (NoSuchElementException ex) {
                System.out.println("Client (" + client.getSocketInfo() + ") is not responding!");
                try { client.out.writeObject("TIMED_OUT"); } catch (IOException e) { System.out.println("Server exception: " + ex.getMessage()); }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (client.out != null) {
                    clients.remove(client);
                }
                try { client.socket.close(); } catch(Exception ex) { System.out.println(ex.getMessage()); }
                System.out.println("Connection with (" + client.getSocketInfo() + ") has been closed!");
            }
        }

        /**
         * Interface for responding to client who provided task to server
         */
        public interface ClientTaskQueueInterface {
            boolean enqueue(BaseMessage message);
        }
    }
}
