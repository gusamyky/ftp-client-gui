package ftp.gusamyky.client.service.command;

import ftp.gusamyky.client.service.error.NetworkErrorHandler;
import ftp.gusamyky.client.service.network.ServerMessageHandler;
import ftp.gusamyky.client.util.FileVerificationUtil;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.function.DoubleConsumer;

public class UploadFileCommand implements NetworkCommand<Boolean> {
    private final File file;
    private final BufferedWriter writer;
    private final ServerMessageHandler messageHandler;
    private final NetworkErrorHandler errorHandler;
    private final DoubleConsumer onProgress;
    private final Socket socket;

    public UploadFileCommand(File file, BufferedWriter writer, ServerMessageHandler messageHandler, Socket socket,
            DoubleConsumer onProgress) {
        this.file = file;
        this.writer = writer;
        this.messageHandler = messageHandler;
        this.errorHandler = NetworkErrorHandler.getInstance();
        this.onProgress = onProgress;
        this.socket = socket;
    }

    @Override
    public String getOperationName() {
        return "wysyłania pliku";
    }

    @Override
    public Boolean execute() throws Exception {
        if (!FileVerificationUtil.verifyFile(file, getOperationName())) {
            return false;
        }

        String filename = file.getName();
        writer.write("UPLOAD " + filename + "\n");
        writer.flush();

        String ready = messageHandler.readLine(getOperationName());
        if (!messageHandler.isReadyResponse(ready)) {
            if (messageHandler.isErrorResponse(ready)) {
                errorHandler.handleFileError(getOperationName(), messageHandler.getErrorMessage(ready));
            } else {
                errorHandler.handleFileError(getOperationName(), "Nieoczekiwana odpowiedź z serwera");
            }
            return false;
        }

        long fileSize = file.length();
        writer.write(fileSize + "\n");
        writer.flush();

        try (InputStream fileIn = new FileInputStream(file)) {
            OutputStream out = new BufferedOutputStream(socket.getOutputStream());
            byte[] buffer = new byte[8192];
            long sent = 0;
            int read;
            long lastProgressTime = System.currentTimeMillis();
            final long TIMEOUT_MS = 30000;

            while ((read = fileIn.read(buffer)) != -1) {
                if (System.currentTimeMillis() - lastProgressTime > TIMEOUT_MS) {
                    errorHandler.handleFileError(getOperationName(), "Przekroczono czas oczekiwania");
                    return false;
                }

                out.write(buffer, 0, read);
                sent += read;
                lastProgressTime = System.currentTimeMillis();

                if (onProgress != null && fileSize > 0) {
                    double progress = (double) sent / fileSize;
                    final double finalProgress = progress;
                    Platform.runLater(() -> onProgress.accept(finalProgress));
                }
            }
            out.flush();

            if (sent != fileSize) {
                errorHandler.handleFileError(getOperationName(), "Niekompletne dane");
                return false;
            }
        }

        String response = messageHandler.readLine(getOperationName());
        if (messageHandler.isErrorResponse(response)) {
            errorHandler.handleFileError(getOperationName(), messageHandler.getErrorMessage(response));
            return false;
        }

        if (!messageHandler.isOkResponse(response)) {
            errorHandler.handleFileError(getOperationName(), "Nieoczekiwana odpowiedź z serwera");
            return false;
        }

        return true;
    }
}