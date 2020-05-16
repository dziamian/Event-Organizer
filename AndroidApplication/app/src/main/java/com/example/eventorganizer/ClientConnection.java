package com.example.eventorganizer;

import network_structures.EventData;
import network_structures.LoginData;
import network_structures.LoginConfirmationData;
import network_structures.SectorInfo;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

public class ClientConnection {
    final static String host = "10.0.2.2";
    final static int port = 9999;
    static ObjectInputStream in = null;
    static ObjectOutputStream out = null;
    static Socket socket = null;

    static EventData eventData;

    interface printMessageInterface {
        void printMessage(String msg);
    }

    interface startNewActivityInterface {
        void startNewActivity();
    }

    public static void establishConnection(String successConnectionMessage, printMessageInterface callback) {
        new Thread(() -> {
            //while (true) {
                String message = successConnectionMessage;
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(host,port),10000);
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());
                    //break;
                } catch (IOException ex) {
                    message = ex.getMessage();
                } finally {
                    callback.printMessage(message);
                }
            //}
        }).start();
    }

    @SuppressWarnings("unchecked")
    public static void loginToServer(String noConnectionErrorMessage, LoginData loginData, printMessageInterface callbackMessage, startNewActivityInterface callbackActivity) {
        if (out != null) {
            new Thread(() -> {
                try {
                    out.writeObject(loginData);
                    final LoginConfirmationData loginConfirmation = (LoginConfirmationData) in.readObject();
                    eventData = (EventData) in.readObject();
                    callbackMessage.printMessage(loginConfirmation.message);
                    if (loginConfirmation.isLogged)
                        callbackActivity.startNewActivity();
                } catch (IOException | ClassNotFoundException e) {
                    callbackMessage.printMessage(e.getMessage());
                }
            }).start();
        } else
            callbackMessage.printMessage(noConnectionErrorMessage);
    }
}
