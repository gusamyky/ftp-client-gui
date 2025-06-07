package ftp.gusamyky.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class RegisterTabController {
    @FXML
    private TextField regUserField;
    @FXML
    private PasswordField regPassField;
    @FXML
    private Button regButton;
    @FXML
    private Label regStatusLabel;

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
        regButton.setOnAction(e -> handleRegister());
    }

    private void handleRegister() {
        String username = regUserField.getText();
        String password = regPassField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            regStatusLabel.setText("Podaj login i hasło.");
            regStatusLabel.setTextFill(Color.RED);
            return;
        }
        try {
            writer.write("REGISTER " + username + " " + password + "\n");
            writer.flush();
            String response = reader.readLine();
            regStatusLabel.setText(response);
            if (response.startsWith("REGISTER OK")) {
                regStatusLabel.setTextFill(Color.GREEN);
            } else {
                regStatusLabel.setTextFill(Color.RED);
            }
        } catch (IOException e) {
            regStatusLabel.setText("Błąd połączenia: " + e.getMessage());
            regStatusLabel.setTextFill(Color.RED);
        }
    }
}