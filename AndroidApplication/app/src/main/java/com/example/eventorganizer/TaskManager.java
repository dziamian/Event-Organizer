package com.example.eventorganizer;

import network_structures.BaseMessage;
import network_structures.EventInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Task responsible for handling server communication, ran on separate thread
 */
public class TaskManager implements Runnable {
    /** Automatically incremented communication stream counter */
    private static int currentCommunicationStream = 1;

    /**
     * @return Next usable communication stream identifier
     */
    public synchronized static int nextCommunicationStream() {
        return currentCommunicationStream++;
    }

    /** Tasks possible for completion immediately */
    private ConcurrentLinkedQueue<BaseMessage> currentTasks;
    /** Lingering tasks, mostly processes waiting for additional data */
    private ConcurrentLinkedQueue<BaseMessage> awaitingTasks;
    /** Messages enqueued for sending to server */
    private ConcurrentLinkedQueue<BaseMessage> messagesToSend;

    /** Server IP address */
    private static final String host = "10.0.2.2";
    /** Server port */
    private static final int port = 9999;
    /** Socket connecting with server */
    private Socket socket = null;

    /** Connection state flag */
    private boolean isConnected;
    /** Event information, mostly unchanging */
    public static EventInfo eventInfo;

    /**
     * Default constructor, does not initialize this object completely
     */
    public TaskManager() {
        this.currentTasks = new ConcurrentLinkedQueue<>();
        this.messagesToSend = new ConcurrentLinkedQueue<>();
        this.awaitingTasks = new ConcurrentLinkedQueue<>();
    }

    /**
     * Polls next task for completion
     * @return Task from immediately completable queue
     */
    private BaseMessage pollCurrentTask() {
        return currentTasks.poll();
    }

    /**
     * Add task into immediately completable queue
     * @param task Task to enqueue
     * @return True if task was successfully added, false otherwise - only returns false if out of memory
     */
    public boolean addCurrentTask(BaseMessage task) {
        return currentTasks.offer(task);
    }

    /**
     * Enqueues message for sending to server
     * @param message Message to send
     * @return True if message was successfully added, false otherwise - only returns false if out of memory
     */
    private boolean sendMessage(BaseMessage message) {
        return messagesToSend.offer(message);
    }

