package ftp.gusamyky.client;

import ftp.gusamyky.client.controller.LoginTabController;
import ftp.gusamyky.client.model.AppState;
import ftp.gusamyky.client.service.network.ClientNetworkService;
import ftp.gusamyky.client.util.ConfigManager;
import ftp.gusamyky.client.util.ExceptionAlertUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.util.Objects;

public class ClientGUI extends Application {
    private final ClientNetworkService networkService = ClientNetworkService.getInstance();
    private final AppState appState = AppState.getInstance();
    private final ConfigManager configManager = ConfigManager.getInstance();
    private LoginTabController loginTabController;
    private TabPane tabPane;
    private Tab loginTab, registerTab, filesTab, historyTab, consoleTab;

    public static void main(String[] args) {
        launch(args);
    }

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
        regLoader.getController();
        registerTab = new Tab("Rejestracja", regRoot);
        registerTab.setClosable(false);

        // Files Tab
        FXMLLoader filesLoader = new FXMLLoader(getClass().getResource("/client/FileTab.fxml"));
        Parent filesRoot = filesLoader.load();
        filesLoader.getController();
        filesTab = new Tab("Pliki", filesRoot);
        filesTab.setClosable(false);

        // History Tab
        FXMLLoader historyLoader = new FXMLLoader(getClass().getResource("/client/HistoryTab.fxml"));
        Parent historyRoot = historyLoader.load();
        historyLoader.getController();
        historyTab = new Tab("Historia", historyRoot);
        historyTab.setClosable(false);

        // Console Tab
        FXMLLoader consoleLoader = new FXMLLoader(getClass().getResource("/client/ConsoleTab.fxml"));
        Parent consoleRoot = consoleLoader.load();
        consoleLoader.getController();
        consoleTab = new Tab("Konsola", consoleRoot);
        consoleTab.setClosable(false);

        tabPane.getTabs().addAll(loginTab, registerTab, filesTab, historyTab, consoleTab);

        // Blokowanie tabów logowania/rejestracji po zalogowaniu
        AppState.getInstance().loggedUserProperty().addListener((obs, oldUser, newUser) -> {
            boolean loggedIn = newUser != null;
            loginTab.setDisable(loggedIn);
            registerTab.setDisable(loggedIn);
        });
        // Ustaw stan początkowy
        boolean loggedIn = AppState.getInstance().getLoggedUser() != null;
        loginTab.setDisable(loggedIn);
        registerTab.setDisable(loggedIn);

        Scene scene = new Scene(tabPane, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/client/app.css")).toExternalForm());
        primaryStage.setTitle("FTP_FS Client GUI");
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
        wireControllers();
    }

    private void connectToServer() {
        try {
            networkService.connect(configManager.getServerHost(), configManager.getServerPort());
        } catch (Exception e) {
            ExceptionAlertUtil.showConnectionError("Nie udało się połączyć z serwerem: " + e.getMessage());
        }
    }

    private void wireControllers() {
        // Po udanym logowaniu przełącz na zakładkę "Pliki"
        loginTabController.setOnLoginSuccess(() -> {
            appState.setLoggedUser(new ftp.gusamyky.client.model.User(loginTabController.getLoginUsername()));
            tabPane.getSelectionModel().select(filesTab);
        });
    }
}