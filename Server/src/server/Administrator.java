package server;

import java.net.Socket;

/**
 * System administrator account class
 */
public class Administrator extends Client{

    public Administrator(Socket socket) {
        super(socket);
    }

    public Administrator(Client client) {
        super(client);
    }
}
