package ftp.gusamyky.client.service.command;

public interface NetworkCommand<T> {
    T execute() throws Exception;

    String getOperationName();
}