package server;

import shared.Config;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageRouter {

    // Queue for outgoing messages
    private static final BlockingQueue<String> messageQueue =
            new LinkedBlockingQueue<>();

    // Start broadcast worker threads
    static {

        int workers = Config.getInt("server.threads.broadcast", 4);

        for (int i = 0; i < workers; i++) {

            new Thread(() -> {

                while (true) {

                    try {

                        String msg = messageQueue.take();

                        var clientsSnapshot = ClientManager.getAllClients();

                        // send first message
                        for (PrintWriter writer : clientsSnapshot.values()) {
                            writer.println(msg);
                        }

                        // batch send additional queued messages
                        String next;
                        while ((next = messageQueue.poll()) != null) {

                            for (PrintWriter writer : clientsSnapshot.values()) {
                                writer.println(next);
                            }

                        }

                    } catch (InterruptedException ignored) {}

                }

            }, "broadcast-worker-" + i).start();

        }

    }


    // send private message
    public static void sendPrivate(String sender, String receiver, String message) {

        PrintWriter writer = ClientManager.getClient(receiver);

        if (writer != null) {
            writer.println("[PRIVATE] " + sender + ": " + message);
        }

    }

    // broadcast message
    public static void broadcast(String sender, String message) {

        messageQueue.offer("[BROADCAST] " + sender + ": " + message);

    }

    // system message
    public static void systemMessage(String message) {

        messageQueue.offer("[SERVER] " + message);

    }

}