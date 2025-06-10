package ftp.gusamyky.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Klasa zarządzająca konfiguracją aplikacji.
 * Singleton.
 */
public class ConfigManager {
    private static ConfigManager instance;
    private final Properties properties;
    private static final String CONFIG_FILE = "/config.properties";

    private ConfigManager() {
        properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration: " + e.getMessage(), e);
        }
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public String getServerHost() {
        return properties.getProperty("server.host", "localhost");
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "2121"));
    }

    public String getServerFilesDir() {
        return properties.getProperty("server.files.dir", "server_files");
    }

    public String getClientFilesDir() {
        return properties.getProperty("client.files.dir", "client_files");
    }

    public int getConnectionTimeout() {
        return Integer.parseInt(properties.getProperty("connection.timeout", "30000"));
    }

    public int getCloudRetryAttempts() {
        return Integer.parseInt(properties.getProperty("cloud.retry.attempts", "3"));
    }

    public int getCloudRetryDelay() {
        return Integer.parseInt(properties.getProperty("cloud.retry.delay", "5000"));
    }

    public int getCloudKeepaliveInterval() {
        return Integer.parseInt(properties.getProperty("cloud.keepalive.interval", "30000"));
    }
}