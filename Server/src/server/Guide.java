package server;

import network_structures.BaseNetworkMessage;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Guide extends Client {

    private static final String[] commands = { "" };

    public Guide(Socket socket) {
        super(socket);
    }

    public Guide(Client client) {
        super(client);
    }

    public static String[] getCommands() {
        return commands;
    }

    private static boolean isCommandRecognized(String command) {
        for (String value : commands) {
            if (command.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static void addToQueue(BaseNetworkMessage msg) {

    }

    private static void removeFromQueue(BaseNetworkMessage msg) {

    }

    protected void chooseProcedure(BaseNetworkMessage message) {
        switch (message.getCommand()) {
            case "addToQueue":
                addToQueue(message);
        }
    }

    @Override
    final protected void handlingRequests(ConcurrentLinkedQueue<BaseNetworkMessage> clientTaskQueue) throws IOException, ClassNotFoundException {
        while(true) {
            BaseNetworkMessage message = (BaseNetworkMessage) in.readObject();
            if (isCommandRecognized(message.getCommand())) {
                Server.enqueueTask(new Server.Task(message, clientTaskQueue::offer));
            } else {
                this.out.writeObject(new BaseNetworkMessage(
                        "error",
                        new String[] { "invalidCommand" },
                        null)
                );
            }
        }
    }
}
