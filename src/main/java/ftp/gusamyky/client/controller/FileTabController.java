package ftp.gusamyky.client.controller;

import ftp.gusamyky.client.model.AppState;
import ftp.gusamyky.client.model.RemoteFile;
import ftp.gusamyky.client.service.factory.RepositoryFactory;
import ftp.gusamyky.client.service.network.ClientNetworkService;
import ftp.gusamyky.client.service.repository.i_repository.IFileRepositoryAsync;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;

/**
 * Kontroler zakładki plików (lista plików, upload, download).
 */
public class FileTabController {
    private final ObservableList<RemoteFile> filteredFiles = FXCollections.observableArrayList();
    private final IFileRepositoryAsync fileRepositoryAsync = RepositoryFactory.getFileRepository();
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
    @FXML
    private ProgressBar progressBar;

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
        fileRepositoryAsync.fetchRemoteFilesAsync(
                files -> {
                    AppState.getInstance().getRemoteFiles().setAll(files);
                    updateFileList();
                },
                ex -> outputArea.appendText("Błąd pobierania plików: " + ex.getMessage() + "\n"));
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
        showProgressBar();
        fileRepositoryAsync.downloadFileAsync(
                file.getName(),
                success -> {
                    hideProgressBar();
                    if (success) {
                        outputArea.appendText("Plik pobrany do domyślnego katalogu: "
                                + ClientNetworkService.getInstance().getDefaultDownloadPath(file.getName()) + "\n");
                    } else {
                        outputArea.appendText("Błąd pobierania pliku: operacja nie powiodła się.\n");
                    }
                },
                ex -> {
                    hideProgressBar();
                    outputArea.appendText("Błąd pobierania pliku: " + ex.getMessage() + "\n");
                },
                progress -> progressBar.setProgress(progress));
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
            showProgressBar();
            fileRepositoryAsync.downloadFileAsync(
                    file.getName(), path,
                    success -> {
                        hideProgressBar();
                        if (success) {
                            outputArea.appendText("Plik pobrany do: " + path + "\n");
                        } else {
                            outputArea.appendText("Błąd pobierania pliku: operacja nie powiodła się.\n");
                        }
                    },
                    ex -> {
                        hideProgressBar();
                        outputArea.appendText("Błąd pobierania pliku: " + ex.getMessage() + "\n");
                    },
                    progress -> progressBar.setProgress(progress));
        }
    }

    private void sendUpload() {
        if (!AppState.requireLoggedIn("file", outputArea))
            return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik do wysłania na serwer");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            showProgressBar();
            fileRepositoryAsync.uploadFileAsync(
                    file,
                    success -> {
                        hideProgressBar();
                        if (success) {
                            outputArea.appendText("Plik wysłany: " + file.getName() + "\n");
                            fetchFileList();
                        } else {
                            outputArea.appendText("Błąd wysyłania pliku: operacja nie powiodła się.\n");
                        }
                    },
                    ex -> {
                        hideProgressBar();
                        outputArea.appendText("Błąd wysyłania pliku: " + ex.getMessage() + "\n");
                    },
                    progress -> progressBar.setProgress(progress));
        }
    }

    private void showProgressBar() {
        progressBar.setProgress(0.0);
        progressBar.setVisible(true);
    }

    private void hideProgressBar() {
        progressBar.setVisible(false);
    }

    private class RemoteFileCell extends ListCell<RemoteFile> {
        private final Label nameLabel = new Label();
        private final Button saveBtn = new Button("Pobierz");
        private final Button saveAsBtn = new Button("Pobierz jako...");
        private final Region spacer = new Region();
        private final HBox hbox = new HBox(10, nameLabel, spacer, saveBtn, saveAsBtn);

        public RemoteFileCell() {
            HBox.setHgrow(spacer, Priority.ALWAYS);
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