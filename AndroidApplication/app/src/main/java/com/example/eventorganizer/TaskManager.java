package com.example.eventorganizer;

import android.util.Log;
import network_structures.*;
import org.bson.types.ObjectId;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Task responsible for handling server communication, ran on separate thread
 */
public class TaskManager implements Runnable {
    /** Automatically incremented communication stream counter */
    private static long currentCommunicationStream = 1;

    /**
     * @return Next usable communication stream identifier
     */
    public synchronized static long nextCommunicationStream() {
        return currentCommunicationStream++;
    }

    /** Messages for immediate  */
    private final ConcurrentLinkedQueue<BaseMessage> incomingMessages;
    /** Lingering tasks, mostly processes waiting for additional data */
    private final ConcurrentLinkedQueue<LingeringTask> lingeringTasks;
    /** Messages enqueued for sending to server */
    private final ConcurrentLinkedQueue<NetworkMessage> messagesToSend;

    /** Server IP address */
    private static final String host = "10.0.2.2";
    /** Server port */
    private static final int port = 9999;
    /** Socket connecting with server */
    private Socket socket = null;

    /// TODO
    private static final long UPDATE_DELAY_MS = 1000;

    /// TODO
    private static final int TIMEOUT_MS = 10 * 1000;

    /** Connection state flag */
    private boolean isConnected;

    /**
     * Default constructor, does not initialize this object completely
     */
    public TaskManager() {
        this.incomingMessages = new ConcurrentLinkedQueue<>();
        this.messagesToSend = new ConcurrentLinkedQueue<>();
        this.lingeringTasks = new ConcurrentLinkedQueue<>();
    }

    /**
     * Polls next message for interpretation
     * @return Message from immediately completable queue or null if queue is empty
     */
    private BaseMessage pollIncomingMessage() {
        return incomingMessages.poll();
    }

    /**
     * Add message into immediately completable queue
     * @param message Message to enqueue
     * @return True if message was successfully added, false otherwise - only returns false if out of memory
     */
    public boolean addIncomingMessage(BaseMessage message) {
        return incomingMessages.offer(message);
    }

    /**
     * Enqueues message for sending to server
     * @param message Message to send
     * @return True if message was successfully added, false otherwise - only returns false if out of memory
     */
    private boolean sendMessage(NetworkMessage message) {
        return messagesToSend.offer(message);
    }

    /**
     * Handler method for incoming messages
     * @param message Message to process
     */
    private void handleMessage(BaseMessage message) {
        /*
         * Recognized client requests:
         * 1. ping - check if server recognizes this client
         * 2. login - log in to server providing credentials in args
         * 3. event_info - request essential information about event, such as sectors / attractions list
         * 4. view_tickets - view my tickets and their states
         * 5. view_reservations - view my active reservation(s)
         * 6. add_to_queue - add my ticket to specified room queue
         * 7. remove_from_queue - remove my ticket from specific queue
         * 8. abandon_reservation - abandon one of my reservations (will result in penalty)
         * 9. update - request update on states of rooms and queues
         * 10. grouping - answer grouping call with decision or send update with changed decision
         */
        LingeringTask[] matchingAwaitingTasks = searchForMatchingLingeringTasks(message);
        if (matchingAwaitingTasks.length > 0) {
            for (LingeringTask matchingAwaitingTask : matchingAwaitingTasks) {
                if (matchingAwaitingTask.getCallable().callOn(message)) {
                    lingeringTasks.remove(matchingAwaitingTask);
                }
            }
        } else {
            switch (message.getCommand()) {
                case "update": {
                    startRequestingUpdates(message);
                } break;
                case "login": {
                    loginToServer(message);
                } break;
                case "event_details": {
                    GuideAccount.createInstance((EventInfoFixed) message.getData());
                    Collection<SectorInfoFixed> sectorsInfoFixed = GuideAccount.getInstance().getEventInfoFixed().getSectors().values();
                    for (SectorInfoFixed sectorInfoFixed : sectorsInfoFixed) {
                        Collection<RoomInfoFixed> roomsInfoFixed = sectorInfoFixed.getRooms().values();
                        for (RoomInfoFixed roomInfoFixed : roomsInfoFixed) {
                            setDisplayedStates(roomInfoFixed);
                        }
                    }
                } break;
                case "add_to_queue": {
                    addGroupToQueue(message);
                } break;
                case "remove_from_queue": {
                    removeFromQueue(message);
                } break;
                case "view_tickets": {
                    viewTickets(message);
                } break;
            }
        }
    }

