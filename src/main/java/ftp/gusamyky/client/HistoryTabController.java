package ftp.gusamyky.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class HistoryTabController {
    @FXML
    private Button refreshHistoryButton;
    @FXML
    private TextArea historyArea;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String loggedUser;

    public void setSocket(Socket socket, BufferedReader reader, BufferedWriter writer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
    }

    public void setLoggedUser(String username) {
        this.loggedUser = username;
    }

    @FXML
    public void initialize() {
        refreshHistoryButton.setOnAction(e -> fetchHistory());
    }

    private void fetchHistory() {
        if (loggedUser == null || loggedUser.isEmpty()) {
            historyArea.setText("Zaloguj się, aby zobaczyć historię operacji.");
            return;
        }
        try {
            writer.write("HISTORY " + loggedUser + "\n");
            writer.flush();
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            if (line == null) {
                historyArea.setText("Błąd połączenia z serwerem.");
                return;
            }
            if (line.startsWith("HISTORY:")) {
                sb.append(line.substring(8)).append("\n");
                while (reader.ready()) {
                    String l = reader.readLine();
                    if (l == null || l.isEmpty())
                        break;
                    sb.append(l).append("\n");
                }
                historyArea.setText(sb.toString());
                historyArea.setScrollTop(Double.MAX_VALUE);
            } else {
                historyArea.setText(line);
            }
        } catch (IOException e) {
            historyArea.setText("Błąd: " + e.getMessage());
        }
    }
}