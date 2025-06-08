package ftp.gusamyky.client.service.repository.impl;

import ftp.gusamyky.client.model.HistoryItem;
import ftp.gusamyky.client.model.User;
import ftp.gusamyky.client.service.network.ClientNetworkService;
import ftp.gusamyky.client.service.repository.i_repository.IHistoryRepositoryAsync;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.function.Consumer;

public class HistoryRepositoryAsync implements IHistoryRepositoryAsync {
    private final ClientNetworkService networkService;

    public HistoryRepositoryAsync(ClientNetworkService networkService) {
        this.networkService = networkService;
    }

    @Override
    public void fetchHistoryAsync(User user, Consumer<ObservableList<HistoryItem>> onSuccess,
                                  Consumer<Throwable> onError) {
        Task<ObservableList<HistoryItem>> task = new Task<>() {
            @Override
            protected ObservableList<HistoryItem> call() {
                return networkService.fetchHistory(user);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> onError.accept(task.getException())));
        new Thread(task).start();
    }
}