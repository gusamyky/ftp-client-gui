package ftp.gusamyky.client.service.factory;

import ftp.gusamyky.client.service.network.ClientNetworkService;
import ftp.gusamyky.client.service.repository.i_repository.*;
import ftp.gusamyky.client.service.repository.impl.*;

public class RepositoryFactory {
    private static ILoginRepositoryAsync loginRepository;
    private static IRegisterRepositoryAsync registerRepository;
    private static IFileRepositoryAsync fileRepository;
    private static IHistoryRepositoryAsync historyRepository;
    private static IConsoleRepositoryAsync consoleRepository;

    public static ILoginRepositoryAsync getLoginRepository() {
        if (loginRepository == null) {
            loginRepository = new LoginRepositoryAsync(ClientNetworkService.getInstance());
        }
        return loginRepository;
    }

    public static IRegisterRepositoryAsync getRegisterRepository() {
        if (registerRepository == null) {
            registerRepository = new RegisterRepositoryAsync(ClientNetworkService.getInstance());
        }
        return registerRepository;
    }

    public static IFileRepositoryAsync getFileRepository() {
        if (fileRepository == null) {
            fileRepository = new FileRepositoryAsync(ClientNetworkService.getInstance());
        }
        return fileRepository;
    }

    public static IHistoryRepositoryAsync getHistoryRepository() {
        if (historyRepository == null) {
            historyRepository = new HistoryRepositoryAsync(ClientNetworkService.getInstance());
        }
        return historyRepository;
    }

    public static IConsoleRepositoryAsync getConsoleRepository() {
        if (consoleRepository == null) {
            consoleRepository = new ConsoleRepositoryAsync(ClientNetworkService.getInstance());
        }
        return consoleRepository;
    }
}