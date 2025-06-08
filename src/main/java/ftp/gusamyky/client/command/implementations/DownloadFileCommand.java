package ftp.gusamyky.client.command.implementations;

import ftp.gusamyky.client.command.interfaces.ClientCommand;
import ftp.gusamyky.client.service.ClientNetworkService;
import java.nio.file.Path;
import java.io.IOException;

/**
 * Komenda pobierania pliku z serwera.
 */
public class DownloadFileCommand implements ClientCommand {
    private final String remoteFileName;
    private final Path localPath; // może być null
    private final ClientNetworkService networkService;

    public DownloadFileCommand(String remoteFileName, ClientNetworkService networkService) {
        this(remoteFileName, null, networkService);
    }

    public DownloadFileCommand(String remoteFileName, Path localPath, ClientNetworkService networkService) {
        this.remoteFileName = remoteFileName;
        this.localPath = localPath;
        this.networkService = networkService;
    }

    @Override
    public void execute() throws IOException {
        boolean result = (localPath == null)
                ? networkService.downloadFile(remoteFileName)
                : networkService.downloadFile(remoteFileName, localPath);
        if (!result)
            throw new IOException("Download failed for " + remoteFileName);
    }

    @Override
    public String getDescription() {
        return "Pobierz plik: " + remoteFileName;
    }
}