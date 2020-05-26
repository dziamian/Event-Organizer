package server;

import network_structures.BaseMessage;

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

    private static void addToQueue(BaseMessage msg) {

    }

    private static void removeFromQueue(BaseMessage msg) {

    }

    protected void chooseProcedure(BaseMessage message) {
        switch (message.getCommand()) {
            case "addToQueue":
                addToQueue(message);
        }
    }

    @Override
    final protected void handlingRequests(ConcurrentLinkedQueue<BaseMessage> clientTaskQueue) throws IOException, ClassNotFoundException {
        while(true) {
            BaseMessage message = (BaseMessage) in.readObject();
            if (isCommandRecognized(message.getCommand())) {
                Server.enqueueTask(new Server.Task(message, clientTaskQueue::offer));
            } else {
                this.out.writeObject(new BaseMessage(
                        "error",
                        new String[] { "invalidCommand" },
                        null)
                );
            }
        }
    }
}
