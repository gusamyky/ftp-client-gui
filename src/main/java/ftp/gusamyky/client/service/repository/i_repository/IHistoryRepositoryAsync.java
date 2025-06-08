package ftp.gusamyky.client.service.repository.i_repository;

import ftp.gusamyky.client.model.HistoryItem;
import ftp.gusamyky.client.model.User;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

public interface IHistoryRepositoryAsync {
    void fetchHistoryAsync(User user, Consumer<ObservableList<HistoryItem>> onSuccess, Consumer<Throwable> onError);
}