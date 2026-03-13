package server;

import shared.CommandParser;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private String username;
    private boolean loggedIn = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {

            this.input = reader;
            this.output = writer;

            output.println("Welcome to Secure Chat Server");
            output.println("Use REGISTER or LOGIN");

            String message;

            while ((message = input.readLine()) != null) {

                CommandParser parser = new CommandParser(message);
                String command = parser.getCommand();
                
                if (command.isEmpty()) continue;

                // Require authentication for chat commands
                if (!loggedIn && !command.equals("REGISTER") && !command.equals("LOGIN")) {
                    output.println("Please LOGIN or REGISTER first");
                    continue;
                }

                switch (command) {
                    case "REGISTER":
                        handleRegister(parser.getArgs());
                        break;
                    case "LOGIN":
                        handleLogin(parser.getArgs());
                        break;
                    case "MSG":
                        handlePrivateMessage(parser.getArgs());
                        break;
                    case "BROADCAST":
                        handleBroadcast(parser.getArgs());
                        break;
                    case "CLIENTS":
                        output.println("Active clients: " + ClientManager.getClientCount());
                        break;
                    case "QUIT":
                        output.println("Bye");
                        return;
                    default:
                        output.println("Unknown command");
                }

            }

        } catch (IOException e) {
            logger.info("Client disconnected or network error: " + socket.getInetAddress());
        } finally {

            try {
                if (username != null) {
                    ClientManager.removeClient(username);
                    MessageRouter.systemMessage(username + " left the chat");
                }
                
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                logger.severe("Error closing client socket.");
            }

        }

    }


    private void handleRegister(String[] args) {
        if (args.length < 2) {
            output.println("Usage: REGISTER username password");
            return;
        }

        boolean registered = UserStore.registerUser(args[0], args[1]);

        if (registered) {
            output.println("REGISTER_SUCCESS");
            logger.info("New user registered: " + args[0]);
        } else {
            output.println("Username already exists");
        }
    }


    private void handleLogin(String[] args) {
        if (args.length < 2) {
            output.println("Usage: LOGIN username password");
            return;
        }

        boolean valid = UserStore.verifyLogin(args[0], args[1]);

        if (valid) {
            boolean added = ClientManager.addClient(args[0], output);

            if (!added) {
                output.println("User already logged in");
                return;
            }

            username = args[0];
            loggedIn = true;

            output.println("LOGIN_SUCCESS");
            MessageRouter.systemMessage(username + " joined the chat");
            logger.info(username + " logged in");
        } else {
            output.println("LOGIN_FAILED");
        }
    }


    private void handlePrivateMessage(String[] args) {
        if (args.length < 2) {
            output.println("Usage: MSG username message");
            return;
        }
        
        // args[0] is target, args[1] is the rest of the message due to split limit of 3 in the parser
        MessageRouter.sendPrivate(username, args[0], args[1]);
    }

    private void handleBroadcast(String[] args) {

        if (args.length < 1) {
            output.println("Usage: BROADCAST message");
            return;
        }

        String msg = String.join(" ", args);

        MessageRouter.broadcast(username, msg);
    }

}