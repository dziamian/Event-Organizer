package server;

import java.net.Socket;

public class Presenter extends Client {

    public Presenter(Socket socket) {
        super(socket);
    }

    public Presenter(Client client) {
        super(client);
    }
}
