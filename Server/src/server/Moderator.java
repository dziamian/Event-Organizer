package server;

import java.net.Socket;

public class Moderator extends Client {

    public Moderator(Socket socket) {
        super(socket);
    }

    public Moderator(Client client) {
        super(client);
    }
}
