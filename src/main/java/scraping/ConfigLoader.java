package scraping;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    public static Properties loadConfig(String configFilePath) {
        Properties properties = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(configFilePath)) {
            if (input == null) {
                System.err.println("Configuration file not found: " + configFilePath);
                return null;
            }
            properties.load(input);
            return properties;
        } catch (Exception e) {
            System.err.println("Error loading configuration file: " + configFilePath);
            e.printStackTrace();
            return null;
        }
    }
}
