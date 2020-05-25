package server;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import network_structures.BaseNetworkMessage;
import org.bson.Document;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {

    protected Socket socket;
    protected ObjectInputStream in;
    protected ObjectOutputStream out;

    public Client(Socket socket) {
        this.socket = socket;
    }

    protected Client(Client client) {
        this.socket = client.socket;
        this.in = client.in;
        this.out = client.out;
    }

    public String getSocketInfo() {
        return socket.getInetAddress().getHostName() + ":" + socket.getPort();
    }

    public final void setConnectionSettings(int timeout) throws IOException {
        this.socket.setSoTimeout(timeout);
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

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

    protected static Client loginToServer(Client client, MongoDatabase database) throws IOException, ClassNotFoundException {
        while (true) {
            BaseNetworkMessage message = (BaseNetworkMessage) client.in.readObject();
            if ("login".equals(message.getCommand())) {
                BasicDBObject userData = new BasicDBObject();
                userData.put("login", message.getArgs()[0]);
                userData.put("password", message.getArgs()[1]);
                Document userFindResult = database.getCollection("users").find(userData).first();
                if (userFindResult != null) {
                    client = setPrivileges(client, userFindResult.getString("role"));
                    client.out.writeObject(new BaseNetworkMessage("login", new String[] {"true"}, "Successfully logged in!"));
                    break;
                } else {
                    client.out.writeObject(new BaseNetworkMessage("login", new String[] {"false"}, "Invalid login or password!"));
                }
            } else if ("ping".equals(message.getCommand())) {
                ///.....
            }
        }
        return client;
    }

    protected final void sendStartingData() throws IOException {
        this.out.writeObject(new BaseNetworkMessage("eventDetails", null, Server.getStartupData()));
    }

    /** Empty procedure to override in inherited classes **/
    protected void handlingRequests(ConcurrentLinkedQueue<BaseNetworkMessage> clientTaskQueue) throws IOException, ClassNotFoundException {

    }

    /** Empty procedure to override in inherited classes **/
    protected void chooseProcedure() {

    }
}
