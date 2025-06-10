package ftp.gusamyky.client.util;

import ftp.gusamyky.client.service.error.NetworkErrorHandler;
import java.io.File;

public class FileVerificationUtil {
    private static final NetworkErrorHandler errorHandler = NetworkErrorHandler.getInstance();

    public static boolean verifyFile(File file, String operation) {
        if (file == null) {
            errorHandler.handleFileError(operation, "Plik nie może być null");
            return false;
        }

        if (!file.exists()) {
            errorHandler.handleFileError(operation, "Plik nie istnieje");
            return false;
        }

        if (!file.isFile()) {
            errorHandler.handleFileError(operation, "Ścieżka nie wskazuje na plik");
            return false;
        }

        if (!file.canRead()) {
            errorHandler.handleFileError(operation, "Brak uprawnień do odczytu pliku");
            return false;
        }

        if (file.length() == 0) {
            errorHandler.handleFileError(operation, "Plik jest pusty");
            return false;
        }

        return true;
    }
}