package server;

import java.net.Socket;

public class Administrator extends Client{

    public Administrator(Socket socket) {
        super(socket);
    }

    public Administrator(Client client) {
        super(client);
    }
}
