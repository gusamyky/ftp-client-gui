package ftp.gusamyky.client.command.implementations;

import ftp.gusamyky.client.command.interfaces.ClientCommand;
import ftp.gusamyky.client.service.ClientNetworkService;
import java.io.File;

/**
 * Komenda wysyłania pliku na serwer.
 */
public class UploadFileCommand implements ClientCommand {
    private final File file;
    private final ClientNetworkService networkService;

    public UploadFileCommand(File file, ClientNetworkService networkService) {
        this.file = file;
        this.networkService = networkService;
    }

    @Override
    public void execute() throws Exception {
        boolean result = networkService.uploadFile(file);
        if (!result)
            throw new Exception("Upload failed for " + file.getName());
    }

    @Override
    public String getDescription() {
        return "Wyślij plik: " + file.getName();
    }
}