    /**
     * Main loop
     */
    @Override
    public void run() {
        while (true) {
            while (!isConnected) {
                BaseMessage task = pollIncomingMessage();
                if (task != null && "connect".equals(task.getCommand())) {
                    isConnected = establishConnection();
                    if (!isConnected) {
                        ((Runnable)task.getData()).run();
                    }
                }
            }

            while (isConnected) {
                BaseMessage task = pollIncomingMessage();
                if (task != null) {
                    handleMessage(task);
                }
                for (LingeringTask lt : lingeringTasks) {
                    lt.getRunnable().run();
                }
            }
        }
    }

    /**
     * Attempts to establish connection with server
     * @return True if successfully connected, false otherwise
     */
    private boolean establishConnection() {
        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);
            new Thread(new OutputToServer(new ObjectOutputStream(socket.getOutputStream()), messagesToSend)).start();
            new Thread(new InputFromServer(new ObjectInputStream(socket.getInputStream()), this::addIncomingMessage)).start();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Adds lingering task which attempts to login into server
     * @param message Incoming message which requested login, following structure is expected:
     *                    command: string "login"
     *                    args: array of two strings - first containing login, second containing password
     *                    data: array of two Runnable callbacks - first for success, second for failure
     */
    private void loginToServer(BaseMessage message) {
        long streamId = TaskManager.nextCommunicationStream();
        lingeringTasks.add(new LingeringTask(
                "login",
                streamId,
                null,
                (msg) -> {
                    if (msg.getArgs()[0].equals("true")) {
                        ((Runnable[]) message.getData())[0].run();
                    } else {
                        ((Runnable[]) message.getData())[1].run();
                    }
                    return true;
                })
        );
        sendMessage(new NetworkMessage(message.getCommand(), message.getArgs(), null, streamId));
    }

    /// TODO:
    private void setDisplayedStates(RoomInfoUpdate roomInfoUpdate) {
        switch (roomInfoUpdate.getState()) {
            case "OPEN": {
                roomInfoUpdate.setState("Dostępny!");
            } break;
            case "RESERVED": {
                roomInfoUpdate.setState("Zarezerwowany!");
            } break;
            case "TAKEN": {
                roomInfoUpdate.setState("Zajęty!");
            } break;
            case "INACTIVE": {
                roomInfoUpdate.setState("Niedostępny!");
            } break;
        }
    }

    /// TODO:
    private void setDisplayedStates(RoomInfoFixed roomInfoFixed) {
        switch (roomInfoFixed.getState()) {
            case "OPEN": {
                roomInfoFixed.setState("Dostępny!");
            } break;
            case "RESERVED": {
                roomInfoFixed.setState("Zarezerwowany!");
            } break;
            case "TAKEN": {
                roomInfoFixed.setState("Zajęty!");
            } break;
            case "INACTIVE": {
                roomInfoFixed.setState("Niedostępny!");
            } break;
        }
    }

