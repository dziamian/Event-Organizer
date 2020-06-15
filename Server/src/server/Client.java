package server;

import com.mongodb.BasicDBObject;
import network_structures.NetworkMessage;
import network_structures.SectorInfoFixed;
import org.bson.Document;
import queue.TourGroup;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class representing specific account within the system, of unspecified role
 * Currently created only when client connects with the system and used only to construct a derived class of
 * specified role (eg. Guide) once identified, should be changed into abstract class in the future
 */
public abstract class Client {

    /// TODO
    protected ConcurrentLinkedQueue<NetworkMessage> outgoingMessages;
    /** Stream for sending information to this specific client */
    protected ObjectOutputStream out;
    /// TODO
    protected final AtomicBoolean outputThreadRunning = new AtomicBoolean(true);
    /// TODO
    protected final AtomicBoolean inputThreadRunning = new AtomicBoolean(true);
    /** Stream for receiving information from this specific client */
    protected ObjectInputStream in;

    /// TODO
    public Client(ObjectOutputStream out, ObjectInputStream in) {
        this.outgoingMessages = new ConcurrentLinkedQueue<>();
        this.out = out;
        this.in = in;
    }

    public void stopOutputThread() {
        outputThreadRunning.set(false);
    }

    public void stopInputThread() {
        inputThreadRunning.set(false);
    }

    /// TODO
    public static Client createSpecifiedClient(ObjectOutputStream out, ObjectInputStream in) throws SocketTimeoutException, EOFException {
        Client client = null;
        try {
            NetworkMessage message = (NetworkMessage) in.readObject();
            if ("login".equals(message.getCommand())) {
                BasicDBObject userData = new BasicDBObject();
                userData.put("login", message.getArgs()[0]);
                userData.put("password", message.getArgs()[1]);
                Document userFindResult = Server.getDatabase().getCollection("users").find(userData).first();
                if (userFindResult != null) {
                    switch (userFindResult.getString("role")) {
                        case "G": {
                            client = new Guide(out, in, new TourGroup());
                        }
                        break;
                        case "P": {
                            client = new Presenter(out, in);
                        }
                        break;
                        case "M": {
                            client = new Moderator(out, in);
                        }
                        break;
                        case "A": {
                            client = new Administrator(out, in);
                        }
                        break;
                    }
                    out.writeObject(new NetworkMessage("login", new String[]{"true"}, null, message.getCommunicationIdentifier()));
                } else {
                    out.writeObject(new NetworkMessage("login", new String[]{"false"}, null, message.getCommunicationIdentifier()));
                }
            } else if ("ping".equals(message.getCommand())) {
                out.writeObject(new NetworkMessage("ping", null, null, message.getCommunicationIdentifier()));
            }
        } catch (SocketTimeoutException | EOFException ex) {
            throw ex;
        } catch (IOException ex) {
            System.err.println("[Client-createSpecifiedClient()]: IOException - " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.err.println("[Client-createSpecifiedClient()]: ClassNotFoundException - " + ex.getMessage());
        }
        return client;
    }

    /**
     * Sends client initial event data, containing only information which doesn't change during the course of this event
     * @throws IOException When socket is unable to send message
     */
    protected final void sendStartingData() throws IOException {
        this.out.writeObject(new NetworkMessage("event_details", null, Server.getEventInfoFixed(), 0));
    }

    public void removeFromSystem() {
    }

    public void dismiss() {
        inputThreadRunning.set(false);
        outputThreadRunning.set(false);
    }

    /// TODO
    protected abstract void handlingInput() throws SocketTimeoutException, EOFException;

    /// TODO
    public boolean addOutgoingMessage(NetworkMessage message) {
        return outgoingMessages.offer(message);
    }

    /// TODO
    protected abstract void handlingOutput();
}
