package server;

import network_structures.NetworkMessage;
import queue.TourGroup;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;

public class Guide extends Client {

    private TourGroup group;

    public Guide(ObjectOutputStream out, ObjectInputStream in, TourGroup group) {
        super(out, in);
        this.group = group;
        if (this.group != null)
            this.group.addGuide(this);
    }

    public Guide(ObjectOutputStream out, ObjectInputStream in) {
        this(out, in, null);
    }

    /**
     * @return Current group of this guide
     */
    public TourGroup getGroup() {
        return this.group;
    }

    /**
     * @param group New group
     */
    protected void setGroup(TourGroup group) {
        this.group = group;
    }

    /**
     * Attempts to assign this guide to new group
     * @param group Group to reassign this guide to
     * @return true if successful, false otherwise
     */
    public boolean changeGroup(TourGroup group) {
        if (group != null && this.group != group) {
            this.group.removeGuide(this);
            return group.addGuide(this);
        }
        return false;
    }

    @Override
    protected void handlingInput() throws SocketTimeoutException, EOFException {
        while (true) {
            try {
                NetworkMessage message = (NetworkMessage) in.readObject();
                switch (message.getCommand()) {
                    case "update": {
                        addOutgoingMessage(new NetworkMessage("update", new String[]{"true"}, Server.getEventInfoUpdate(), message.getCommunicationIdentifier()));
                    } break;
                    case "add_to_queue":// For these calls, following structure is expected: args[0] should be sector ObjectId, args[1] should be room ObjectId
                    case "view_tickets":
                    case "remove_from_queue": {
                        Server.enqueueTask(new Server.Task(
                                message.getCommand(),
                                message.getArgs(),
                                this.group,
                                message.getCommunicationIdentifier(),
                                this::addOutgoingMessage
                        ));
                    } break;
                    default: {
                        if (Server.isCommandRecognizedByServer(message.getCommand()))
                            Server.enqueueTask(new Server.Task(
                                    message.getCommand(),
                                    message.getArgs(),
                                    message.getData(),
                                    message.getCommunicationIdentifier(),
                                    this::addOutgoingMessage
                            ));
                        else
                            addOutgoingMessage(new NetworkMessage("error", new String[] { "invalid_command" }, null, message.getCommunicationIdentifier()));
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

    @Override
    public void removeFromSystem() {
        super.removeFromSystem();
        int queuesRemovedFrom = group.removeFromAllQueues();
        System.err.println("Guide removed from " + queuesRemovedFrom + " queues");
    }
}
