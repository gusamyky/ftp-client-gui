package ftp.gusamyky.client.command.interfaces;

/**
 * Interfejs bazowy dla komend klienta (wzorzec Command).
 * Pozwala na wykonywanie operacji (np. pobieranie, wysyłanie plików) w sposób
 * rozszerzalny.
 */
public interface ClientCommand {
    /**
     * Wykonuje operację klienta.
     */
    void execute() throws Exception;

    /**
     * Zwraca opis operacji.
     */
    String getDescription();
}