    /**
     * Handler method for task from immediately completable queue
     * @param currentTask Task to handle
     */
    private void handleCurrentTask(BaseMessage currentTask) {
        BaseMessage[] matchingAwaitingTasks = searchForAwaitingTasks(currentTask);
        if (matchingAwaitingTasks.length > 0) {
            for (BaseMessage matchingAwaitingTask : matchingAwaitingTasks) {
                if (((CallAwaitingTaskInterface) matchingAwaitingTask.getData()).callAwaitingTaskOn(currentTask)) {
                    awaitingTasks.remove(matchingAwaitingTask);
                }
            }
        } else {
            switch (currentTask.getCommand()) {
                case "login": {
                    loginToServer(currentTask);
                } break;
                case "eventDetails": {
                    eventInfo = (EventInfo) currentTask.getData();
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
                BaseMessage task = pollCurrentTask();
                if (task != null && "connect".equals(task.getCommand())) {
                    isConnected = establishConnection();
                    if (!isConnected) {
                        ((Runnable)task.getData()).run();
                    }
                }
            }

            while (isConnected) {
                BaseMessage task = pollCurrentTask();
                if (task != null)
                    handleCurrentTask(task);
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
            this.socket.connect(new InetSocketAddress(host, port), 10000);
            new Thread(new OutputToServer(new ObjectOutputStream(socket.getOutputStream()), messagesToSend)).start();
            new Thread(new InputFromServer(new ObjectInputStream(socket.getInputStream()), this::addCurrentTask)).start();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Adds lingering tasks which attempts to login into server
     * @param currentTask Task which requested login, following structure is required:
     *                    command: string "login"
     *                    args: array of two strings - first containing login, second containing password
     *                    data: array of two callbacks - first for success, second for failure
     */
    private void loginToServer(BaseMessage currentTask) {
        int streamId = TaskManager.nextCommunicationStream();
        awaitingTasks.add(new BaseMessage(
                "login",
                null,
                (CallAwaitingTaskInterface) (msg) -> {
                    if (msg.getArgs()[0].equals("true")) {
                        ((Runnable[]) currentTask.getData())[0].run();
                    } else {
                        ((Runnable[]) currentTask.getData())[1].run();
                    }
                    return true;
                }, streamId)
        );
        sendMessage(new BaseMessage(currentTask.getCommand(), currentTask.getArgs(), null, streamId));
    }

    /**
     * Searches awaiting tasks for those waiting for given message
     * @param message Message to match awaiting tasks with
     * @return Array containing all matching tasks; if none are found, array length will be 0
     */
    private BaseMessage[] searchForAwaitingTasks(BaseMessage message) {
        int communicationID = message.getCommunicationStream();
        ArrayList<BaseMessage> awaitingTaskInterfaces = new ArrayList<>();
        for (BaseMessage awaitingTask : awaitingTasks) {
            if (awaitingTask.getCommunicationStream() == communicationID) {
                awaitingTaskInterfaces.add(awaitingTask);
            }
        }
        BaseMessage[] returnValue = new BaseMessage[awaitingTaskInterfaces.size()];
        for (int i = 0; i < returnValue.length; ++i) {
            returnValue[i] = awaitingTaskInterfaces.get(i);
        }
        return returnValue;
    }

    /**
     * Interface for awaiting tasks to handle received messages
     */
    private interface CallAwaitingTaskInterface {
        /**
         * Callback for handling received message
         * @param message Message to handle
         * @return True if task in question is done and should be removed from lingering tasks, false otherwise
         */
        boolean callAwaitingTaskOn(BaseMessage message);
    }

    /**
     * Task responsible for receiving data from server, ran on separate thread
     */
    private static class InputFromServer implements Runnable {

        /** Input stream to read data from */
        private ObjectInputStream in;
        /** Interface for passing received messages to TaskManager */
        private AddTaskForManagerInterface addTaskForManagerInterface;

        /**
         * Basic constructor
         * @param in Input stream linked with server
         * @param addTaskForManagerInterface Callback to pass message to
         */
        public InputFromServer(ObjectInputStream in, AddTaskForManagerInterface addTaskForManagerInterface) {
            this.in = in;
            this.addTaskForManagerInterface = addTaskForManagerInterface;
        }

        /**
         * Reads message from server
         * @return Message from server if one was waiting in the buffer; null otherwise
         */
        private BaseMessage receive() {
            try {
                return (BaseMessage) this.in.readObject();
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
                BaseMessage message = receive();
                if (message != null) {
                    addTaskForManagerInterface.add(message);
                }
            }
        }

        private interface AddTaskForManagerInterface {
            boolean add(BaseMessage message);
        }
    }

    /**
     * Task responsible for sending data to server, ran on separate thread
     */
    private static class OutputToServer implements Runnable {

        /** Output stream to write messages to */
        private ObjectOutputStream out;
        /** Queue to send messages from */
        private ConcurrentLinkedQueue<BaseMessage> messagesToSend;

        /**
         * Basic constructor
         * @param out Output stream linked with server
         * @param messagesToSend Queue containing messages to send
         */
        public OutputToServer(ObjectOutputStream out, ConcurrentLinkedQueue<BaseMessage> messagesToSend) {
            this.out = out;
            this.messagesToSend = messagesToSend;
        }

        /**
         * Sends given message to server
         * @param message Message to send
         */
        private void send(BaseMessage message) {
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
                BaseMessage message = messagesToSend.poll();
                if (message != null) {
                    send(message);
                }
            }
        }
    }
}
