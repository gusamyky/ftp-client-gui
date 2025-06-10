package ftp.gusamyky.client.service.command;

import ftp.gusamyky.client.service.error.NetworkErrorHandler;
import ftp.gusamyky.client.service.network.ServerMessageHandler;
import ftp.gusamyky.client.util.ConfigManager;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.DoubleConsumer;

public class DownloadFileCommand implements NetworkCommand<Boolean> {
    private static final int BUFFER_SIZE = 16384; // 16KB buffer
    private static final int PROGRESS_LOG_INTERVAL = 10; // Log every 10%

    private final String filename;
    private final Path localPath;
    private final BufferedWriter writer;
    private final ServerMessageHandler messageHandler;
    private final NetworkErrorHandler errorHandler;
    private final DoubleConsumer onProgress;
    private final Socket socket;
    private final ConfigManager configManager;

    public DownloadFileCommand(String filename, Path localPath, BufferedWriter writer,
            ServerMessageHandler messageHandler, Socket socket, DoubleConsumer onProgress) {
        this.filename = filename;
        this.localPath = localPath;
        this.writer = writer;
        this.messageHandler = messageHandler;
        this.errorHandler = NetworkErrorHandler.getInstance();
        this.onProgress = onProgress;
        this.socket = socket;
        this.configManager = ConfigManager.getInstance();
    }

    private Path getDefaultDownloadPath() {
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

    @Override
    public String getOperationName() {
        return "pobierania pliku";
    }

    @Override
    public Boolean execute() throws Exception {
        if (filename == null || filename.isEmpty()) {
            errorHandler.handleFileError(getOperationName(), "Nazwa pliku nie może być pusta");
            return false;
        }

        Path targetPath = localPath != null ? localPath : getDefaultDownloadPath();
        System.out.println("[Download] Saving file to: " + targetPath);

        writer.write("DOWNLOAD " + filename + "\n");
        writer.flush();

        String response = messageHandler.readLine(getOperationName());
        if (messageHandler.isErrorResponse(response)) {
            errorHandler.handleFileError(getOperationName(), messageHandler.getErrorMessage(response));
            return false;
        }

        long fileSize;
        try {
            fileSize = Long.parseLong(response.trim());
        } catch (NumberFormatException e) {
            errorHandler.handleFileError(getOperationName(), "Nieprawidłowy format rozmiaru pliku");
            return false;
        }

        if (fileSize <= 0) {
            errorHandler.handleFileError(getOperationName(), "Nieprawidłowy rozmiar pliku");
            return false;
        }

        try {
            Files.createDirectories(targetPath.getParent());
            try (OutputStream fileOut = Files.newOutputStream(targetPath)) {
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                long received = 0;
                int lastProgressPercent = 0;

                while (received < fileSize) {
                    try {
                        int toRead = (int) Math.min(buffer.length, fileSize - received);
                        int read = in.read(buffer, 0, toRead);
                        if (read == -1) {
                            errorHandler.handleFileError(getOperationName(), "Nieoczekiwany koniec strumienia");
                            return false;
                        }
                        fileOut.write(buffer, 0, read);
                        received += read;

                        // Calculate and report progress
                        if (onProgress != null && fileSize > 0) {
                            double progress = (double) received / fileSize;
                            int currentProgressPercent = (int) (progress * 100);

                            // Log progress every 10%
                            if (currentProgressPercent >= lastProgressPercent + PROGRESS_LOG_INTERVAL) {
                                System.out.printf("[Download] Progress: %d%% (%d/%d bytes)%n",
                                        currentProgressPercent, received, fileSize);
                                lastProgressPercent = currentProgressPercent;
                            }

                            final double finalProgress = progress;
                            Platform.runLater(() -> onProgress.accept(finalProgress));
                        }
                    } catch (SocketException e) {
                        errorHandler.handleFileError(getOperationName(),
                                "Połączenie zostało przerwane: " + e.getMessage());
                        return false;
                    }
                }

                if (received != fileSize) {
                    errorHandler.handleFileError(getOperationName(), "Niekompletny transfer");
                    return false;
                }

                System.out.println("[Download] Transfer completed successfully: " + filename);
                return true;
            }
        } catch (IOException e) {
            errorHandler.handleFileError(getOperationName(), "Błąd podczas zapisu pliku: " + e.getMessage());
            return false;
        }
    }
}