package ftp.gusamyky.client.controller;

import ftp.gusamyky.client.model.AppState;
import ftp.gusamyky.client.model.User;
import ftp.gusamyky.client.service.factory.RepositoryFactory;
import ftp.gusamyky.client.service.repository.i_repository.IRegisterRepositoryAsync;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * Kontroler zakładki rejestracji użytkownika.
 */
public class RegisterTabController {
    private final IRegisterRepositoryAsync registerRepositoryAsync = RepositoryFactory.getRegisterRepository();
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
        registerRepositoryAsync.registerAsync(
                username, password,
                success -> {
                    if (success) {
                        regStatusLabel.setText("REGISTER OK");
                        regStatusLabel.setTextFill(Color.GREEN);
                        AppState.getInstance().setLoggedUser(new User(username));
                    } else {
                        regStatusLabel.setText("REGISTER ERROR");
                        regStatusLabel.setTextFill(Color.RED);
                    }
                },
                ex -> {
                    regStatusLabel.setText("Błąd rejestracji: " + ex.getMessage());
                    regStatusLabel.setTextFill(Color.RED);
                });
    }
}