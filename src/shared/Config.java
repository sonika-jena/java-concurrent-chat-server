package shared;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {

    private static final Logger logger = Logger.getLogger(Config.class.getName());
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream in = new FileInputStream("config.properties")) {
            properties.load(in);
            logger.info("Loaded config.properties successfully.");
        } catch (IOException e) {
            logger.warning("Could not load config.properties. Falling back to default values.");
        }
    }

    public static int getInt(String key, int defaultValue) {
        String val = properties.getProperty(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue).trim();
    }
}
