package ftp.gusamyky.client;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;

public class ClientGUI extends Application {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private LoginTabController loginTabController;
    private RegisterTabController registerTabController;
    private FileTabController fileTabController;
    private HistoryTabController historyTabController;
    private ConsoleTabController consoleTabController;

    private TabPane tabPane;
    private Tab loginTab, registerTab, filesTab, historyTab, consoleTab;

    private String loggedUser;

    @Override
    public void start(Stage primaryStage) throws Exception {
        tabPane = new TabPane();

        // Login Tab
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/client/LoginTab.fxml"));
        Parent loginRoot = loginLoader.load();
        loginTabController = loginLoader.getController();
        loginTab = new Tab("Logowanie", loginRoot);
        loginTab.setClosable(false);

        // Register Tab
        FXMLLoader regLoader = new FXMLLoader(getClass().getResource("/client/RegisterTab.fxml"));
        Parent regRoot = regLoader.load();
        registerTabController = regLoader.getController();
        registerTab = new Tab("Rejestracja", regRoot);
        registerTab.setClosable(false);

        // Files Tab
        FXMLLoader filesLoader = new FXMLLoader(getClass().getResource("/client/FileTab.fxml"));
        Parent filesRoot = filesLoader.load();
        fileTabController = filesLoader.getController();
        filesTab = new Tab("Pliki", filesRoot);
        filesTab.setClosable(false);

        // History Tab
        FXMLLoader historyLoader = new FXMLLoader(getClass().getResource("/client/HistoryTab.fxml"));
        Parent historyRoot = historyLoader.load();
        historyTabController = historyLoader.getController();
        historyTab = new Tab("Historia", historyRoot);
        historyTab.setClosable(false);

        // Console Tab
        FXMLLoader consoleLoader = new FXMLLoader(getClass().getResource("/client/ConsoleTab.fxml"));
        Parent consoleRoot = consoleLoader.load();
        consoleTabController = consoleLoader.getController();
        consoleTab = new Tab("Konsola", consoleRoot);
        consoleTab.setClosable(false);

        tabPane.getTabs().addAll(loginTab, registerTab, filesTab, historyTab, consoleTab);

        Scene scene = new Scene(tabPane, 700, 500);
        primaryStage.setTitle("FTP_FS Client GUI");
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
        wireControllers();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 2121);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // Odczytaj powitanie serwera (opcjonalnie)
            reader.readLine();
        } catch (IOException e) {
            // Możesz dodać obsługę błędu połączenia
        }
    }

    private void wireControllers() {
        // Przekaż socket, reader, writer do każdego kontrolera
        loginTabController.setSocket(socket, reader, writer);
        registerTabController.setSocket(socket, reader, writer);
        fileTabController.setSocket(socket, reader, writer);
        historyTabController.setSocket(socket, reader, writer);
        consoleTabController.setSocket(socket, reader, writer);

        // Po udanym logowaniu przełącz na zakładkę "Pliki" i ustaw użytkownika w
        // historii
        loginTabController.setOnLoginSuccess(() -> {
            this.loggedUser = loginTabController.getLoginUsername();
            historyTabController.setLoggedUser(this.loggedUser);
            tabPane.getSelectionModel().select(filesTab);
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}