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

public class TaskManager implements Runnable {

    private static int currentCommunicationStream = 1;

    public synchronized static int nextCommunicationStream() {
        return currentCommunicationStream++;
    }

    private ConcurrentLinkedQueue<BaseMessage> currentTasks;
    private ConcurrentLinkedQueue<BaseMessage> awaitingTasks;

    private ConcurrentLinkedQueue<BaseMessage> messagesToSend;

    private static final String host = "10.0.2.2";
    private static final int port = 9999;
    private Socket socket = null;

    private boolean isConnected;
    public static EventInfo eventInfo;

    public TaskManager() {
        this.currentTasks = new ConcurrentLinkedQueue<>();
        this.messagesToSend = new ConcurrentLinkedQueue<>();
        this.awaitingTasks = new ConcurrentLinkedQueue<>();
    }

    private BaseMessage pollCurrentTask() {
        return currentTasks.poll();
    }

    public boolean addCurrentTask(BaseMessage task) {
        return currentTasks.offer(task);
    }

    private boolean sendMessage(BaseMessage message) {
        return messagesToSend.offer(message);
    }

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

    private BaseMessage[] searchForAwaitingTasks(BaseMessage task) {
        int communicationID = task.getCommunicationStream();
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

    private interface CallAwaitingTaskInterface {
        boolean callAwaitingTaskOn(BaseMessage task); // true jesli zadanie ma zostac usuniete, false jesli ma zostac
    }

    private static class InputFromServer implements Runnable {

        private ObjectInputStream in;
        private AddTaskForManagerInterface addTaskForManagerInterface;

        public InputFromServer(ObjectInputStream in, AddTaskForManagerInterface addTaskForManagerInterface) {
            this.in = in;
            this.addTaskForManagerInterface = addTaskForManagerInterface;
        }

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

    private static class OutputToServer implements Runnable {

        private ObjectOutputStream out;
        private ConcurrentLinkedQueue<BaseMessage> messagesToSend;

        public OutputToServer(ObjectOutputStream out, ConcurrentLinkedQueue<BaseMessage> messagesToSend) {
            this.out = out;
            this.messagesToSend = messagesToSend;
        }

        private void send(BaseMessage message) {
            try {
                this.out.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace(); //do zmiany pozniej
            }
        }

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
