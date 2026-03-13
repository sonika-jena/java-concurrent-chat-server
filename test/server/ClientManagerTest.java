package server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ClientManagerTest {

    @BeforeEach
    public void setUp() {
         // Clear clients before each test
         for (String key : ClientManager.getAllClients().keySet()) {
             ClientManager.removeClient(key);
         }
    }

    @Test
    public void testAddClient() {
        PrintWriter dummyWriter = new PrintWriter(new StringWriter());
        
        boolean success = ClientManager.addClient("testUser", dummyWriter);
        assertTrue(success, "Should successfully add a new client");
        assertEquals(1, ClientManager.getClientCount(), "Client count should be 1");
    }

    @Test
    public void testDuplicateClientLogin() {
        PrintWriter dummyWriter1 = new PrintWriter(new StringWriter());
        PrintWriter dummyWriter2 = new PrintWriter(new StringWriter());
        
        ClientManager.addClient("testUser", dummyWriter1);
        boolean duplicateSuccess = ClientManager.addClient("testUser", dummyWriter2);
        
        assertFalse(duplicateSuccess, "Should NOT allow a duplicate username to be added");
        assertEquals(1, ClientManager.getClientCount(), "Client count should still be 1 after failed duplicate addition");
    }

    @Test
    public void testRemoveClient() {
        PrintWriter dummyWriter = new PrintWriter(new StringWriter());
        ClientManager.addClient("testUser", dummyWriter);
        
        ClientManager.removeClient("testUser");
        
        assertEquals(0, ClientManager.getClientCount(), "Client count should be 0 after removal");
        assertNull(ClientManager.getClient("testUser"), "Getting a removed client should return null");
    }
}
