package com.example.eventorganizer;

import android.util.Log;
import network_structures.BaseMessage;
import network_structures.EventInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionToServer implements Runnable {

    /// EWENTUALNIE DO ZMIANY
    private ConcurrentLinkedQueue<BaseMessage> tasks;

    private ConcurrentLinkedQueue<BaseMessage> receivedMessages;
    private ConcurrentLinkedQueue<BaseMessage> messagesToSend;

    private static final String host = "10.0.2.2";
    private static final int port = 9999;
    private Socket socket = null;

    private boolean isConnected;
    public static EventInfo eventInfo;

    public ConnectionToServer() {
        this.tasks = new ConcurrentLinkedQueue<>();
        this.messagesToSend = new ConcurrentLinkedQueue<>();
        this.receivedMessages = new ConcurrentLinkedQueue<>();
    }

    private BaseMessage pollTask() {
        return tasks.poll();
    }

    public boolean addTask(BaseMessage task) {
        return tasks.offer(task);
    }

    private BaseMessage receiveMessage() {
        return receivedMessages.poll();
    }

    private boolean sendMessage(BaseMessage message) {
        return messagesToSend.offer(message);
    }

    private void handleTask(BaseMessage task) {
        switch (task.getCommand()) {
            case "login": {
                loginToServer(task.getArgs());
                while (true) {
                    BaseMessage message = receiveMessage();
                    if (message != null && "login".equals(message.getCommand())) {
                        Log.d("MESSAGE", message.toString());
                        if (message.getArgs()[0].equals("true")) {
                            ((Runnable[]) task.getData())[0].run();
                        } else {
                            ((Runnable[]) task.getData())[1].run();
                        }
                        break;
                    }
                }
            } break;
            default: {
                ///error
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            while (!isConnected) {
                BaseMessage task = pollTask();
                if (task != null) {
                    Log.d("MESSAGE", task.toString());
                }
                //if (task != null && "connect".equals(task.getCommand()) && !(isConnected = establishConnection()))
                if (task != null && "connect".equals(task.getCommand())) {
                    isConnected = establishConnection();
                    Log.d("CONNECTION_STATE", ""+isConnected);
                    if (!isConnected) {
                        ((Runnable)task.getData()).run();
                    }
                }
            }

            while (isConnected) {
                BaseMessage task = pollTask();
                if (task != null) {
                    Log.d("MESSAGE", task.toString());
                }
                if (task != null)
                    handleTask(task);
            }
        }
    }

    private boolean establishConnection() {
        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(host, port), 10000);
            new Thread(new OutputToServer(new ObjectOutputStream(socket.getOutputStream()), messagesToSend)).start();
            new Thread(new InputFromServer(new ObjectInputStream(socket.getInputStream()), receivedMessages)).start();
            Log.d("JD", "JD");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void loginToServer(String[] loginArgs) {
        sendMessage(new BaseMessage("login", loginArgs, null));
    }

    /*@SuppressWarnings("unchecked")
    public static void loginToServer(String noConnectionErrorMessage, LoginData loginData, printMessageInterface callbackMessage, startNewActivityInterface callbackActivity) {
        if (out != null) {
            new Thread(() -> {
                try {
                    out.writeObject(loginData);
                    final LoginConfirmationData loginConfirmation = (LoginConfirmationData) in.readObject();
                    eventData = (EventInfo) in.readObject();
                    callbackMessage.printMessage(loginConfirmation.message);
                    if (loginConfirmation.isLogged)
                        callbackActivity.startNewActivity();
                } catch (IOException | ClassNotFoundException e) {
                    callbackMessage.printMessage(e.getMessage());
                }
            }).start();
        } else
            callbackMessage.printMessage(noConnectionErrorMessage);
    }*/

    private static class InputFromServer implements Runnable {

        private ObjectInputStream in;
        private ConcurrentLinkedQueue<BaseMessage> receivedMessages;

        public InputFromServer(ObjectInputStream in, ConcurrentLinkedQueue<BaseMessage> receivedMessages) {
            this.in = in;
            this.receivedMessages = receivedMessages;
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
                Log.d("MESSAGE", message.toString());
                if (message != null) {
                    receivedMessages.offer(message);
                }
            }
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
