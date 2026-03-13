package server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    @Test
    public void testSaltGenerationLength() {
        String salt = PasswordHasher.generateSalt();
        assertNotNull(salt);
        // 16 bytes = 32 hex characters
        assertEquals(32, salt.length()); 
    }

    @Test
    public void testSaltsAreRandom() {
        String salt1 = PasswordHasher.generateSalt();
        String salt2 = PasswordHasher.generateSalt();
        assertNotEquals(salt1, salt2, "Two generated salts should not identical");
    }

    @Test
    public void testHashingIsDeterministic() {
        String password = "mypassword123";
        String salt = "f4c9a6239103b41d0859ef09decf37c0"; 
        
        String hash1 = PasswordHasher.hashPassword(password, salt);
        String hash2 = PasswordHasher.hashPassword(password, salt);
        
        assertEquals(hash1, hash2, "Hashing the same password and salt twice should produce the exact same hash");
    }

    @Test
    public void testHashingWithDifferentSalts() {
        String password = "mypassword123";
        String salt1 = PasswordHasher.generateSalt();
        String salt2 = PasswordHasher.generateSalt();
        
        String hash1 = PasswordHasher.hashPassword(password, salt1);
        String hash2 = PasswordHasher.hashPassword(password, salt2);
        
        assertNotEquals(hash1, hash2, "The exact same password hashed with different salts must produce vastly different hashes");
    }
}
