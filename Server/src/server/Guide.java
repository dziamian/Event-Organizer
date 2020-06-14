package server;

import network_structures.EventInfoUpdate;
import network_structures.NetworkMessage;
import queue.TourGroup;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Guide extends Client {
    private final Consumer<Server.Task> enqueueTaskForServer;
    private final Function<String, Boolean> commandRecognitionFunction;
    private final Supplier<EventInfoUpdate> updateSupplier;
    private TourGroup group;

    public Guide(
            ObjectOutputStream out,
            ObjectInputStream in,
            TourGroup group,
            Consumer<Server.Task> enqueueTaskForServer,
            Function<String, Boolean> commandRecognitionFunction,
            Supplier<EventInfoUpdate> updateSupplier
    ) {
        super(out, in);
        this.group = group;
        if (this.group != null)
            group.addGuide(this);
        this.enqueueTaskForServer = enqueueTaskForServer;
        this.commandRecognitionFunction = commandRecognitionFunction;
        this.updateSupplier = updateSupplier;
    }

    public Guide(ObjectOutputStream out, ObjectInputStream in, TourGroup group) {
        this(out, in, group, Server::enqueueTask, Server::isCommandRecognizedByServer, Server::getEventInfoUpdate);
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
     * Attempts to assign this guide to new group
     * @param group Group to reassign this guide to
     * @return true if successful, false otherwise
     */
    public boolean changeGroup(TourGroup group) {
        if (group != null && this.group != group) {
            group.removeGuide(this);
            return group.addGuide(this);
        }
        return false;
    }

    @Override
    public void handlingInput() throws SocketTimeoutException, EOFException {
        while (inputThreadRunning.get()) {
            try {
                NetworkMessage message = (NetworkMessage) in.readObject();
                switch (message.getCommand()) {
                    case "update": {
                        addOutgoingMessage(new NetworkMessage("update", new String[]{"true"}, updateSupplier.get(), message.getCommunicationIdentifier()));
                    } break;
                    case "add_to_queue":// For these calls, following structure is expected: args[0] should be sector ObjectId, args[1] should be room ObjectId
                    case "view_tickets":
                    case "remove_from_queue": {
                       enqueueTaskForServer.accept(new Server.Task(
                                message.getCommand(),
                                message.getArgs(),
                                this.group,
                                message.getCommunicationIdentifier(),
                                this::addOutgoingMessage
                        ));
                    } break;
                    default: {
                        if (commandRecognitionFunction.apply(message.getCommand()))
                            enqueueTaskForServer.accept(new Server.Task(
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
    public void handlingOutput() {
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
        super.dismiss();
        super.removeFromSystem();
        if (group != null) {
            group.removeGuide(this);
            int queuesRemovedFrom = group.removeFromAllQueues();
            System.out.println("Guide removed from " + queuesRemovedFrom + " queues");
        }
    }
}
