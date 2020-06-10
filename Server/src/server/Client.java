package server;

import com.mongodb.BasicDBObject;
import network_structures.NetworkMessage;
import org.bson.Document;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    /** Stream for receiving information from this specific client */
    protected ObjectInputStream in;

    /// TODO
    public Client(ObjectOutputStream out, ObjectInputStream in) {
        this.outgoingMessages = new ConcurrentLinkedQueue<>();
        this.out = out;
        this.in = in;
    }

    /// TODO
    public static Client createSpecifiedClient(ObjectOutputStream out, ObjectInputStream in) {
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
                            client = new Guide(out, in);
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
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
            return client;
        }
        return client;
    }

    /**
     * Sends client initial event data, containing only information which doesn't change during the course of this event
     * @throws IOException When socket is unable to send message
     */
    protected final void sendStartingData() throws IOException {
        this.out.writeObject(new NetworkMessage("eventDetails", null, Server.getEventInfoFixed(), 0));
    }

    /// TODO
    protected abstract void handlingInput();

    /// TODO
    public boolean addMessage(NetworkMessage message) {
        return outgoingMessages.offer(message);
    }

    /// TODO
    protected abstract void handlingOutput();
}
