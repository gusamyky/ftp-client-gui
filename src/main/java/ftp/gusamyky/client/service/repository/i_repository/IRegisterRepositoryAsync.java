package ftp.gusamyky.client.service.repository.i_repository;

import java.util.function.Consumer;

public interface IRegisterRepositoryAsync {
    void registerAsync(String username, String password, Consumer<Boolean> onSuccess, Consumer<Throwable> onError);
}