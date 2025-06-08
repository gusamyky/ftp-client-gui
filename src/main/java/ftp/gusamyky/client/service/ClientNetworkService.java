package ftp.gusamyky.client.service;

import ftp.gusamyky.client.model.HistoryItem;
import ftp.gusamyky.client.model.RemoteFile;
import ftp.gusamyky.client.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Serwis sieciowy obsługujący połączenie z serwerem FTP oraz operacje sieciowe.
 * Singleton.
 */
public class ClientNetworkService {
    private static ClientNetworkService instance;
    private final ReentrantLock lock = new ReentrantLock();
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private ClientNetworkService() {
    }

    public static synchronized ClientNetworkService getInstance() {
        if (instance == null) {
            instance = new ClientNetworkService();
        }
        return instance;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void connect(String host, int port) throws IOException {
        lock.lock();
        try {
            System.out.println("[ClientNetworkService] Próba połączenia z serwerem: " + host + ":" + port);
            disconnect();
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String welcome = reader.readLine(); // Odczytaj powitanie serwera
            System.out.println(
                    "[ClientNetworkService] Połączono z serwerem: " + host + ":" + port + ". Powitanie: " + welcome);
        } catch (IOException e) {
            System.out.println(
                    "[ClientNetworkService] Błąd połączenia z serwerem: " + host + ":" + port + " - " + e.getMessage());
            throw e;
        } finally {
            lock.unlock();
        }
    }

    public void disconnect() {
        lock.lock();
        try {
            if (reader != null || writer != null || socket != null) {
                System.out.println("[ClientNetworkService] Rozłączanie z serwerem...");
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
                reader = null;
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
                writer = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                socket = null;
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean checkConnectionAndNotify(String operation) {
        if (!isConnected()) {
            ftp.gusamyky.client.util.ExceptionAlertUtil.showConnectionError(
                    "Brak połączenia z serwerem (strumienie nie są zainicjalizowane).\nOperacja: " + operation);
            return false;
        }
        return true;
    }

    public boolean login(String username, String password) {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("Logowanie"))
                return false;
            try {
                writer.write("LOGIN " + username + " " + password + "\n");
                writer.flush();
                String response = reader.readLine();
                return response != null && response.startsWith("LOGIN OK");
            } catch (IOException e) {
                disconnect();
                ftp.gusamyky.client.util.ExceptionAlertUtil.showConnectionError("Błąd logowania: " + e.getMessage());
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean register(String username, String password) {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("Rejestracja"))
                return false;
            try {
                writer.write("REGISTER " + username + " " + password + "\n");
                writer.flush();
                String response = reader.readLine();
                return response != null && response.startsWith("REGISTER OK");
            } catch (IOException e) {
                disconnect();
                ftp.gusamyky.client.util.ExceptionAlertUtil.showConnectionError("Błąd rejestracji: " + e.getMessage());
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public ObservableList<RemoteFile> fetchRemoteFiles() {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("Pobieranie listy plików"))
                return FXCollections.observableArrayList();
            ObservableList<RemoteFile> files = FXCollections.observableArrayList();
            try {
                writer.write("LIST\n");
                writer.flush();
                String response = reader.readLine();
                if (response != null && response.startsWith("FILES:")) {
                    String[] fileArr = response.substring(6).trim().split(" ");
                    for (String f : fileArr) {
                        if (!f.isBlank() && !f.equals("(brak"))
                            files.add(new RemoteFile(f, "?"));
                    }
                }
            } catch (IOException e) {
                disconnect();
                ftp.gusamyky.client.util.ExceptionAlertUtil
                        .showConnectionError("Błąd pobierania listy plików: " + e.getMessage());
            }
            return files;
        } finally {
            lock.unlock();
        }
    }

    public boolean downloadFile(String filename) {
        return downloadFile(filename, getDefaultDownloadPath(filename));
    }

    public boolean downloadFile(String filename, java.nio.file.Path localPath) {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("Pobieranie pliku"))
                return false;
            if (filename == null || filename.isEmpty())
                return false;
            try {
                writer.write("DOWNLOAD " + filename + "\n");
                writer.flush();
                String sizeLine = reader.readLine();
                if (sizeLine == null || sizeLine.startsWith("DOWNLOAD ERROR")) {
                    ftp.gusamyky.client.util.ExceptionAlertUtil.showError("Błąd pobierania pliku: " + sizeLine);
                    return false;
                }
                long fileSize = Long.parseLong(sizeLine);
                java.nio.file.Files.createDirectories(localPath.getParent());
                try (OutputStream fileOut = java.nio.file.Files.newOutputStream(localPath)) {
                    InputStream in = socket.getInputStream();
                    byte[] buffer = new byte[4096];
                    long received = 0;
                    while (received < fileSize) {
                        int toRead = (int) Math.min(buffer.length, fileSize - received);
                        int read = in.read(buffer, 0, toRead);
                        if (read == -1)
                            break;
                        fileOut.write(buffer, 0, read);
                        received += read;
                    }
                }
                return true;
            } catch (Exception e) {
                disconnect();
                ftp.gusamyky.client.util.ExceptionAlertUtil
                        .showConnectionError("Błąd pobierania pliku: " + e.getMessage());
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public java.nio.file.Path getDefaultDownloadPath(String filename) {
        String userHome = System.getProperty("user.home");
        java.nio.file.Path downloads = java.nio.file.Paths.get(userHome, "Downloads");
        if (!java.nio.file.Files.exists(downloads)) {
            downloads = java.nio.file.Paths.get(userHome, "Pobrane");
        }
        if (!java.nio.file.Files.exists(downloads)) {
            downloads = java.nio.file.Paths.get(userHome, "client_files");
        }
        return downloads.resolve(filename);
    }

    public boolean uploadFile(java.io.File file) {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("Wysyłanie pliku"))
                return false;
            if (file == null)
                return false;
            String filename = file.getName();
            try {
                writer.write("UPLOAD " + filename + "\n");
                writer.flush();
                String ready = reader.readLine();
                if (!"READY".equals(ready)) {
                    ftp.gusamyky.client.util.ExceptionAlertUtil.showError("UPLOAD ERROR: Server not ready");
                    return false;
                }
                long fileSize = file.length();
                writer.write(fileSize + "\n");
                writer.flush();
                try (InputStream fileIn = new FileInputStream(file)) {
                    OutputStream out = socket.getOutputStream();
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = fileIn.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    out.flush();
                }
                String response = reader.readLine();
                return response != null && response.startsWith("UPLOAD OK");
            } catch (Exception e) {
                disconnect();
                ftp.gusamyky.client.util.ExceptionAlertUtil
                        .showConnectionError("Błąd wysyłania pliku: " + e.getMessage());
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public ObservableList<HistoryItem> fetchHistory(User user) {
        lock.lock();
        try {
            ObservableList<HistoryItem> history = FXCollections.observableArrayList();
            if (user == null) {
                history.add(new HistoryItem("Zaloguj się, aby zobaczyć historię operacji.", ""));
                return history;
            }
            if (!checkConnectionAndNotify("Pobieranie historii")) {
                history.add(new HistoryItem("Błąd połączenia z serwerem.", ""));
                return history;
            }
            try {
                writer.write("HISTORY " + user.getUsername() + "\n");
                writer.flush();
                String line = reader.readLine();
                if (line == null) {
                    history.add(new HistoryItem("Błąd połączenia z serwerem.", ""));
                    return history;
                }
                if (line.startsWith("HISTORY:")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(line.substring(8)).append("\n");
                    while (reader.ready()) {
                        String l = reader.readLine();
                        if (l == null || l.isEmpty())
                            break;
                        sb.append(l).append("\n");
                    }
                    // Każda linia jako osobny wpis historii
                    String[] lines = sb.toString().split("\\n");
                    for (String histLine : lines) {
                        if (!histLine.isEmpty())
                            history.add(new HistoryItem(histLine, ""));
                    }
                } else {
                    history.add(new HistoryItem(line, ""));
                }
            } catch (IOException e) {
                history.add(new HistoryItem("Błąd: " + e.getMessage(), ""));
            }
            return history;
        } finally {
            lock.unlock();
        }
    }

    public String sendCommand(String command) {
        lock.lock();
        try {
            if (!checkConnectionAndNotify("Wysyłanie komendy"))
                return "Brak połączenia z serwerem.";
            try {
                writer.write(command + "\n");
                writer.flush();
                return reader.readLine();
            } catch (IOException e) {
                disconnect();
                ftp.gusamyky.client.util.ExceptionAlertUtil
                        .showConnectionError("Błąd wysyłania komendy: " + e.getMessage());
                return "Błąd: " + e.getMessage();
            }
        } finally {
            lock.unlock();
        }
    }
}