package server;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {

    private static Map<String, PrintWriter> clients =
            new ConcurrentHashMap<>();

    public static boolean addClient(String username, PrintWriter writer) {

        return clients.putIfAbsent(username, writer) == null;

    }

    public static void removeClient(String username) {

        clients.remove(username);
    }

    public static PrintWriter getClient(String username) {

        return clients.get(username);
    }

    public static Map<String, PrintWriter> getAllClients() {
//        return Collections.unmodifiableMap(clients);
        return  clients;
    }

    public static int getClientCount() {

        return clients.size();
    }
}