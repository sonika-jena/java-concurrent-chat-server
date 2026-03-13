package server;

import shared.Config;

import java.io.IOException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ChatServer {

    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    private final int port;
    private final ExecutorService threadPool;

    public ChatServer() {
        this.port = Config.getInt("server.port", 8010);
        int threadPoolSize = Config.getInt("server.threads.client", 500);
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void startServer() {

        String keystorePath = Config.getString("auth.keystore.path", "data/keystore.p12");
        String keystorePass = Config.getString("auth.keystore.pass", "changeit");

        System.setProperty("javax.net.ssl.keyStore", keystorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", keystorePass);

        // Graceful server shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Server is shutting down... notifying clients");
            MessageRouter.systemMessage("Server is instantly shutting down for maintenance.");
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
        }));

        try {

            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);

            logger.info("Secure Chat Server started on port " + port);

            while (true) {

                Socket clientSocket = serverSocket.accept();
                logger.info("Secure client connected: " + clientSocket.getInetAddress());

                threadPool.execute(new ClientHandler(clientSocket));

            }

        } catch (IOException e) {
            logger.severe("Could not start server. Ensure " + keystorePath + " exists and password is correct.");
            e.printStackTrace();
        }

    }

}