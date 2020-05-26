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

public class Server {

    /// USED COMMANDS: ping, login, eventInfo, error

    private static final ConcurrentLinkedQueue<Task> taskManager = new ConcurrentLinkedQueue<>();

    private static final Set<Client> clients = new HashSet<>();
    private static final int port = 9999;

    //private static MongoClient mongoClient;
    private static MongoDatabase database;

    private static Map<ObjectId, Sector> sectors = new TreeMap<>();
    //private static Map<ObjectId, Room.RoomQueue>
    private static EventInfo startupData = new EventInfo();
    private static int sectorsSize = 0;

    private static final long DATE_CHECKING_DELAY = 1000;
    private static long eventStartingDate;
    private static boolean eventHasStarted = false;

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

    public static EventInfo getStartupData() {
        return startupData;
    }

    public static void enqueueTask(Task task) {
        taskManager.offer(task);
    }

    //private static void

    private static void serverSetup() {

        FindIterable<Document> sectorsIterator = database.getCollection("sectors").find();
        for (var sectorIterator : sectorsIterator) {
            ObjectId sectorId = sectorIterator.getObjectId("_id");
            String sectorName = sectorIterator.getString("name");
            String sectorAddress = sectorIterator.getString("address");
            String sectorDescription = sectorIterator.getString("description");
            FindIterable<Document> roomsOfSector = database.getCollection("sector" + sectorId.toString()).find();
            Sector sector = new Sector(sectorId, sectorName, sectorAddress, sectorDescription);
            sectors.put(sectorId, sector);
            startupData.getSectors().put(sectorId, sector.getInformations());
            ++sectorsSize;
            for (var room : roomsOfSector) {
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

    private static class MainServerTask implements Runnable {

        private void handleTask(Task task) {

        }

        @Override
        public void run() {
            
            while (new Date().getTime() < eventStartingDate) {
                try { Thread.sleep(DATE_CHECKING_DELAY); } catch (InterruptedException e) { System.out.println(e.getMessage()); }
            }

            eventHasStarted = true;
            System.out.println("Event has started! All queues are open!");

            // Checking if every sector and room has been loaded properly //
            for (var sector : sectors.values()) {
                System.out.println(sector.getInformations().name + ":");
                for (var room : sector.getRooms()) {
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

    public static class Task extends BaseMessage {

        private final ClientHandler.ClientTaskQueueInterface taskQueueInterface;

        public Task(BaseMessage message, ClientHandler.ClientTaskQueueInterface taskQueueInterface) {
            super(message.getCommand(), message.getArgs(), message.getData());
            this.taskQueueInterface = taskQueueInterface;
        }

        public ClientHandler.ClientTaskQueueInterface getTaskQueueInterface() {
            return taskQueueInterface;
        }
    }

    private static class ClientHandler implements Runnable {

        ConcurrentLinkedQueue<BaseMessage> clientTaskQueue;
        Client client;

        public ClientHandler(Socket socket) {
            this.client = new Client(socket);
            this.clientTaskQueue = new ConcurrentLinkedQueue<>();
        }

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

        public interface ClientTaskQueueInterface {
            boolean enqueue(BaseMessage message);
        }
    }
}
