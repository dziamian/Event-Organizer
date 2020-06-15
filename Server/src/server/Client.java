package server;

import com.mongodb.BasicDBObject;
import network_structures.NetworkMessage;
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

    /** Messages enqueued for sending to client */
    protected ConcurrentLinkedQueue<NetworkMessage> outgoingMessages;
    /** Stream for sending information to this specific client */
    protected ObjectOutputStream out;
    /** Condition for output thread to work */
    protected final AtomicBoolean outputThreadRunning = new AtomicBoolean(true);
    /** Condition for input thread to work */
    protected final AtomicBoolean inputThreadRunning = new AtomicBoolean(true);
    /** Stream for receiving information from this specific client */
    protected ObjectInputStream in;

    /**
     * Creates basic client connection with provided streams
     * @param out Output stream connected to client
     * @param in Input stream connected to client
     */
    protected Client(ObjectOutputStream out, ObjectInputStream in) {
        this.outgoingMessages = new ConcurrentLinkedQueue<>();
        this.out = out;
        this.in = in;
    }

    /**
     * Stops client output thread
     */
    public void stopOutputThread() {
        outputThreadRunning.set(false);
    }


    /**
     * Stops client input thread
     */
    public void stopInputThread() {
        inputThreadRunning.set(false);
    }

    /**
     * Creates proper subclass of Client depending on his assigned role within the system
     * @param out Output stream connected to client
     * @param in Input stream connected to client
     * @return New client of proper type
     * @throws SocketTimeoutException if client takes too long to respond
     * @throws EOFException if client's socket has been closed
     */
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

    /**
     * Empty procedure to override in subclasses used for removing all references to given client from the system, mostly used when client disconnects
     */
    public void removeFromSystem() {}

    /**
     * Closes client's communication threads (can be restarted later)
     */
    public void dismiss() {
        inputThreadRunning.set(false);
        outputThreadRunning.set(false);
    }

    /**
     * Method responsible for receiving and filtering data received from the client.
     * @throws SocketTimeoutException when client response time is longer than given maximum
     * @throws EOFException when client socket has been closed
     */
    protected abstract void handlingInput() throws SocketTimeoutException, EOFException;

    /**
     * Method responsible for handling sending outgoing data to the client through provided output stream.
     */
    protected abstract void handlingOutput();

    /**
     * Adds message for sending to this client. Detailed description of message structure can be found in {@link NetworkMessage}.
     * @param message Message to be sent
     * @return True if message has been successfully enqueued for sending, false otherwise
     */
    public boolean addOutgoingMessage(NetworkMessage message) {
        return outgoingMessages.offer(message);
    }

}
