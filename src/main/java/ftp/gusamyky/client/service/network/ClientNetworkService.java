package ftp.gusamyky.client.service.network;

import ftp.gusamyky.client.model.HistoryItem;
import ftp.gusamyky.client.model.RemoteFile;
import ftp.gusamyky.client.model.User;
import ftp.gusamyky.client.service.command.*;
import ftp.gusamyky.client.service.error.NetworkErrorHandler;
import ftp.gusamyky.client.util.ConfigManager;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.DoubleConsumer;

public class ClientNetworkService {
    private static ClientNetworkService instance;
    private final ReentrantLock lock = new ReentrantLock();
    private final ConfigManager configManager = ConfigManager.getInstance();
    private final NetworkErrorHandler errorHandler = NetworkErrorHandler.getInstance();
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ServerMessageHandler messageHandler;
    private static final int SOCKET_BUFFER_SIZE = 65536;
    private static final int SOCKET_TIMEOUT = 300000;

    private ClientNetworkService() {
    }

    public static synchronized ClientNetworkService getInstance() {
        if (instance == null) {
            instance = new ClientNetworkService();
        }
        return instance;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() && reader != null && writer != null;
    }

    public void connect(String host, int port) throws IOException {
        lock.lock();
        try {
            if (isConnecting.get()) {
                System.out.println("[ClientNetworkService] Connection attempt already in progress");
                throw new IOException("Connection attempt already in progress");
            }
            isConnecting.set(true);

            int attempts = 0;
            IOException lastException = null;

            while (attempts < configManager.getCloudRetryAttempts()) {
                try {
                    System.out.println("[ClientNetworkService] Attempting to connect to " + host + ":" + port +
                            " (Attempt " + (attempts + 1) + "/" + configManager.getCloudRetryAttempts() + ")");

                    if (isConnected()) {
                        System.out
                                .println("[ClientNetworkService] Disconnecting existing connection before new attempt");
                        disconnect();
                    }

                    socket = new Socket(host, port);
                    socket.setSoTimeout(SOCKET_TIMEOUT);
                    socket.setKeepAlive(true);
                    socket.setTcpNoDelay(true);
                    socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
                    socket.setSendBufferSize(SOCKET_BUFFER_SIZE);

                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    messageHandler = new ServerMessageHandler(reader);

                    StringBuilder welcomeMsg = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if ("END".equals(line))
                            break;
                        welcomeMsg.append(line).append("\n");
                    }
                    if (welcomeMsg.length() == 0) {
                        throw new IOException("No welcome message received from server");
                    }

                    System.out.println("[ClientNetworkService] Successfully connected to " + host + ":" + port +
                            ". Welcome message: " + welcomeMsg.toString().trim());
                    errorHandler.resetErrorState();
                    return;
                } catch (IOException e) {
                    lastException = e;
                    attempts++;
                    System.out.println(
                            "[ClientNetworkService] Connection attempt " + attempts + " failed: " + e.getMessage());

                    if (attempts < configManager.getCloudRetryAttempts()) {
                        System.out.println("[ClientNetworkService] Waiting " + configManager.getCloudRetryDelay() +
                                "ms before next attempt...");
                        try {
                            Thread.sleep(configManager.getCloudRetryDelay());
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Connection interrupted", ie);
                        }
                    }
                }
            }
            System.out
                    .println("[ClientNetworkService] Failed to connect after " + attempts + " attempts. Last error: " +
                            lastException.getMessage());
            throw new IOException(
                    "Failed to connect after " + attempts + " attempts. Last error: " + lastException.getMessage());
        } finally {
            isConnecting.set(false);
            lock.unlock();
        }
    }

    public void disconnect() {
        System.out.println("[ClientNetworkService] Disconnecting from server...");
        lock.lock();
        try {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
                reader = null;
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
                writer = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                socket = null;
            }
            messageHandler = null;
            System.out.println("[ClientNetworkService] Successfully disconnected");
        } finally {
            lock.unlock();
        }
    }

    private boolean checkConnectionAndNotify(String operation) {
        if (!isConnected()) {
            errorHandler.handleError(operation, "Brak połączenia z serwerem");
            return false;
        }
        return true;
    }

    public boolean login(String username, String password) {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("logowania"))
                return false;
            try {
                LoginCommand command = new LoginCommand(username, password, writer, messageHandler);
                return command.execute();
            } catch (Exception e) {
                disconnect();
                errorHandler.handleError("logowania", e.getMessage());
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean register(String username, String password) {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("rejestracji"))
                return false;
            try {
                RegisterCommand command = new RegisterCommand(username, password, writer, messageHandler);
                return command.execute();
            } catch (Exception e) {
                disconnect();
                errorHandler.handleError("rejestracji", e.getMessage());
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public ObservableList<RemoteFile> fetchRemoteFiles() {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("pobierania listy plików"))
                return null;
            try {
                FetchRemoteFilesCommand command = new FetchRemoteFilesCommand(writer, messageHandler);
                return command.execute();
            } catch (Exception e) {
                disconnect();
                errorHandler.handleError("pobierania listy plików", e.getMessage());
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean downloadFile(String filename) {
        return downloadFile(filename, getDefaultDownloadPath(filename));
    }

    public boolean downloadFile(String filename, Path localPath) {
        return downloadFileWithProgress(filename, localPath, null);
    }

    public Path getDefaultDownloadPath(String filename) {
        String userHome = System.getProperty("user.home");
        Path downloads = Path.of(userHome, configManager.getClientFilesDir());
        if (!Files.exists(downloads)) {
            downloads = Path.of(userHome, "Downloads");
        }
        if (!Files.exists(downloads)) {
            downloads = Path.of(userHome, "Pobrane");
        }
        return downloads.resolve(filename);
    }

    public boolean uploadFile(File file) {
        return uploadFileWithProgress(file, null);
    }

    public ObservableList<HistoryItem> fetchHistory(User user) {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("pobierania historii"))
                return null;
            try {
                FetchHistoryCommand command = new FetchHistoryCommand(user, writer, messageHandler);
                return command.execute();
            } catch (Exception e) {
                disconnect();
                errorHandler.handleError("pobierania historii", e.getMessage());
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public String sendCommand(String command) {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("wysyłania komendy"))
                return "Brak połączenia z serwerem.";
            try {
                writer.write(command + "\n");
                writer.flush();
                return messageHandler.readLine("wysyłania komendy");
            } catch (IOException e) {
                disconnect();
                errorHandler.handleError("wysyłania komendy", e.getMessage());
                return "Błąd: " + e.getMessage();
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean downloadFileWithProgress(String filename, Path localPath, DoubleConsumer onProgress) {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("pobierania pliku"))
                return false;
            try {
                DownloadFileCommand command = new DownloadFileCommand(filename, localPath, writer, messageHandler,
                        socket, onProgress);
                return command.execute();
            } catch (Exception e) {
                disconnect();
                errorHandler.handleError("pobierania pliku", e.getMessage());
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean uploadFileWithProgress(File file, DoubleConsumer onProgress) {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("wysyłania pliku"))
                return false;
            try {
                UploadFileCommand command = new UploadFileCommand(file, writer, messageHandler, socket, onProgress);
                return command.execute();
            } catch (Exception e) {
                disconnect();
                errorHandler.handleError("wysyłania pliku", e.getMessage());
                return false;
            }
        } finally {
            lock.unlock();
        }
    }
}