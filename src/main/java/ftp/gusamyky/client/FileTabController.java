package ftp.gusamyky.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileTabController {
    @FXML
    private TextField filterField;
    @FXML
    private Button refreshFilesButton;
    @FXML
    private Button uploadButton;
    @FXML
    private VBox filesListVBox;
    @FXML
    private TextArea outputArea;

    private List<String> allFiles = new ArrayList<>();
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void setSocket(Socket socket, BufferedReader reader, BufferedWriter writer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        fetchFileList();
    }

    @FXML
    public void initialize() {
        filterField.textProperty().addListener((obs, oldVal, newVal) -> updateFileListVBox());
        refreshFilesButton.setOnAction(e -> fetchFileList());
        uploadButton.setOnAction(e -> sendUpload());
    }

    private void fetchFileList() {
        try {
            writer.write("LIST\n");
            writer.flush();
            String response = reader.readLine();
            allFiles.clear();
            if (response.startsWith("FILES:")) {
                String[] files = response.substring(6).trim().split(" ");
                for (String f : files) {
                    if (!f.isBlank() && !f.equals("(brak"))
                        allFiles.add(f);
                }
            } else {
                outputArea.appendText("Błąd pobierania listy plików: " + response + "\n");
            }
            updateFileListVBox();
        } catch (IOException e) {
            outputArea.appendText("Błąd pobierania listy plików: " + e.getMessage() + "\n");
        }
    }

    private void updateFileListVBox() {
        filesListVBox.getChildren().clear();
        String filter = filterField.getText().toLowerCase();
        for (String file : allFiles) {
            if (!filter.isEmpty() && !file.toLowerCase().contains(filter))
                continue;
            HBox row = new HBox(10);
            row.setPadding(new javafx.geometry.Insets(6));
            row.setStyle(
                    "-fx-border-color: #90caf9; -fx-border-radius: 6; -fx-background-radius: 6; -fx-background-color: #e3f2fd;");
            Label nameLabel = new Label(file);
            nameLabel.setFont(Font.font(14));
            nameLabel.setMinWidth(200);
            Button downloadBtn = new Button("Pobierz");
            downloadBtn.setPrefWidth(100);
            downloadBtn.setStyle("-fx-background-color: #64b5f6; -fx-text-fill: white;");
            downloadBtn.setOnAction(e -> sendDownload(file));
            row.getChildren().addAll(nameLabel, downloadBtn);
            filesListVBox.getChildren().add(row);
        }
    }

    private void sendDownload(String filename) {
        if (filename == null || filename.isEmpty())
            return;
        try {
            writer.write("DOWNLOAD " + filename + "\n");
            writer.flush();
            String sizeLine = reader.readLine();
            if (sizeLine.startsWith("DOWNLOAD ERROR")) {
                outputArea.appendText("> DOWNLOAD " + filename + "\n" + sizeLine + "\n");
                return;
            }
            long fileSize = Long.parseLong(sizeLine);
            Path dir = Paths.get("client_files");
            Files.createDirectories(dir);
            Path filePath = dir.resolve(filename);
            try (OutputStream fileOut = Files.newOutputStream(filePath)) {
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
            outputArea.appendText("> DOWNLOAD " + filename + "\nDOWNLOAD OK: zapisano do " + filePath + "\n");
        } catch (Exception e) {
            outputArea.appendText("DOWNLOAD ERROR: " + e.getMessage() + "\n");
        }
    }

    private void sendUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik do wysłania");
        File file = fileChooser.showOpenDialog(null);
        if (file == null)
            return;
        String filename = file.getName();
        try {
            writer.write("UPLOAD " + filename + "\n");
            writer.flush();
            String ready = reader.readLine();
            if (!"READY".equals(ready)) {
                outputArea.appendText("UPLOAD ERROR: Server not ready\n");
                return;
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
            outputArea.appendText("> UPLOAD " + filename + "\n" + response + "\n");
        } catch (Exception e) {
            outputArea.appendText("UPLOAD ERROR: " + e.getMessage() + "\n");
        }
    }
}