package ftp.gusamyky.client.service.repository.impl;

import ftp.gusamyky.client.model.RemoteFile;
import ftp.gusamyky.client.service.network.ClientNetworkService;
import ftp.gusamyky.client.service.repository.i_repository.IFileRepositoryAsync;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class FileRepositoryAsync implements IFileRepositoryAsync {
    private final ClientNetworkService networkService;

    public FileRepositoryAsync(ClientNetworkService networkService) {
        this.networkService = networkService;
    }

    @Override
    public void fetchRemoteFilesAsync(Consumer<ObservableList<RemoteFile>> onSuccess, Consumer<Throwable> onError) {
        Task<ObservableList<RemoteFile>> task = new Task<>() {
            @Override
            protected ObservableList<RemoteFile> call() {
                return networkService.fetchRemoteFiles();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> onError.accept(task.getException())));
        new Thread(task).start();
    }

    @Override
    public void downloadFileAsync(String filename, Consumer<Boolean> onSuccess, Consumer<Throwable> onError,
            DoubleConsumer onProgress) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return networkService.downloadFileWithProgress(filename, null, onProgress);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> onError.accept(task.getException())));
        new Thread(task).start();
    }

    @Override
    public void downloadFileAsync(String filename, Path localPath, Consumer<Boolean> onSuccess,
            Consumer<Throwable> onError, DoubleConsumer onProgress) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return networkService.downloadFileWithProgress(filename, localPath, onProgress);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> onError.accept(task.getException())));
        new Thread(task).start();
    }

    @Override
    public void uploadFileAsync(File file, Consumer<Boolean> onSuccess, Consumer<Throwable> onError,
            DoubleConsumer onProgress) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return networkService.uploadFileWithProgress(file, onProgress);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> onError.accept(task.getException())));
        new Thread(task).start();
    }
}