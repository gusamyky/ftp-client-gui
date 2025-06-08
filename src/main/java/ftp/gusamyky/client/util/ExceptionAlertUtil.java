package ftp.gusamyky.client.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Narzędzie do obsługi wyjątków: wyświetlanie alertów i logowanie.
 */
public class ExceptionAlertUtil {
    public static void showError(String message, Throwable ex) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText("Wystąpił błąd");
        alert.setContentText(message + (ex != null ? ("\n" + ex.getMessage()) : ""));
        alert.showAndWait();
        if (ex != null) {
            ex.printStackTrace();
        }
    }

    public static void showError(String message) {
        showError(message, null);
    }

    /**
     * Pokazuje alert o braku połączenia (bez reconnect).
     */
    public static void showConnectionError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Brak połączenia");
        alert.setHeaderText("Brak połączenia z serwerem");
        alert.setContentText(message);
        alert.showAndWait();
    }
}