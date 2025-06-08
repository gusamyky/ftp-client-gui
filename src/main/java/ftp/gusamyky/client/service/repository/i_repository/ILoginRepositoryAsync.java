package ftp.gusamyky.client.service.repository.i_repository;

import java.util.function.Consumer;

public interface ILoginRepositoryAsync {
    void loginAsync(String username, String password, Consumer<Boolean> onSuccess, Consumer<Throwable> onError);
}