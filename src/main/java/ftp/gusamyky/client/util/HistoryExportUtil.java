package ftp.gusamyky.client.util;

import ftp.gusamyky.client.model.HistoryItem;
import javafx.collections.ObservableList;
import java.io.*;

/**
 * Narzędzie do eksportu historii operacji do pliku binarnego (Serializable).
 */
public class HistoryExportUtil {
    public static void exportToFile(ObservableList<HistoryItem> history, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(new java.util.ArrayList<>(history));
        }
    }

    @SuppressWarnings("unchecked")
    public static ObservableList<HistoryItem> importFromFile(String filePath)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            java.util.List<HistoryItem> list = (java.util.List<HistoryItem>) ois.readObject();
            return javafx.collections.FXCollections.observableArrayList(list);
        }
    }
}