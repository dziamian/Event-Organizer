package server;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import network_structures.EventData;
import org.bson.Document;
import org.bson.types.ObjectId;

import network_structures.SectorInfo;

import java.io.*;
import java.net.*;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import queue.Sector;
import queue.Room;

public class Server {

    final static Set<Client> clients = new HashSet<>();
    final static int port = 9999;

    static MongoClient mongoClient;
    static MongoDatabase database;

    static Map<ObjectId, Sector> sectors;
    static EventData startupData = new EventData();
    static int sectorsSize = 0;

    static boolean isOpen = false;

    public static void main(String[] args) {

        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);

        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("guideDB");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port " + serverSocket.getLocalPort() + "...\n");

            new OpeningAllQueues().start();

            while (true) {
                new SocketThread(serverSocket.accept()).start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
        }
    }

    private static class OpeningAllQueues extends Thread {

        @Override
        public void run() {

            MongoCollection<Document> serverVariablesColl = database.getCollection("serverVariables");
            Document serverVariables = serverVariablesColl.find().first();
            assert serverVariables != null : "Cannot get document from collection!";

            long serverOpenDate = serverVariables.getDate("serverOpenDate").getTime();
            while (new Date().getTime() < serverOpenDate) {
                try { sleep(1000); } catch (InterruptedException e) { System.out.println(e.getMessage()); }
            }

            FindIterable<Document> buildingsIterator = database.getCollection("sectors").find();
            sectors = new TreeMap<>();
            startupData.sectors = new TreeMap<>();
            for (var buildingIterator : buildingsIterator) {
                ObjectId buildingId = buildingIterator.getObjectId("_id");
                String buildingName = buildingIterator.getString("name");
                String buildingAddress = buildingIterator.getString("address");
                String buildingDescription = buildingIterator.getString("description");
                FindIterable<Document> instancesOfBuilding = database.getCollection("sector" + buildingId.toString()).find();
                Sector building = new Sector(buildingName, buildingAddress, buildingDescription);
                sectors.put(buildingId, building);
                startupData.sectors.put(buildingId, building.getInformations());
                ++sectorsSize;
                for (var instanceDoc : instancesOfBuilding) {
                    ObjectId roomId = instanceDoc.getObjectId("_id");
                    String roomName = instanceDoc.getString("instance_name");
                    //assert roomName != null;
                    //try {
                        Room newRoom = new Room(roomName,"", 1);
                        building.addRoom(roomId, newRoom);
                        building.getInformations().rooms.put(roomId, newRoom.getInformations());
                    //} catch(Exception ex) {
                       //System.out.println();
                    //}
                }
            }

            isOpen = true;
            System.out.println("All queues in server are opened!");

            //TourGroup group = new TourGroup();

            for (var sector : sectors.values()) {
                System.out.println(sector.getInformations().name + ":");
                for (var room : sector.getRooms()) {
                    System.out.println("\tRoom " + room.getInformations().name);
                    //TourGroup group = new TourGroup();
                    //room.addToQueue(group);
                    System.out.println("\t\tRoom State: " + room.getState());
                    //System.out.println("\t\tTicket expiration date: " + group.getReservation().getExpirationDate());
                }
            }

            /*while (true) {
                for (var writer : writers) {
                    writer.println("UPDATE");
                }
                try { sleep(2000); } catch (InterruptedException e) { System.out.println(e.getMessage()); }
            }*/

        }
    }

    private static class SocketThread extends Thread {

        Client client;

        public SocketThread(Socket socket) {
            this.client = new Client(socket);
        }

        @Override
        public void run() {
            try {
                System.out.println("Client (" + client.getSocketInfo() + ") connected!");

                client.setConnectionSettings(15 * 60 * 1000);
                clients.add(client);

                client = Client.loginToServer(client, database);
                client.sendStartingData();

                client.handlingRequests();

            } catch (NoSuchElementException ex) {
                System.out.println("Client (" + client.getSocketInfo() + ") is not responding!");
                try { client.out.writeObject("TIMED_OUT"); } catch (IOException e) { System.out.println("Server exception: " + ex.getMessage()); }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (client.out != null)
                    clients.remove(client);
                try { client.socket.close(); } catch(Exception ex) { System.out.println(ex.getMessage()); }
                System.out.println("Connection with (" + client.getSocketInfo() + ") has been closed!");
            }
        }
    }
}
