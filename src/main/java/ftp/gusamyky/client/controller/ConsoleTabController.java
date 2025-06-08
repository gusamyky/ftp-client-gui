package ftp.gusamyky.client.controller;

import ftp.gusamyky.client.model.AppState;
import ftp.gusamyky.client.service.factory.RepositoryFactory;
import ftp.gusamyky.client.service.repository.i_repository.IConsoleRepositoryAsync;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Kontroler zakładki konsoli poleceń.
 */
public class ConsoleTabController {
    private final IConsoleRepositoryAsync consoleRepositoryAsync = RepositoryFactory.getConsoleRepository();
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
        consoleRepositoryAsync.sendCommandAsync(
                cmd,
                response -> {
                    consoleArea.appendText("> " + cmd + "\n" + response + "\n");
                    inputField.clear();
                    consoleArea.setScrollTop(Double.MAX_VALUE);
                },
                ex -> {
                    consoleArea.appendText("> " + cmd + "\nBłąd: " + ex.getMessage() + "\n");
                    inputField.clear();
                    consoleArea.setScrollTop(Double.MAX_VALUE);
                });
    }
}