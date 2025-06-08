package ftp.gusamyky.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import ftp.gusamyky.client.service.ClientNetworkService;
import ftp.gusamyky.client.model.AppState;

/**
 * Kontroler zakładki konsoli poleceń.
 */
public class ConsoleTabController {
    @FXML
    private TextArea consoleArea;
    @FXML
    private TextField inputField;
    @FXML
    private Button sendButton;

    /**
     * Inicjalizuje kontroler (ustawia obsługę przycisków i pola tekstowego).
     */
    @FXML
    public void initialize() {
        sendButton.setOnAction(e -> sendCommand());
        inputField.setOnAction(e -> sendCommand());
    }

    private void sendCommand() {
        if (!AppState.requireLoggedIn("console", consoleArea)) {
            inputField.clear();
            return;
        }
        String cmd = inputField.getText();
        if (cmd.isEmpty())
            return;
        String response = ClientNetworkService.getInstance().sendCommand(cmd);
        consoleArea.appendText("> " + cmd + "\n" + response + "\n");
        inputField.clear();
        consoleArea.setScrollTop(Double.MAX_VALUE);
    }
}