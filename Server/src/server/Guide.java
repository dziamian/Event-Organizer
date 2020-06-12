package server;

import network_structures.NetworkMessage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;

public class Guide extends Client {

    public Guide(ObjectOutputStream out, ObjectInputStream in) {
        super(out, in);
    }

    @Override
    protected void handlingInput() throws SocketTimeoutException, EOFException {
        while (true) {
            try {
                NetworkMessage message = (NetworkMessage) in.readObject();
                switch (message.getCommand()) {
                    case "update": {
                        addMessage(new NetworkMessage("update", new String[]{"true"}, Server.getEventInfoUpdate(), message.getCommunicationIdentifier()));
                    } break;
                    default: {
                        addMessage(new NetworkMessage("error", new String[] {"invalid_command"}, null, message.getCommunicationIdentifier()));
                    } break;
                }
            } catch (SocketTimeoutException | EOFException ex) {
                throw ex;
            } catch (IOException ex) {
                System.err.println("[Guide-handlingInput()]: IOException - " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                System.err.println("[Guide-handlingInput()]: ClassNotFoundException - " + ex.getMessage());
            }
        }
    }

    @Override
    protected void handlingOutput() {
        while (outputThreadRunning.get() || !outgoingMessages.isEmpty()) {
            NetworkMessage message = outgoingMessages.poll();
            try {
                if (message != null) {
                    if ("update".equals(message.getCommand())) {
                        out.reset();
                    }
                    out.writeObject(message);
                }
            } catch (IOException ex) {
                System.err.println("[Guide-handlingOutput()]: IOException - " + ex.getMessage());
            }
        }
    }
}
