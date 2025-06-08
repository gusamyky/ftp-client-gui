package ftp.gusamyky.client.command.implementations;

import ftp.gusamyky.client.command.interfaces.ClientCommand;
import ftp.gusamyky.client.service.ClientNetworkService;
import java.io.File;
import java.io.IOException;

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
    public void execute() throws IOException {
        boolean result = networkService.uploadFile(file);
        if (!result)
            throw new IOException("Upload failed for " + file.getName());
    }

    @Override
    public String getDescription() {
        return "Wyślij plik: " + file.getName();
    }
}