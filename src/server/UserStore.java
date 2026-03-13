package server;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserStore {

    // Changed from static final so tests can override the file path
    private static String FILE_PATH = "data/users.csv";

    // Allow tests to override the save file location
    public static void setFilePath(String path) {
        FILE_PATH = path;
    }

    private static Map<String, User> users = new ConcurrentHashMap<>();

    public static void loadUsers() {

        users.clear();

        try {

            File file = new File(FILE_PATH);

            if (!file.exists()) {

                file.getParentFile().mkdirs();
                file.createNewFile();

                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        users.put(parts[0], new User(parts[0], parts[1], parts[2]));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized static void appendUser(User user) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {

            writer.write(user.getUsername() + "," + user.getSalt() + "," + user.getPasswordHash());
            writer.newLine();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized static boolean registerUser(String username, String password) {

        if (users.containsKey(username)) {
            return false;
        }

        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);
        User newUser = new User(username, salt, hash);

        users.put(username, newUser);
        appendUser(newUser);

        return true;

    }

    public synchronized static boolean verifyLogin(String username, String password) {

        User user = users.get(username);
        if (user != null) {
            String hash = PasswordHasher.hashPassword(password, user.getSalt());
            return hash.equals(user.getPasswordHash());
        }

        return false;

    }

}