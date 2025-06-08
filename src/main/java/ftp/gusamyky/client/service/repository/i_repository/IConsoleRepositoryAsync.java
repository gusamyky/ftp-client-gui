package ftp.gusamyky.client.service.repository.i_repository;

import java.util.function.Consumer;

public interface IConsoleRepositoryAsync {
    void sendCommandAsync(String command, Consumer<String> onSuccess, Consumer<Throwable> onError);
}