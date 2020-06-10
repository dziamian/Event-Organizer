package server;

import network_structures.NetworkMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Guide extends Client {

    public Guide(ObjectOutputStream out, ObjectInputStream in) {
        super(out, in);
    }

    @Override
    protected void handlingInput() {
        while (true) {
            try {
                NetworkMessage message = (NetworkMessage) in.readObject();
                switch (message.getCommand()) {
                    case "update": {
                        addMessage(new NetworkMessage("update", new String[] { "true" }, Server.getEventInfoUpdate(), message.getCommunicationIdentifier()));
                    } break;
                    default: {
                        addMessage(new NetworkMessage("error", new String[] {"invalid_command"}, null, message.getCommunicationIdentifier()));
                    } break;
                }
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    @Override
    protected void handlingOutput() {
        while (true) {
            NetworkMessage message = outgoingMessages.poll();
            try {
                out.writeObject(message);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}
