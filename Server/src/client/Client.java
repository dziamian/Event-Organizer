package client;

import java.io.*;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {

    final static String host = "localhost";
    final static int port = 9999;
    private static Scanner in;
    private static PrintWriter out;

    public static void main(String[] args) {
        for(int i = 0; i < 1; ++i) {
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(host, port), 10000);
                Scanner input = new Scanner(System.in);
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(),true);

                while(true) {
                    String line = in.nextLine();
                    if(line.startsWith("SUBMIT")) {
                        out.println("R_SUBMIT " + input.nextLine());
                    } else if(line.startsWith("UPDATE")) {
                        System.out.println(line);
                    } else if(line.startsWith("TIMED_OUT")) {
                        System.out.println("Connection timed out!");
                        return;
                    }
                }

            } catch (NoSuchElementException e) {
                System.out.println("Server is not responding!");
            } catch (ConnectException e) {
                System.out.println("Connection issues!");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                try { socket.close(); } catch(IOException e) { System.out.println(e.getMessage()); }
            }
        }
    }
}
