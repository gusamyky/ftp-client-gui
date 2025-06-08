package ftp.gusamyky.client;

import ftp.gusamyky.client.command.implementations.DownloadFileCommand;
import ftp.gusamyky.client.command.implementations.UploadFileCommand;
import ftp.gusamyky.client.model.AppState;
import ftp.gusamyky.client.model.RemoteFile;
import ftp.gusamyky.client.service.ClientNetworkService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;

/**
 * Kontroler zakładki plików (lista plików, upload, download).
 */
public class FileTabController {
    private final ObservableList<RemoteFile> filteredFiles = FXCollections.observableArrayList();
    @FXML
    private TextField filterField;
    @FXML
    private Button refreshFilesButton;
    @FXML
    private Button uploadButton;
    @FXML
    private ListView<RemoteFile> filesListView;
    @FXML
    private TextArea outputArea;

    /**
     * Inicjalizuje kontroler (ustawia obsługę przycisków i filtrów).
     */
    @FXML
    public void initialize() {
        filterField.textProperty().addListener((obs, oldVal, newVal) -> updateFileList());
        refreshFilesButton.setOnAction(e -> fetchFileList());
        uploadButton.setOnAction(e -> sendUpload());
        filesListView.setCellFactory(listView -> new RemoteFileCell());
    }

    private void fetchFileList() {
        if (!AppState.requireLoggedIn("file", outputArea))
            return;
        ObservableList<RemoteFile> files = ClientNetworkService.getInstance().fetchRemoteFiles();
        AppState.getInstance().getRemoteFiles().setAll(files);
        updateFileList();
    }

    private void updateFileList() {
        String filter = filterField.getText().toLowerCase();
        filteredFiles.setAll(AppState.getInstance().getRemoteFiles()
                .filtered(f -> filter.isEmpty() || f.getName().toLowerCase().contains(filter)));
        filesListView.setItems(filteredFiles);
    }

    private void saveFileDefault(RemoteFile file) {
        if (!AppState.requireLoggedIn("file", outputArea))
            return;
        try {
            DownloadFileCommand cmd = new DownloadFileCommand(file.getName(), ClientNetworkService.getInstance());
            cmd.execute();
            outputArea.appendText("Plik pobrany do domyślnego katalogu: "
                    + ClientNetworkService.getInstance().getDefaultDownloadPath(file.getName()) + "\n");
        } catch (Exception e) {
            outputArea.appendText("Błąd pobierania pliku: " + e.getMessage() + "\n");
        }
    }

    private void saveFileAs(RemoteFile file) {
        if (!AppState.requireLoggedIn("file", outputArea))
            return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz plik jako...");
        fileChooser.setInitialFileName(file.getName());
        File dest = fileChooser.showSaveDialog(null);
        if (dest != null) {
            Path path = dest.toPath();
            try {
                DownloadFileCommand cmd = new DownloadFileCommand(file.getName(), path,
                        ClientNetworkService.getInstance());
                cmd.execute();
                outputArea.appendText("Plik pobrany do: " + path + "\n");
            } catch (Exception e) {
                outputArea.appendText("Błąd pobierania pliku: " + e.getMessage() + "\n");
            }
        }
    }

    private void sendUpload() {
        if (!AppState.requireLoggedIn("file", outputArea))
            return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik do wysłania na serwer");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                UploadFileCommand cmd = new UploadFileCommand(file, ClientNetworkService.getInstance());
                cmd.execute();
                outputArea.appendText("Plik wysłany: " + file.getName() + "\n");
                fetchFileList();
            } catch (Exception e) {
                outputArea.appendText("Błąd wysyłania pliku: " + e.getMessage() + "\n");
            }
        }
    }

    private class RemoteFileCell extends ListCell<RemoteFile> {
        private final Label nameLabel = new Label();
        private final Button saveBtn = new Button("SAVE");
        private final Button saveAsBtn = new Button("SAVE AS");
        private final HBox hbox = new HBox(10, nameLabel, saveBtn, saveAsBtn);

        public RemoteFileCell() {
            saveBtn.setOnAction(e -> {
                RemoteFile file = getItem();
                if (file != null)
                    saveFileDefault(file);
            });
            saveAsBtn.setOnAction(e -> {
                RemoteFile file = getItem();
                if (file != null)
                    saveFileAs(file);
            });
        }

        @Override
        protected void updateItem(RemoteFile file, boolean empty) {
            super.updateItem(file, empty);
            if (empty || file == null) {
                setText(null);
                setGraphic(null);
            } else {
                nameLabel.setText(file.getName());
                setGraphic(hbox);
            }
        }
    }
}