package server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class UserStoreTest {

    private static final String TEST_DB = "data/test_users.csv";

    @BeforeEach
    public void setUp() {
         // Re-point the internal Store to our dummy database
         UserStore.setFilePath(TEST_DB);
         
         // Delete test DB if it exists
         File f = new File(TEST_DB);
         if (f.exists()) {
             f.delete();
         }
         
         // Clear the in-memory map by forcing a load of an empty file
         UserStore.loadUsers();
    }

    @AfterAll
    public static void tearDown() {
         File f = new File(TEST_DB);
         if (f.exists()) {
             f.delete();
         }
    }

    @Test
    public void testRegistrationSavesAndLoads() {
        boolean success = UserStore.registerUser("alice", "secret123");
        assertTrue(success, "First registration should succeed");

        // Force reload from disk to prove appendUser() worked
        UserStore.loadUsers();
        
        boolean loginSuccess = UserStore.verifyLogin("alice", "secret123");
        assertTrue(loginSuccess, "Login should succeed after reloading from disk");
    }

    @Test
    public void testDuplicateRegistrationFails() {
        UserStore.registerUser("bob", "password");
        boolean secondAttempt = UserStore.registerUser("bob", "password");
        
        assertFalse(secondAttempt, "Duplicate username registration must fail");
    }

    @Test
    public void testIncorrectPasswordFails() {
        UserStore.registerUser("charlie", "correcthorse");
        
        boolean badLogin = UserStore.verifyLogin("charlie", "wrongbattery");
        assertFalse(badLogin, "Login with the wrong password must fail");
    }
    
    @Test
    public void testNonexistentUserLoginFails() {
        boolean login = UserStore.verifyLogin("ghost", "boo");
        assertFalse(login, "Login for a user that hasn't been registered must fail");
    }
}
