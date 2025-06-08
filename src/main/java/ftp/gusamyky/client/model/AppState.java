package ftp.gusamyky.client.model;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;

/**
 * Singleton przechowujący globalny stan aplikacji.
 */
public class AppState {
    private static AppState instance;
    private final ObservableList<RemoteFile> remoteFiles = FXCollections.observableArrayList();
    private final ObservableList<HistoryItem> history = FXCollections.observableArrayList();
    private User loggedUser;

    private AppState() {
    }

    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    /**
     * Sprawdza, czy użytkownik jest zalogowany. Jeśli nie, wyświetla komunikat w
     * odpowiednim miejscu.
     *
     * @param context    'console', 'history', 'file' lub null
     * @param outputArea opcjonalny TextArea do wyświetlenia komunikatu (może być
     *                   null)
     * @return true jeśli zalogowany, false jeśli nie
     */
    public static boolean requireLoggedIn(String context, TextArea outputArea) {
        if (getInstance().getLoggedUser() != null)
            return true;
        String msg = switch (context == null ? "" : context) {
            case "console" -> "[ERROR] Musisz być zalogowany, aby wysyłać komendy do serwera.\n";
            case "history" -> "Musisz być zalogowany, aby korzystać z historii.\n";
            case "file" -> "Musisz być zalogowany, aby pobierać lub wysyłać pliki.\n";
            default -> "Musisz być zalogowany, aby wykonać tę operację.\n";
        };
        if (outputArea != null) {
            Platform.runLater(() -> outputArea.appendText(msg));
        }
        return false;
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    public ObservableList<RemoteFile> getRemoteFiles() {
        return remoteFiles;
    }

    public ObservableList<HistoryItem> getHistory() {
        return history;
    }
}