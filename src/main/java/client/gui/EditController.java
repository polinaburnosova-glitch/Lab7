package client.gui;

import client.SimpleClient;
import client.gui.localization.LocalizationManager;
import common.model.*;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import common.network.ResponseStatus;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class EditController {

    private final SimpleClient client;
    private final User currentUser;
    private final HumanBeing editingHuman;
    private final boolean isEditMode;
    private final boolean isAddIfMin;
    private final boolean isAddIfMax;
    private Stage stage;
    private boolean saved = false;

    private TextField nameField;
    private TextField xField;
    private TextField yField;
    private ComboBox<Boolean> realHeroCombo;
    private ComboBox<Boolean> hasToothpickCombo;
    private TextField impactSpeedField;
    private TextField soundtrackField;
    private ComboBox<WeaponType> weaponCombo;
    private ComboBox<Mood> moodCombo;
    private ComboBox<Boolean> carCoolCombo;

    // Конструктор для обычного ADD (без флагов)
    public EditController(SimpleClient client, User currentUser, HumanBeing human) {
        this(client, currentUser, human, false, false);
    }

    // Конструктор для ADD_IF_MIN
    public EditController(SimpleClient client, User currentUser, HumanBeing human, boolean isAddIfMin) {
        this(client, currentUser, human, isAddIfMin, false);
    }

    // Основной конструктор
    public EditController(SimpleClient client, User currentUser, HumanBeing human, boolean isAddIfMin, boolean isAddIfMax) {
        this.client = client;
        this.currentUser = currentUser;
        this.editingHuman = human;
        this.isEditMode = (human != null && human.getId() != null);
        this.isAddIfMin = isAddIfMin;
        this.isAddIfMax = isAddIfMax;
    }

    public void showAndWait() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(isEditMode ?
                LocalizationManager.getString("edit.title") :
                LocalizationManager.getString("add.title"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        int row = 0;

        grid.add(new Label(LocalizationManager.getString("col.name") + ":"), 0, row);
        nameField = new TextField();
        nameField.setPromptText("Name");
        grid.add(nameField, 1, row++);

        grid.add(new Label("X:"), 0, row);
        xField = new TextField();
        xField.setPromptText("X");
        grid.add(xField, 1, row++);

        grid.add(new Label("Y:"), 0, row);
        yField = new TextField();
        yField.setPromptText("Y");
        grid.add(yField, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.real_hero") + ":"), 0, row);
        realHeroCombo = new ComboBox<>();
        realHeroCombo.getItems().addAll(true, false);
        grid.add(realHeroCombo, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.has_toothpick") + ":"), 0, row);
        hasToothpickCombo = new ComboBox<>();
        hasToothpickCombo.getItems().addAll(true, false);
        grid.add(hasToothpickCombo, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.impact_speed") + ":"), 0, row);
        impactSpeedField = new TextField();
        impactSpeedField.setPromptText("<=657");
        grid.add(impactSpeedField, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.soundtrack") + ":"), 0, row);
        soundtrackField = new TextField();
        grid.add(soundtrackField, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.weapon_type") + ":"), 0, row);
        weaponCombo = new ComboBox<>();
        weaponCombo.getItems().addAll(WeaponType.values());
        grid.add(weaponCombo, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.mood") + ":"), 0, row);
        moodCombo = new ComboBox<>();
        moodCombo.getItems().addAll(Mood.values());
        moodCombo.getItems().add(null);
        grid.add(moodCombo, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.car_cool") + ":"), 0, row);
        carCoolCombo = new ComboBox<>();
        carCoolCombo.getItems().addAll(true, false);
        grid.add(carCoolCombo, 1, row++);

        if (isEditMode && editingHuman != null) {
            fillFields();
        }

        Button saveBtn = new Button(LocalizationManager.getString("save"));
        saveBtn.setOnAction(e -> save());

        Button cancelBtn = new Button(LocalizationManager.getString("cancel"));
        cancelBtn.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10, saveBtn, cancelBtn);

        VBox root = new VBox(20, grid, buttonBox);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void fillFields() {
        nameField.setText(editingHuman.getName());
        xField.setText(String.valueOf(editingHuman.getCoordinates().getX()));
        yField.setText(String.valueOf(editingHuman.getCoordinates().getY()));
        realHeroCombo.setValue(editingHuman.getRealHero());
        hasToothpickCombo.setValue(editingHuman.getHasToothpick());
        impactSpeedField.setText(String.valueOf(editingHuman.getImpactSpeed()));
        soundtrackField.setText(editingHuman.getSoundtrackName());
        weaponCombo.setValue(editingHuman.getWeaponType());
        moodCombo.setValue(editingHuman.getMood());
        carCoolCombo.setValue(editingHuman.getCar().getCool());
    }

    private void save() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showAlert("Name cannot be empty");
                return;
            }

            double x = Double.parseDouble(xField.getText().trim());
            float y = Float.parseFloat(yField.getText().trim());
            float impactSpeed = Float.parseFloat(impactSpeedField.getText().trim());

            if (impactSpeed > 657) {
                showAlert("ImpactSpeed must be <= 657");
                return;
            }

            String soundtrack = soundtrackField.getText().trim();
            if (soundtrack.isEmpty()) {
                showAlert("Soundtrack cannot be empty");
                return;
            }

            Coordinates coordinates = new Coordinates(x, y);
            Car car = new Car(carCoolCombo.getValue());

            HumanBeing human = new HumanBeing(
                    name, coordinates,
                    realHeroCombo.getValue(),
                    hasToothpickCombo.getValue(),
                    impactSpeed,
                    soundtrack,
                    weaponCombo.getValue(),
                    moodCombo.getValue(),
                    car,
                    currentUser.getUsername()
            );

            CommandType commandType;
            Object[] args;

            if (isEditMode) {
                human.setId(editingHuman.getId());
                human.setCreationDate(editingHuman.getCreationDate());
                commandType = CommandType.UPDATE;
                args = new Object[]{editingHuman.getId(), human};
            } else if (isAddIfMin) {
                commandType = CommandType.ADD_IF_MIN;
                args = new Object[]{human};
            } else if (isAddIfMax) {
                commandType = CommandType.ADD_IF_MAX;
                args = new Object[]{human};
            } else {
                commandType = CommandType.ADD;
                args = new Object[]{human};
            }

            Request request = new Request(commandType, args, currentUser);
            client.sendRequest(request);
            Response response = client.receiveResponse();

            if (response.getStatus() == ResponseStatus.OK) {
                saved = true;
                stage.close();
            } else {
                showAlert(response.getMessage());
            }

        } catch (NumberFormatException e) {
            showAlert("Invalid number format");
        } catch (IOException | ClassNotFoundException e) {
            showAlert("Network error: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(LocalizationManager.getString("app.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isSaved() {
        return saved;
    }
}