package server;

public class User {

    private String username;
    private String salt;
    private String passwordHash;

    public User(String username, String salt, String passwordHash) {

        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;

    }

    public String getUsername() {
        return username;
    }

    public String getSalt() {
        return salt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

}