    /**
     * Creates lingering tasks responsible for pulling updates from server
     * and passing them to the UI thread, given proper request message
     * @param message Message containing method used for handling updates received from server
     */
    private void startRequestingUpdates(BaseMessage message) {
        long streamId = TaskManager.nextCommunicationStream();
        lingeringTasks.add(new LingeringTask(
                "update",
                streamId,
                new Runnable() {
                    private long lastRequestUpdateTime = System.currentTimeMillis();
                    @Override
                    public void run() {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastRequestUpdateTime > UPDATE_DELAY_MS) {
                            sendMessage(new NetworkMessage(
                                    "update",
                                    null,
                                    null,
                                    streamId
                            ));
                            lastRequestUpdateTime = currentTime;
                        }
                    }
                }, (msg) -> {
                    GuideAccount.getInstance().setEventInfoUpdate((EventInfoUpdate) msg.getData());
                    Collection<SectorInfoUpdate> sectorsInfoUpdate = GuideAccount.getInstance().getEventInfoUpdate().getSectors().values();
                    for (SectorInfoUpdate sectorInfoUpdate : sectorsInfoUpdate) {
                        Collection<RoomInfoUpdate> roomsInfoUpdate = sectorInfoUpdate.getRooms().values();
                        for (RoomInfoUpdate roomInfoUpdate : roomsInfoUpdate) {
                            setDisplayedStates(roomInfoUpdate);
                        }
                    }
                    if (HomeActivity.getUpdatingUI()) {
                        ((Runnable) message.getData()).run();
                    }
                    return !HomeActivity.getUpdatingUI();
                })
        );
    }

    private void addGroupToQueue(BaseMessage message) {
        long streamId = TaskManager.nextCommunicationStream();
        lingeringTasks.add(new LingeringTask(
                "add_to_queue",
                streamId,
                null,
                (msg) -> {
                    if ("0".equals(msg.getArgs()[0])) {
                        ((Runnable[]) message.getData())[1].run();
                    } else {
                        ((Runnable[]) message.getData())[0].run();
                    }
                    return true;
                })
        );
        sendMessage(new NetworkMessage(message.getCommand(), message.getArgs(), null, streamId));
    }

    private void removeFromQueue(BaseMessage message) {
        long streamId = TaskManager.nextCommunicationStream();
        lingeringTasks.add(new LingeringTask(
                "remove_from_queue",
                streamId,
                null,
                (msg) -> {
                    if ("true".equals(msg.getArgs()[0])) {
                        ((Runnable[]) message.getData())[0].run();
                    } else {
                        ((Runnable[]) message.getData())[1].run();
                    }
                    return true;
                })
        );
        sendMessage(new NetworkMessage(message.getCommand(), message.getArgs(), null, streamId));
    }

    private void viewTickets(BaseMessage message) {
        long streamId = TaskManager.nextCommunicationStream();
        lingeringTasks.add(new LingeringTask(
                "view_tickets",
                streamId,
                null,
                (msg) -> {
                    GuideAccount.getInstance().setQueues((QueueInfo[]) msg.getData());
                    ((Runnable)message.getData()).run();
                    return true;
                }
        ));
        sendMessage(new NetworkMessage(message.getCommand(), message.getArgs(), null, streamId));
    }

    /**
     * Searches awaiting tasks for those waiting for given message
     * @param message Message to match awaiting tasks with
     * @return Array containing all matching tasks; if none are found, array length will be 0
     */
    private LingeringTask[] searchForMatchingLingeringTasks(BaseMessage message) {
        long comId = message.getCommunicationIdentifier();
        ArrayList<LingeringTask> awaitingTaskInterfaces = new ArrayList<>();
        for (LingeringTask awaitingTask : lingeringTasks) {
            if (awaitingTask.getCommunicationIdentifier() == comId) {
                awaitingTaskInterfaces.add(awaitingTask);
            }
        }
        LingeringTask[] returnValue = new LingeringTask[awaitingTaskInterfaces.size()];
        for (int i = 0; i < returnValue.length; ++i) {
            returnValue[i] = awaitingTaskInterfaces.get(i);
        }
        return returnValue;
    }

    /**
     * Class representing ongoing processes within application core
     * Field <b>runnable</b> should contain method to run periodically regardless of current client state
     * Field <b>callable</b> should contain handler method for received messages
     * If no <b>runnable</b> is provided, this object will not be invoked periodically
     * If no <b>callable</b> is provided, this object will be removed should it receive any message
     */
    private static class LingeringTask extends BaseMessage {
        private final Runnable runnable;
        private final Callable callable;

        /**
         * Basic constructor initializing all fields
         * @param command Command to perform
         * @param args Arguments for command
         * @param data Any type of data needed for task execution
         * @param communicationId Identifier of communication stream this message belongs to
         * @param runnable Runnable to perform periodically regardless of received messages
         * @param callable Callable to perform when this lingering task receives message from elsewhere
         */
        public LingeringTask(String command, String[] args, Object data, long communicationId, Runnable runnable, Callable callable) {
            super(command, args, data, communicationId);
            this.runnable = (runnable != null ? runnable : () -> {});
            this.callable = (callable != null ? callable : (msg) -> true);
        }

        /**
         * Simplified basic constructor when arguments and/or data is not required
         * @param command Command to perform
         * @param communicationId Identifier of communication stream this message belongs to
         * @param runnable Runnable to perform periodically regardless of received messages
         * @param callable Callable to perform when this lingering task receives message from elsewhere
         */
        public LingeringTask(String command, long communicationId, Runnable runnable, Callable callable) {
            this(command,null,null,communicationId,runnable,callable);
        }

        /**
         * Getter for this lingering tasks's runnable
         * @return Runnable
         */
        public Runnable getRunnable() {
            return runnable;
        }

        /**
         * Getter for this lingering task's callabe
         * @return Callable
         */
        public Callable getCallable() {
            return callable;
        }

        /**
         * Interface for lingering tasks to handle received messages
         */
        public interface Callable {
            /**
             * Callback for handling received messages
             * @param message Message to handle
             * @return True if task in question is done and should be removed from lingering tasks, false otherwise
             */
            boolean callOn(BaseMessage message);
        }
    }

    /**
     * Task responsible for receiving data from server, ran on separate thread
     */
    private static class InputFromServer implements Runnable {

        /** Input stream to read data from */
        private final ObjectInputStream in;
        /** Interface for passing received messages to TaskManager */
        private final PassMessageToTaskManagerInterface taskManagerInterface;

        /**
         * Basic constructor
         * @param in Input stream linked with server
         * @param taskManagerInterface Callback to pass message to
         */
        public InputFromServer(ObjectInputStream in, PassMessageToTaskManagerInterface taskManagerInterface) {
            this.in = in;
            this.taskManagerInterface = taskManagerInterface;
        }

        /**
         * Reads message from server
         * @return Message from server if one was waiting in the buffer; null otherwise
         */
        private NetworkMessage receive() {
            try {
                return (NetworkMessage) this.in.readObject();
            } catch (EOFException e) {
                /// reconnect handling
            } catch (ClassNotFoundException e) {
                e.printStackTrace(); //do zmiany pozniej
            } catch (IOException e) {
                e.printStackTrace(); //do zmiany pozniej
            }
            return null;
        }

        /**
         * Main loop
         */
        @Override
        public void run() {
            while (true) {
                NetworkMessage message = receive();
                if (message != null) {
                    taskManagerInterface.passMessage(BaseMessage.convertToBaseMessage(message));
                }
            }
        }

        /**
         * Interface used by server communication task for passing messages to TaskManager thread
         */
        private interface PassMessageToTaskManagerInterface {
            /**
             * Adds message to specific message queue
             * @param message Message to add
             * @return True if successfully added the message, false otherwise
             */
            boolean passMessage(BaseMessage message);
        }
    }

    /**
     * Task responsible for sending data to server, ran on separate thread
     */
    private static class OutputToServer implements Runnable {

        /** Output stream to write messages to */
        private final ObjectOutputStream out;
        /** Queue to send messages from */
        private final ConcurrentLinkedQueue<NetworkMessage> messagesToSend;

        /**
         * Basic constructor
         * @param out Output stream linked with server
         * @param messagesToSend Queue containing messages to send
         */
        public OutputToServer(ObjectOutputStream out, ConcurrentLinkedQueue<NetworkMessage> messagesToSend) {
            this.out = out;
            this.messagesToSend = messagesToSend;
        }

        /**
         * Sends given message to server
         * @param message Message to send
         */
        private void send(NetworkMessage message) {
            try {
                this.out.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace(); //do zmiany pozniej
            }
        }

        /**
         * Main loop
         */
        @Override
        public void run() {
            while (true) {
                NetworkMessage message = messagesToSend.poll();
                if (message != null) {
                    send(message);
                }
            }
        }
    }
}
