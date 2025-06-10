package ftp.gusamyky.client.service.command;

import ftp.gusamyky.client.service.error.NetworkErrorHandler;
import ftp.gusamyky.client.service.network.ServerMessageHandler;

import java.io.BufferedWriter;

public class RegisterCommand implements NetworkCommand<Boolean> {
    private final String username;
    private final String password;
    private final BufferedWriter writer;
    private final ServerMessageHandler messageHandler;
    private final NetworkErrorHandler errorHandler;

    public RegisterCommand(String username, String password, BufferedWriter writer,
            ServerMessageHandler messageHandler) {
        this.username = username;
        this.password = password;
        this.writer = writer;
        this.messageHandler = messageHandler;
        this.errorHandler = NetworkErrorHandler.getInstance();
    }

    @Override
    public String getOperationName() {
        return "rejestracji";
    }

    @Override
    public Boolean execute() throws Exception {
        if (username == null || username.isEmpty()) {
            errorHandler.handleError(getOperationName(), "Nazwa użytkownika nie może być pusta");
            return false;
        }

        if (password == null || password.isEmpty()) {
            errorHandler.handleError(getOperationName(), "Hasło nie może być puste");
            return false;
        }

        writer.write("REGISTER " + username + " " + password + "\n");
        writer.flush();

        String response = messageHandler.readLine(getOperationName());
        if (messageHandler.isErrorResponse(response)) {
            errorHandler.handleError(getOperationName(), messageHandler.getErrorMessage(response));
            return false;
        }

        if (!response.startsWith("OK: Registration successful")) {
            errorHandler.handleError(getOperationName(), "Nieoczekiwana odpowiedź z serwera");
            return false;
        }

        return true;
    }
}