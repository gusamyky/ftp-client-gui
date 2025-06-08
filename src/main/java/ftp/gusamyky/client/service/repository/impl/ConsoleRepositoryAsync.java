package ftp.gusamyky.client.service.repository.impl;

import ftp.gusamyky.client.service.network.ClientNetworkService;
import ftp.gusamyky.client.service.repository.i_repository.IConsoleRepositoryAsync;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.function.Consumer;

public class ConsoleRepositoryAsync implements IConsoleRepositoryAsync {
    private final ClientNetworkService networkService;

    public ConsoleRepositoryAsync(ClientNetworkService networkService) {
        this.networkService = networkService;
    }

    @Override
    public void sendCommandAsync(String command, Consumer<String> onSuccess, Consumer<Throwable> onError) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return networkService.sendCommand(command);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> onError.accept(task.getException())));
        new Thread(task).start();
    }
}