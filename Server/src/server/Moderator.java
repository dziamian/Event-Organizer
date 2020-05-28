package server;

import java.net.Socket;

/**
 * System moderator account class
 */
public class Moderator extends Client {

    public Moderator(Socket socket) {
        super(socket);
    }

    public Moderator(Client client) {
        super(client);
    }


}
