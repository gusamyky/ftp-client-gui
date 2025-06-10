package ftp.gusamyky.client.service.network;

import ftp.gusamyky.client.service.error.NetworkErrorHandler;
import java.io.BufferedReader;
import java.io.IOException;

public class ServerMessageHandler {
    private final BufferedReader reader;
    private final NetworkErrorHandler errorHandler;

    public ServerMessageHandler(BufferedReader reader) {
        this.reader = reader;
        this.errorHandler = NetworkErrorHandler.getInstance();
    }

    public String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            errorHandler.handleError("odczytu wiadomości", "Brak odpowiedzi z serwera");
            throw new IOException("No response from server");
        }
        return line;
    }

    public String readLine(String operation) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            errorHandler.handleError(operation, "Brak odpowiedzi z serwera");
            throw new IOException("No response from server");
        }
        return line;
    }

    public boolean isErrorResponse(String response) {
        return response != null && response.startsWith("ERROR:");
    }

    public String getErrorMessage(String response) {
        return response.substring(6).trim();
    }

    public boolean isOkResponse(String response) {
        return response != null && response.startsWith("OK:");
    }

    public boolean isReadyResponse(String response) {
        return "READY".equals(response);
    }
}