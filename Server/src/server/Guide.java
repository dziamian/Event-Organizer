package server;

import java.io.IOException;
import java.net.Socket;

public class Guide extends Client {

    public Guide(Socket socket) {
        super(socket);
    }

    public Guide(Client client) {
        super(client);
    }

    @Override
    final protected void handlingRequests() throws IOException, ClassNotFoundException {
        while(true) {
            //Object obj = in.readObject();
            //if (obj instanceof ClassName1) ...
            //else if (obj instanceof ClassName2) ...
        }
    }
}
