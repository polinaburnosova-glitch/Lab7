package client.gui;

import client.SimpleClient;
import client.gui.localization.LocalizationManager;
import common.EnvLoader;
import common.model.User;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import common.network.ResponseStatus;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    private final SimpleClient client;
    private Stage stage;

    private static final String[] LANGUAGES = {"Русский", "Deutsch", "Magyar", "Español"};

    public LoginController(SimpleClient client) {
        this.client = client;
    }

    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        Label titleLabel = new Label(LocalizationManager.getString("app.title"));
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText(LocalizationManager.getString("username"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(LocalizationManager.getString("password"));

        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll(LANGUAGES);

        String defaultLanguage = EnvLoader.get("DEFAULT_LANGUAGE", "Русский");
        langCombo.setValue(defaultLanguage);

        langCombo.setOnAction(e -> {
            String selected = langCombo.getValue();
            LocalizationManager.setLocale(selected);
            updateTexts(titleLabel, usernameField, passwordField, langCombo);
        });

        Button loginBtn = new Button(LocalizationManager.getString("login"));
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            login(username, password);
        });

        Button registerBtn = new Button(LocalizationManager.getString("register"));
        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            register(username, password);
        });

        VBox root = new VBox(15, titleLabel, usernameField, passwordField, langCombo, loginBtn, registerBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 350, 350);
        stage.setTitle(LocalizationManager.getString("app.title"));
        stage.setScene(scene);
        stage.show();
    }

    private void updateTexts(Label title, TextField username, PasswordField password, ComboBox<String> lang) {
        title.setText(LocalizationManager.getString("app.title"));
        username.setPromptText(LocalizationManager.getString("username"));
        password.setPromptText(LocalizationManager.getString("password"));
        stage.setTitle(LocalizationManager.getString("app.title"));
    }

    private void login(String username, String password) {
        try {
            Request request = new Request(CommandType.LOGIN, new Object[]{username, password}, null);
            client.sendRequest(request);
            Response response = client.receiveResponse();

            if (response.getStatus() == ResponseStatus.OK && response.getData() != null) {
                User user = (User) response.getData().get(0);
                client.setCurrentUser(user);

                MainController mainController = new MainController(client, user);
                mainController.start(stage);
            } else {
                showAlert(LocalizationManager.getString("error.auth"));
            }
        } catch (IOException | ClassNotFoundException e) {
            showAlert(LocalizationManager.getString("error.connection") + ": " + e.getMessage());
        }
    }

    private void register(String username, String password) {
        try {
            Request request = new Request(CommandType.REGISTER, new Object[]{username, password}, null);
            client.sendRequest(request);
            Response response = client.receiveResponse();

            if (response.getStatus() == ResponseStatus.OK) {
                showAlert(LocalizationManager.getString("status.success") + " " + LocalizationManager.getString("register"));
            } else {
                showAlert(response.getMessage());
            }
        } catch (IOException | ClassNotFoundException e) {
            showAlert(LocalizationManager.getString("error.connection") + ": " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LocalizationManager.getString("app.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}