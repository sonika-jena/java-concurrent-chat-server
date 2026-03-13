package server;

public class Main {

    public static void main(String[] args) {

        UserStore.loadUsers();

        ChatServer server = new ChatServer();
        server.startServer();

    }
}