package ftp.gusamyky.client.service.repository.impl;

import ftp.gusamyky.client.service.network.ClientNetworkService;
import ftp.gusamyky.client.service.repository.i_repository.ILoginRepositoryAsync;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.function.Consumer;

public class LoginRepositoryAsync implements ILoginRepositoryAsync {
    private final ClientNetworkService networkService;

    public LoginRepositoryAsync(ClientNetworkService networkService) {
        this.networkService = networkService;
    }

    @Override
    public void loginAsync(String username, String password, Consumer<Boolean> onSuccess, Consumer<Throwable> onError) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return networkService.login(username, password);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> onError.accept(task.getException())));
        new Thread(task).start();
    }
}