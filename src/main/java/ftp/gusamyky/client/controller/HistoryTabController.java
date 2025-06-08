package ftp.gusamyky.client.controller;

import ftp.gusamyky.client.model.AppState;
import ftp.gusamyky.client.model.HistoryItem;
import ftp.gusamyky.client.service.factory.RepositoryFactory;
import ftp.gusamyky.client.service.repository.i_repository.IHistoryRepositoryAsync;
import ftp.gusamyky.client.util.HistoryExportUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Kontroler zakładki historii operacji.
 */
public class HistoryTabController {
    private final ObservableList<HistoryItem> historyItems = FXCollections.observableArrayList();
    private final Label notLoggedInPlaceholder = new Label("Musisz być zalogowany, aby zobaczyć historię.");
    private final Label emptyPlaceholder = new Label("Brak historii operacji.");
    private final IHistoryRepositoryAsync historyRepositoryAsync = RepositoryFactory.getHistoryRepository();
    @FXML
    private Button refreshHistoryButton;
    @FXML
    private Button exportHistoryButton;
    @FXML
    private Button importHistoryButton;
    @FXML
    private ListView<HistoryItem> historyListView;

    /**
     * Inicjalizuje kontroler (ustawia obsługę przycisków i placeholdery).
     */
    @FXML
    public void initialize() {
        refreshHistoryButton.setOnAction(e -> fetchHistory());
        exportHistoryButton.setOnAction(e -> exportHistory());
        importHistoryButton.setOnAction(e -> importHistory());
        historyListView.setCellFactory(listView -> new HistoryItemCell());
        historyListView.setItems(historyItems);
        emptyPlaceholder.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
        notLoggedInPlaceholder.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
        historyListView.setPlaceholder(emptyPlaceholder);
    }

    private void exportHistory() {
        if (!AppState.requireLoggedIn("history", null))
            return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Eksportuj historię do pliku");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki binarne", "*.bin"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                HistoryExportUtil.exportToFile(AppState.getInstance().getHistory(), file.getAbsolutePath());
            } catch (Exception e) {
                ftp.gusamyky.client.util.ExceptionAlertUtil.showError("Błąd eksportu historii", e);
            }
        }
    }

    private void importHistory() {
        if (!AppState.requireLoggedIn("history", null))
            return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importuj historię z pliku");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki binarne", "*.bin"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                ObservableList<HistoryItem> imported = HistoryExportUtil.importFromFile(file.getAbsolutePath());
                AppState.getInstance().getHistory().setAll(imported);
                historyItems.setAll(imported);
            } catch (Exception e) {
                ftp.gusamyky.client.util.ExceptionAlertUtil.showError("Błąd importu historii", e);
            }
        }
    }

    private void fetchHistory() {
        if (!AppState.requireLoggedIn("history", null)) {
            historyItems.clear();
            historyListView.setPlaceholder(notLoggedInPlaceholder);
            return;
        }
        historyRepositoryAsync.fetchHistoryAsync(
                AppState.getInstance().getLoggedUser(),
                history -> {
                    AppState.getInstance().getHistory().setAll(history);
                    historyItems.setAll(history);
                    historyListView.setPlaceholder(emptyPlaceholder);
                },
                ex -> {
                    historyItems.clear();
                    historyListView.setPlaceholder(new Label("Błąd pobierania historii: " + ex.getMessage()));
                });
    }

    private static class HistoryItemCell extends ListCell<HistoryItem> {
        @Override
        protected void updateItem(HistoryItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getTimestamp() + ": " + item.getOperation());
            }
        }
    }
}