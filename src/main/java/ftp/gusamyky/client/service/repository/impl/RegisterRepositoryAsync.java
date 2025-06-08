package ftp.gusamyky.client.service.repository.impl;

import ftp.gusamyky.client.service.network.ClientNetworkService;
import ftp.gusamyky.client.service.repository.i_repository.IRegisterRepositoryAsync;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.function.Consumer;

public class RegisterRepositoryAsync implements IRegisterRepositoryAsync {
    private final ClientNetworkService networkService;

    public RegisterRepositoryAsync(ClientNetworkService networkService) {
        this.networkService = networkService;
    }

    @Override
    public void registerAsync(String username, String password, Consumer<Boolean> onSuccess,
                              Consumer<Throwable> onError) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return networkService.register(username, password);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> onError.accept(task.getException())));
        new Thread(task).start();
    }
}