package server;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import network_structures.BaseMessage;
import org.bson.Document;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class representing specific account within the system, of unspecified role
 * Currently created only when client connects with the system and used only to construct a derived class of
 * specified role (eg. Guide) once identified, should be changed into abstract class in the future
 */
public class Client {

    /** Socket connected with client */
    protected Socket socket;
    /** Stream for receiving information from this specific client */
    protected ObjectInputStream in;
    /** Stream for sending information to this specific client */
    protected ObjectOutputStream out;

    /**
     * Base constructor, requires only functioning socket connection for setup
     * @param socket Valid socket to connect with client - should be exclusive for this object
     */
    public Client(Socket socket) {
        this.socket = socket;
    }

    /**
     * Default shallow copy constructor
     * @param client Client to copy
     */
    protected Client(Client client) {
        this.socket = client.socket;
        this.in = client.in;
        this.out = client.out;
    }

    /**
     * Information about specific client's socket for use by admin
     * @return String containing socket address, host name and port used
     */
    public String getSocketInfo() {
        return socket.getInetAddress().getHostName() + ":" + socket.getPort();
    }

    /**
     * Sets various characteristics of this connection
     * @param timeout Time client can be unresponsive before disconnecting with him
     * @throws IOException When socket does not function properly
     */
    public final void setConnectionSettings(int timeout) throws IOException {
        this.socket.setSoTimeout(timeout);
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    /**
     * Assigns specific role for this client, creating new representation in the process
     * @param client Client to receive new role
     * @param role Role to assign
     * @return New instance of derived, role-specific class
     */
    private static Client setPrivileges(Client client, String role) {
        switch (role) {
            case "G": {
                client = new Guide(client);
            } break;
            case "P": {
                client = new Presenter(client);
            } break;
            case "M": {
                client = new Moderator(client);
            } break;
            case "A": {
                client = new Administrator(client);
            } break;
        }
        return client;
    }

    /**
     * @deprecated Attempts to login client to the system by searching for his credentials within provided database
     * Might be removed during future Client rework
     * @param client Client to log in
     * @param database Database containing user credentials
     * @return Logged-in client
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected static Client loginToServer(Client client, MongoDatabase database) throws IOException, ClassNotFoundException {
        while (true) {
            BaseMessage message = (BaseMessage) client.in.readObject();
            if ("login".equals(message.getCommand())) {
                BasicDBObject userData = new BasicDBObject();
                userData.put("login", message.getArgs()[0]);
                userData.put("password", message.getArgs()[1]);
                Document userFindResult = database.getCollection("users").find(userData).first();
                if (userFindResult != null) {
                    client = setPrivileges(client, userFindResult.getString("role"));
                    client.out.writeObject(new BaseMessage("login", new String[] { "true" }, null, message.getCommunicationStream()));
                    break;
                } else {
                    client.out.writeObject(new BaseMessage("login", new String[] { "false" }, null, message.getCommunicationStream()));
                }
            } else if ("ping".equals(message.getCommand())) {
                ///.....
            }
        }
        return client;
    }

    /**
     * Sends client initial event data, containing only information which doesn't change during the course of this event
     * @throws IOException When socket is unable to send message
     */
    protected final void sendStartingData() throws IOException {
        this.out.writeObject(new BaseMessage("eventDetails", null, Server.getStartupData()));
    }

    /** Empty procedure to override in inherited classes **/
    protected void handlingRequests(ConcurrentLinkedQueue<BaseMessage> clientTaskQueue) throws IOException, ClassNotFoundException {

    }

    /** Empty procedure to override in inherited classes **/
    protected void chooseProcedure() {

    }
}
