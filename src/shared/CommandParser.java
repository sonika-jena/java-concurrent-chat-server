package shared;

public class CommandParser {

    private String command;
    private String[] args;

    public CommandParser(String message) {
        if (message == null || message.trim().isEmpty()) {
            this.command = "";
            this.args = new String[0];
            return;
        }

        // Split by spaces, treating consecutive spaces as one, with a max split of 3
        String[] parts = message.trim().split("\\s+", 3);
        
        this.command = parts[0].toUpperCase();
        
        if (parts.length > 1) {
            this.args = new String[parts.length - 1];
            System.arraycopy(parts, 1, this.args, 0, parts.length - 1);
        } else {
            this.args = new String[0];
        }
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public int getArgCount() {
        return args.length;
    }
}
