package ftp.gusamyky.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import ftp.gusamyky.client.service.ClientNetworkService;
import ftp.gusamyky.client.model.AppState;
import ftp.gusamyky.client.model.User;

/**
 * Kontroler zakładki logowania użytkownika.
 */
public class LoginTabController {
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
        boolean success = ClientNetworkService.getInstance().login(username, password);
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