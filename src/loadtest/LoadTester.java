package loadtest;

import shared.Config;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTester implements Runnable {

    private static final int SERVER_PORT =
            Config.getInt("server.port", 8010);
    private static final String SERVER_HOST = "localhost";
    private static final int MESSAGES_PER_CLIENT = 10;

    private static AtomicInteger successfulConnections = new AtomicInteger(0);
    private static AtomicInteger authFailures = new AtomicInteger(0);
    private static AtomicInteger connectionFailures = new AtomicInteger(0);
    
    private static AtomicInteger messagesSent = new AtomicInteger(0);
    private static AtomicInteger messagesReceived = new AtomicInteger(0);
    
    // Track latency over the entire test
    private static AtomicLong totalDeliveryLatency = new AtomicLong(0);
    private static AtomicLong maxDeliveryLatency = new AtomicLong(0);

    private int clientId;

    public LoadTester(int clientId) {
        this.clientId = clientId;
    }

    @Override
    public void run() {

        try {
            // Configure SSL to trust the server
            System.setProperty("javax.net.ssl.trustStore", "data/keystore.p12");
            System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket socket;
            
            try {
                socket = sf.createSocket(SERVER_HOST, SERVER_PORT);
            } catch (IOException e) {
                connectionFailures.incrementAndGet();
                return;
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            String username = "user" + clientId;
            String password = "pass";

            // Ignore welcome messages
            input.readLine(); // Welcome
            input.readLine(); // Use REG/LOG

            // register and login
            output.println("REGISTER " + username + " " + password);
            input.readLine(); // Read register response
            
            output.println("LOGIN " + username + " " + password);
            String loginResponse = input.readLine();

            if (loginResponse == null || !loginResponse.equals("LOGIN_SUCCESS")) {
                authFailures.incrementAndGet();
                socket.close();
                return;
            }

            successfulConnections.incrementAndGet();

            // Thread to listen for incoming broadcasts and calculate latency
            Thread listener = new Thread(() -> {
                try {
                    String response;
                    while ((response = input.readLine()) != null) {
                        if (response.startsWith("[BROADCAST]")) {
                            messagesReceived.incrementAndGet();
                            
                            // Extract timestamp from the payload if it exists
                            String[] parts = response.split("TS:");
                            if (parts.length > 1) {
                                try {
                                    long sentTime = Long.parseLong(parts[1].trim());
                                    long latency = System.currentTimeMillis() - sentTime;
                                    
                                    totalDeliveryLatency.addAndGet(latency);
                                    
                                    // Update max latency thread-safely
                                    maxDeliveryLatency.updateAndGet(currentMax -> Math.max(currentMax, latency));
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                } catch (IOException ignored) {}
            });
            listener.start();


            // send messages with timestamp payload
            for (int i = 0; i < MESSAGES_PER_CLIENT; i++) {
                
                long timestamp = System.currentTimeMillis();
                output.println("BROADCAST hello TS:" + timestamp);
                messagesSent.incrementAndGet();
                
                Thread.sleep(100);
            }

            // Allow time for final broadcasts to arrive before quitting
            Thread.sleep(2000);

            output.println("QUIT");
            socket.close();

        } catch (Exception e) {
            connectionFailures.incrementAndGet();
        }

    }

    public static void main(String[] args) {

        int numberOfClients = 500; // Reduced from 2000 to prevent local OS port exhaustion during testing

        System.out.println("Starting load test with " + numberOfClients + " clients...");
        System.out.println("Each client will send " + MESSAGES_PER_CLIENT + " broadcasts.");
        
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= numberOfClients; i++) {
            Thread thread = new Thread(new LoadTester(i));
            thread.start();
        }

        // Add shutdown hook to print beautiful metrics on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            long endTime = System.currentTimeMillis();
            long totalTimeMs = endTime - startTime;
            double totalTimeSec = totalTimeMs / 1000.0;
            
            int totalRcv = messagesReceived.get();
            double avgLatency = totalRcv > 0 ? (double) totalDeliveryLatency.get() / totalRcv : 0;
            double tps = totalRcv / totalTimeSec;

            System.out.println("\n===== ADVANCED LOAD TEST RESULTS =====");
            System.out.println("--- Connection Stats ---");
            System.out.println("Successful Connections: " + successfulConnections.get());
            System.out.println("Connection Failures:    " + connectionFailures.get());
            System.out.println("Authentication Fails:   " + authFailures.get());
            
            System.out.println("\n--- Traffic Stats ---");
            System.out.println("Messages Sent (Up):     " + messagesSent.get());
            System.out.println("Messages Rcvd (Down):   " + totalRcv);
            
            System.out.println("\n--- Performance Metrics ---");
            System.out.println(String.format("Total Test Duration:    %.2f seconds", totalTimeSec));
            System.out.println(String.format("System Throughput:      %,.0f messages/sec", tps));
            System.out.println(String.format("Average Latency:        %.2f ms", avgLatency));
            System.out.println("Max Delivery Latency:   " + maxDeliveryLatency.get() + " ms");
            
            System.out.println("======================================");

        }));

    }

}