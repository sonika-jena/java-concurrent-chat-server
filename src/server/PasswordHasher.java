package server;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class PasswordHasher {

    // Reusing the same SecureRandom instance is much more memory and CPU efficient
    private static final SecureRandom random = new SecureRandom();
    
    // PBKDF2 Constants
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public static String generateSalt() {

        byte[] salt = new byte[16];

        random.nextBytes(salt);

        StringBuilder hex = new StringBuilder();

        for (byte b : salt) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();

    }

    public static String hashPassword(String password, String saltHex) {

        try {
            
            // Convert hex salt back to bytes
            byte[] salt = new byte[saltHex.length() / 2];
            for (int i = 0; i < salt.length; i++) {
                salt[i] = (byte) Integer.parseInt(saltHex.substring(2 * i, 2 * i + 2), 16);
            }

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            
            byte[] hash = factory.generateSecret(spec).getEncoded();

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (Exception e) {

            throw new RuntimeException("Error hashing password with PBKDF2", e);

        }

    }

}