package ftp.gusamyky.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class LoginTabController {
    @FXML
    private TextField loginUserField;
    @FXML
    private PasswordField loginPassField;
    @FXML
    private Button loginButton;
    @FXML
    private Label loginStatusLabel;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Runnable onLoginSuccess;

    public void setSocket(Socket socket, BufferedReader reader, BufferedWriter writer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
    }

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    @FXML
    public void initialize() {
        loginButton.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String username = loginUserField.getText();
        String password = loginPassField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            loginStatusLabel.setText("Podaj login i hasło.");
            loginStatusLabel.setTextFill(Color.RED);
            return;
        }
        try {
            writer.write("LOGIN " + username + " " + password + "\n");
            writer.flush();
            String response = reader.readLine();
            loginStatusLabel.setText(response);
            if (response.startsWith("LOGIN OK")) {
                loginStatusLabel.setTextFill(Color.GREEN);
                if (onLoginSuccess != null)
                    onLoginSuccess.run();
            } else {
                loginStatusLabel.setTextFill(Color.RED);
            }
        } catch (IOException e) {
            loginStatusLabel.setText("Błąd połączenia: " + e.getMessage());
            loginStatusLabel.setTextFill(Color.RED);
        }
    }

    public String getLoginUsername() {
        return loginUserField.getText();
    }
}