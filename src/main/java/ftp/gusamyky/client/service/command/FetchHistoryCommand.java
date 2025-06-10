package ftp.gusamyky.client.service.command;

import ftp.gusamyky.client.model.HistoryItem;
import ftp.gusamyky.client.model.User;
import ftp.gusamyky.client.service.network.ServerMessageHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedWriter;

public class FetchHistoryCommand implements NetworkCommand<ObservableList<HistoryItem>> {
    private final User user;
    private final BufferedWriter writer;
    private final ServerMessageHandler messageHandler;

    public FetchHistoryCommand(User user, BufferedWriter writer, ServerMessageHandler messageHandler) {
        this.user = user;
        this.writer = writer;
        this.messageHandler = messageHandler;
    }

    @Override
    public String getOperationName() {
        return "FETCH_HISTORY";
    }

    @Override
    public ObservableList<HistoryItem> execute() throws Exception {
        ObservableList<HistoryItem> history = FXCollections.observableArrayList();

        if (user == null) {
            history.add(new HistoryItem("Zaloguj się, aby zobaczyć historię operacji.", ""));
            return history;
        }

        writer.write("HISTORY " + user.getUsername() + "\n");
        writer.flush();

        String line = messageHandler.readLine(getOperationName());
        if (messageHandler.isErrorResponse(line)) {
            history.add(new HistoryItem(messageHandler.getErrorMessage(line), ""));
            return history;
        }

        if (!line.startsWith("HISTORY:")) {
            history.add(new HistoryItem(line, ""));
            return history;
        }

        String content = line.substring(8).trim();
        if (content.equals("(no operations)")) {
            history.add(new HistoryItem("Brak operacji w historii.", ""));
            return history;
        }

        // Split history entries by semicolon
        String[] entries = content.split(";");
        for (String entry : entries) {
            entry = entry.trim();
            if (!entry.isEmpty()) {
                history.add(new HistoryItem(entry, ""));
            }
        }

        return history;
    }
}