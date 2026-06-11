package client.gui;

import client.SimpleClient;
import client.gui.localization.LocalizationManager;
import common.EnvLoader;
import javafx.application.Application;
import javafx.stage.Stage;

public class Launcher extends Application {

    private SimpleClient client;

    @Override
    public void start(Stage primaryStage) {
        String host = EnvLoader.get("CLIENT_HOST", "localhost");
        int port = EnvLoader.getInt("CLIENT_PORT", 5556);

        String defaultLanguage = EnvLoader.get("DEFAULT_LANGUAGE", "Русский");
        LocalizationManager.setLocale(defaultLanguage);

        client = new SimpleClient(host, port);

        LoginController loginController = new LoginController(client);
        loginController.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}