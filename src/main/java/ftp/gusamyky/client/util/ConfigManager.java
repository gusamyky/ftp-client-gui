package ftp.gusamyky.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for managing application configuration.
 * Loads properties from config.properties file and provides access to them.
 */
public class ConfigManager {
    private static final String CONFIG_FILE = "/config.properties";
    private static ConfigManager instance;
    private final Properties properties;

    private ConfigManager() {
        properties = new Properties();
        loadProperties();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration: " + e.getMessage(), e);
        }
    }

    public String getServerHost() {
        return properties.getProperty("server.host", "localhost");
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "2121"));
    }

    public String getClientDownloadsDir() {
        return properties.getProperty("client.downloads.dir", "client_files");
    }

    public int getConnectionTimeout() {
        return Integer.parseInt(properties.getProperty("client.connection.timeout", "5000"));
    }
}