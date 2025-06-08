package ftp.gusamyky.client.service.repository.impl;

import ftp.gusamyky.client.model.RemoteFile;
import ftp.gusamyky.client.service.network.ClientNetworkService;
import ftp.gusamyky.client.service.repository.i_repository.IFileRepositoryAsync;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
                return downloadFileWithProgress(filename, null, onProgress);
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
                return downloadFileWithProgress(filename, localPath, onProgress);
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
                return uploadFileWithProgress(file, onProgress);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> onError.accept(task.getException())));
        new Thread(task).start();
    }

    private boolean downloadFileWithProgress(String filename, Path localPath, DoubleConsumer onProgress) {
        try {
            ClientNetworkService ns = networkService;
            if (!ns.isConnected())
                return false;
            if (filename == null || filename.isEmpty())
                return false;
            ns.getLock().lock();
            try {
                ns.getWriter().write("DOWNLOAD " + filename + "\n");
                ns.getWriter().flush();
                String sizeLine = ns.getReader().readLine();
                if (sizeLine == null || sizeLine.startsWith("DOWNLOAD ERROR"))
                    return false;
                long fileSize = Long.parseLong(sizeLine);
                Path path = (localPath != null) ? localPath : ns.getDefaultDownloadPath(filename);
                java.nio.file.Files.createDirectories(path.getParent());
                try (OutputStream fileOut = java.nio.file.Files.newOutputStream(path)) {
                    InputStream in = ns.getSocket().getInputStream();
                    byte[] buffer = new byte[4096];
                    long received = 0;
                    while (received < fileSize) {
                        int toRead = (int) Math.min(buffer.length, fileSize - received);
                        int read = in.read(buffer, 0, toRead);
                        if (read == -1)
                            break;
                        fileOut.write(buffer, 0, read);
                        received += read;
                        if (onProgress != null && fileSize > 0) {
                            double progress = (double) received / fileSize;
                            Platform.runLater(() -> onProgress.accept(progress));
                        }
                    }
                }
                return true;
            } finally {
                ns.getLock().unlock();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean uploadFileWithProgress(File file, DoubleConsumer onProgress) {
        try {
            ClientNetworkService ns = networkService;
            if (!ns.isConnected())
                return false;
            if (file == null)
                return false;
            ns.getLock().lock();
            try {
                String filename = file.getName();
                ns.getWriter().write("UPLOAD " + filename + "\n");
                ns.getWriter().flush();
                String ready = ns.getReader().readLine();
                if (!"READY".equals(ready))
                    return false;
                long fileSize = file.length();
                ns.getWriter().write(fileSize + "\n");
                ns.getWriter().flush();
                try (InputStream fileIn = new FileInputStream(file)) {
                    OutputStream out = ns.getSocket().getOutputStream();
                    byte[] buffer = new byte[4096];
                    long sent = 0;
                    int read;
                    while ((read = fileIn.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        sent += read;
                        if (onProgress != null && fileSize > 0) {
                            double progress = (double) sent / fileSize;
                            Platform.runLater(() -> onProgress.accept(progress));
                        }
                    }
                    out.flush();
                }
                String response = ns.getReader().readLine();
                return response != null && response.startsWith("UPLOAD OK");
            } finally {
                ns.getLock().unlock();
            }
        } catch (Exception e) {
            return false;
        }
    }
}