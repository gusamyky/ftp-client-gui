package ftp.gusamyky.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import ftp.gusamyky.client.service.ClientNetworkService;
import ftp.gusamyky.client.model.AppState;
import ftp.gusamyky.client.model.User;

/**
 * Kontroler zakładki rejestracji użytkownika.
 */
public class RegisterTabController {
    @FXML
    private TextField regUserField;
    @FXML
    private PasswordField regPassField;
    @FXML
    private Button regButton;
    @FXML
    private Label regStatusLabel;

    /**
     * Inicjalizuje kontroler (ustawia obsługę przycisku rejestracji).
     */
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
        boolean success = ClientNetworkService.getInstance().register(username, password);
        if (success) {
            regStatusLabel.setText("REGISTER OK");
            regStatusLabel.setTextFill(Color.GREEN);
            AppState.getInstance().setLoggedUser(new User(username));
        } else {
            regStatusLabel.setText("REGISTER ERROR");
            regStatusLabel.setTextFill(Color.RED);
        }
    }
}