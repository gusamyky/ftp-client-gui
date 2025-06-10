package ftp.gusamyky.client.service.command;

import ftp.gusamyky.client.model.RemoteFile;
import ftp.gusamyky.client.service.error.NetworkErrorHandler;
import ftp.gusamyky.client.service.network.ServerMessageHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

public class FetchRemoteFilesCommand implements NetworkCommand<ObservableList<RemoteFile>> {
    private final BufferedWriter writer;
    private final ServerMessageHandler messageHandler;
    private final NetworkErrorHandler errorHandler;

    public FetchRemoteFilesCommand(BufferedWriter writer, ServerMessageHandler messageHandler) {
        this.writer = writer;
        this.messageHandler = messageHandler;
        this.errorHandler = NetworkErrorHandler.getInstance();
    }

    @Override
    public String getOperationName() {
        return "pobierania listy plików";
    }

    @Override
    public ObservableList<RemoteFile> execute() throws Exception {
        List<RemoteFile> tempFiles = new ArrayList<>();
        ObservableList<RemoteFile> files = FXCollections.observableArrayList();

        writer.write("LIST\n");
        writer.flush();

        String response = messageHandler.readLine(getOperationName());
        if (messageHandler.isErrorResponse(response)) {
            errorHandler.handleError(getOperationName(), messageHandler.getErrorMessage(response));
            return files;
        }

        if (!response.startsWith("FILES:")) {
            errorHandler.handleError(getOperationName(), "Nieoczekiwana odpowiedź z serwera");
            return files;
        }

        String fileList = response.substring(6).trim();
        if (!fileList.equals("(no files)")) {
            String[] fileArr = fileList.split(" ");
            for (String f : fileArr) {
                if (!f.isBlank()) {
                    tempFiles.add(new RemoteFile(f, "?"));
                }
            }
        }

        Platform.runLater(() -> files.setAll(tempFiles));
        return files;
    }
}