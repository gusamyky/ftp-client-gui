package ftp.gusamyky.client.service.error;

import javafx.application.Platform;
import ftp.gusamyky.client.util.ExceptionAlertUtil;

public class NetworkErrorHandler {
    private static NetworkErrorHandler instance;
    private boolean isErrorState = false;

    private NetworkErrorHandler() {
    }

    public static synchronized NetworkErrorHandler getInstance() {
        if (instance == null) {
            instance = new NetworkErrorHandler();
        }
        return instance;
    }

    public void handleError(String operation, String errorMessage) {
        if (isErrorState) {
            return;
        }

        isErrorState = true;
        Platform.runLater(() -> ExceptionAlertUtil.showConnectionError(
                String.format("Błąd podczas %s: %s", operation, errorMessage)));
    }

    public void handleFileError(String operation, String errorMessage) {
        Platform.runLater(() -> ExceptionAlertUtil.showError(
                String.format("Błąd podczas %s: %s", operation, errorMessage)));
    }

    public void resetErrorState() {
        isErrorState = false;
    }

    public boolean isInErrorState() {
        return isErrorState;
    }
}