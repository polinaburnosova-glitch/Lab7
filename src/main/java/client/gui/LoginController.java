package client.gui;

import client.SimpleClient;
import client.gui.localization.LocalizationManager;
import common.EnvLoader;
import common.model.User;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import common.network.ResponseStatus;
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
        langCombo.getItems().addAll(
                LocalizationManager.LANG_RU,
                LocalizationManager.LANG_DE,
                LocalizationManager.LANG_HU,
                LocalizationManager.LANG_ES
        );

        String defaultLanguage = EnvLoader.get("DEFAULT_LANGUAGE", LocalizationManager.LANG_RU);
        langCombo.setValue(defaultLanguage);
        LocalizationManager.setLocale(defaultLanguage);

        Button loginBtn = new Button(LocalizationManager.getString("login"));
        loginBtn.setDefaultButton(true);
        loginBtn.setOnAction(e -> login(usernameField.getText().trim(), passwordField.getText().trim()));

        Button registerBtn = new Button(LocalizationManager.getString("register"));
        registerBtn.setOnAction(e -> register(usernameField.getText().trim(), passwordField.getText().trim()));

        langCombo.setOnAction(e -> {
            LocalizationManager.setLocale(langCombo.getValue());
            updateTexts(titleLabel, usernameField, passwordField, loginBtn, registerBtn);
        });

        Label languageLabel = new Label(LocalizationManager.getString("language") + ":");

        VBox root = new VBox(15, titleLabel, usernameField, passwordField, languageLabel, langCombo, loginBtn, registerBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 380, 380);
        stage.setTitle(LocalizationManager.getString("app.title"));
        stage.setScene(scene);
        stage.show();
    }

    private void updateTexts(Label title, TextField username, PasswordField password,
                             Button loginBtn, Button registerBtn) {
        title.setText(LocalizationManager.getString("app.title"));
        username.setPromptText(LocalizationManager.getString("username"));
        password.setPromptText(LocalizationManager.getString("password"));
        loginBtn.setText(LocalizationManager.getString("login"));
        registerBtn.setText(LocalizationManager.getString("register"));
        stage.setTitle(LocalizationManager.getString("app.title"));
    }

    private void login(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(LocalizationManager.getString("error.empty_credentials"));
            return;
        }
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
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(LocalizationManager.getString("error.empty_credentials"));
            return;
        }
        try {
            Request request = new Request(CommandType.REGISTER, new Object[]{username, password}, null);
            client.sendRequest(request);
            Response response = client.receiveResponse();

            if (response.getStatus() == ResponseStatus.OK) {
                showAlert(LocalizationManager.getString("status.success") + ": "
                        + LocalizationManager.getString("register"));
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
