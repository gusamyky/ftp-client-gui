package ftp.gusamyky.client.controller;

import ftp.gusamyky.client.model.AppState;
import ftp.gusamyky.client.model.User;
import ftp.gusamyky.client.service.factory.RepositoryFactory;
import ftp.gusamyky.client.service.repository.i_repository.ILoginRepositoryAsync;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * Kontroler zakładki logowania użytkownika.
 */
public class LoginTabController {
    private final ILoginRepositoryAsync loginRepositoryAsync = RepositoryFactory.getLoginRepository();
    @FXML
    private TextField loginUserField;
    @FXML
    private PasswordField loginPassField;
    @FXML
    private Button loginButton;
    @FXML
    private Label loginStatusLabel;
    private Runnable onLoginSuccess;

    /**
     * Ustawia callback po udanym logowaniu.
     *
     * @param onLoginSuccess callback
     */
    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    /**
     * Inicjalizuje kontroler (ustawia obsługę przycisku logowania).
     */
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
        loginRepositoryAsync.loginAsync(
                username, password,
                success -> {
                    if (success) {
                        loginStatusLabel.setText("LOGIN OK");
                        loginStatusLabel.setTextFill(Color.GREEN);
                        AppState.getInstance().setLoggedUser(new User(username));
                        if (onLoginSuccess != null)
                            onLoginSuccess.run();
                    } else {
                        loginStatusLabel.setText("LOGIN ERROR");
                        loginStatusLabel.setTextFill(Color.RED);
                    }
                },
                ex -> {
                    loginStatusLabel.setText("Błąd logowania: " + ex.getMessage());
                    loginStatusLabel.setTextFill(Color.RED);
                });
    }

    /**
     * Zwraca login wpisany przez użytkownika.
     *
     * @return login
     */
    public String getLoginUsername() {
        return loginUserField.getText();
    }
}