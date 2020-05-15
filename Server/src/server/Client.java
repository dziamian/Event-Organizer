package server;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import network_structures.LoginData;
import network_structures.LoginConfirmationData;
import network_structures.SectorInfo;
import org.bson.Document;
import queue.Sector;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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

    final public void setConnectionSettings(int timeout) throws IOException {
        this.socket.setSoTimeout(timeout);
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    static protected Client loginToServer(Client client, MongoDatabase database) throws IOException, ClassNotFoundException {
        while (true) {
            Object obj = client.in.readObject();
            if (obj instanceof LoginData) {
                LoginData user = (LoginData) obj;
                BasicDBObject userPreferences = new BasicDBObject();
                userPreferences.put("login", user.login);
                userPreferences.put("password", user.password);
                Document userFindResult = database.getCollection("users").find(userPreferences).first();
                if (userFindResult != null) {
                    switch (userFindResult.getString("role")) {
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
                    client.out.writeObject(new LoginConfirmationData(true, "Successfully logged in!"));
                    break;
                }
                else
                    client.out.writeObject(new LoginConfirmationData(false, "Invalid login or password!"));
            }
        }
        return client;
    }

    protected void sendStartingData() throws IOException {
        this.out.writeObject(Server.startupData);
    }

    /** Empty procedure to override in inherited classes **/
    protected void handlingRequests() throws IOException, ClassNotFoundException {

    }
}
