package ftp.gusamyky.client.service.repository.i_repository;

import ftp.gusamyky.client.model.RemoteFile;
import javafx.collections.ObservableList;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public interface IFileRepositoryAsync {
    void fetchRemoteFilesAsync(Consumer<ObservableList<RemoteFile>> onSuccess, Consumer<Throwable> onError);

    void downloadFileAsync(String filename, Consumer<Boolean> onSuccess, Consumer<Throwable> onError,
            DoubleConsumer onProgress);

    void downloadFileAsync(String filename, Path localPath, Consumer<Boolean> onSuccess, Consumer<Throwable> onError,
            DoubleConsumer onProgress);

    void uploadFileAsync(File file, Consumer<Boolean> onSuccess, Consumer<Throwable> onError,
            DoubleConsumer onProgress);
}