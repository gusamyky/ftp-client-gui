package ftp.gusamyky.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class ConsoleTabController {
    @FXML
    private TextArea consoleArea;
    @FXML
    private TextField inputField;
    @FXML
    private Button sendButton;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void setSocket(Socket socket, BufferedReader reader, BufferedWriter writer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
    }

    @FXML
    public void initialize() {
        sendButton.setOnAction(e -> sendCommand());
        inputField.setOnAction(e -> sendCommand());
    }

    private void sendCommand() {
        String cmd = inputField.getText();
        if (cmd.isEmpty())
            return;
        try {
            writer.write(cmd + "\n");
            writer.flush();
            String response = reader.readLine();
            consoleArea.appendText("> " + cmd + "\n" + response + "\n");
            inputField.clear();
            consoleArea.setScrollTop(Double.MAX_VALUE);
        } catch (IOException e) {
            consoleArea.appendText("Error: " + e.getMessage() + "\n");
        }
    }
}