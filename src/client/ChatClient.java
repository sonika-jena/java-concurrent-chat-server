package client;

import shared.Config;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    private static final String SERVER_HOST =
            Config.getString("server.host", "localhost");
    private static final int SERVER_PORT = 8010;

    // Terminal colors
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";

    public static void main(String[] args) {

        // Configure SSL properties programmatically
        System.setProperty(
                "javax.net.ssl.trustStore",
                Config.getString("auth.keystore.path", "data/keystore.p12")
        );

        System.setProperty(
                "javax.net.ssl.trustStorePassword",
                Config.getString("auth.keystore.pass", "changeit")
        );

        try {

            javax.net.ssl.SSLSocketFactory sf =
                    (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();

            Socket socket = sf.createSocket(SERVER_HOST, SERVER_PORT);

            System.out.println(GREEN + "Connected securely to server" + RESET);

            System.out.println("\nCommands:");
            System.out.println("REGISTER <username> <password>");
            System.out.println("LOGIN <username> <password>");
            System.out.println("MSG <username> <message>");
            System.out.println("BROADCAST <message>");
            System.out.println("CLIENTS");
            System.out.println("QUIT\n");

            BufferedReader keyboard =
                    new BufferedReader(new InputStreamReader(System.in));

            BufferedReader serverInput =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));

            PrintWriter serverOutput =
                    new PrintWriter(socket.getOutputStream(), true);

            // Thread to listen for server messages
            new Thread(() -> {
                try {
                    String response;

                    while ((response = serverInput.readLine()) != null) {

                        if (response.startsWith("[BROADCAST]")) {
                            System.out.println("\n" + BLUE + response + RESET);
                        }
                        else if (response.startsWith("[PRIVATE]")) {
                            System.out.println("\n" + YELLOW + response + RESET);
                        }
                        else if (response.startsWith("[SERVER]")) {
                            System.out.println("\n" + GREEN + response + RESET);
                        }
                        else {
                            System.out.println("\n" + response);
                        }

                        System.out.print("> ");
                    }

                } catch (IOException e) {
                    System.out.println(RED + "\nDisconnected from server" + RESET);
                }
            }).start();

            // Input loop
            while (true) {

                System.out.print("> ");
                String message = keyboard.readLine();

                if (message == null)
                    break;

                serverOutput.println(message);

                if (message.equalsIgnoreCase("QUIT"))
                    break;
            }

            socket.close();

        } catch (Exception e) {

            System.err.println(
                    RED + "Could not connect to server. Ensure it is running and keystore exists." + RESET
            );

            e.printStackTrace();
        }
    }
}




//package client;
//
//import shared.Config;
//
//import java.io.*;
//import java.net.Socket;
//
//public class ChatClient {
//
//    private static final String SERVER_HOST =
//            Config.getString("server.host", "localhost");
//    private static final int SERVER_PORT = 8010;
//
//    public static void main(String[] args) {
//
//        // Configure SSL properties programmatically to trust the self-signed certificate we made
//        System.setProperty(
//                "javax.net.ssl.trustStore",
//                Config.getString("auth.keystore.path", "data/keystore.p12")
//        );
//
//        System.setProperty(
//                "javax.net.ssl.trustStorePassword",
//                Config.getString("auth.keystore.pass", "changeit")
//        );
//
//        try {
//
//            javax.net.ssl.SSLSocketFactory sf = (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();
//            Socket socket = sf.createSocket(SERVER_HOST, SERVER_PORT);
//
//            System.out.println("Connected securely to server");
//
//            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
//            BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);
//
//            // Thread to listen for server messages
//            new Thread(() -> {
//                try {
//                    String response;
//                    while ((response = serverInput.readLine()) != null) {
//                        System.out.println(response);
//                    }
//                } catch (IOException e) {
//                    System.out.println("Disconnected from server");
//                }
//            }).start();
//
//            while (true) {
//                String message = keyboard.readLine();
//                if (message == null) break;
//
//                serverOutput.println(message);
//                if (message.equalsIgnoreCase("QUIT")) {
//                    break;
//                }
//            }
//
//            socket.close();
//
//        } catch (Exception e) {
//            System.err.println("Could not connect to server. Ensure it is running and keystore exists.");
//            e.printStackTrace();
//        }
//    }
//
//}