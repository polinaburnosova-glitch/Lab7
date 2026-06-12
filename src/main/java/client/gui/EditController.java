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
import javafx.util.StringConverter;

import java.io.IOException;
import java.text.ParseException;

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

    public EditController(SimpleClient client, User currentUser, HumanBeing human,
                          boolean isAddIfMin, boolean isAddIfMax) {
        this.client = client;
        this.currentUser = currentUser;
        this.editingHuman = human;
        this.isEditMode = human != null && human.getId() != null;
        this.isAddIfMin = isAddIfMin;
        this.isAddIfMax = isAddIfMax;
    }

    public void showAndWait() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(resolveTitle());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        int row = 0;

        grid.add(new Label(LocalizationManager.getString("col.name") + ":"), 0, row);
        nameField = new TextField();
        grid.add(nameField, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.x") + ":"), 0, row);
        xField = new TextField();
        grid.add(xField, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.y") + ":"), 0, row);
        yField = new TextField();
        grid.add(yField, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.real_hero") + ":"), 0, row);
        realHeroCombo = createBooleanCombo();
        grid.add(realHeroCombo, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.has_toothpick") + ":"), 0, row);
        hasToothpickCombo = createBooleanCombo();
        grid.add(hasToothpickCombo, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.impact_speed") + ":"), 0, row);
        impactSpeedField = new TextField();
        grid.add(impactSpeedField, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.soundtrack") + ":"), 0, row);
        soundtrackField = new TextField();
        grid.add(soundtrackField, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.weapon_type") + ":"), 0, row);
        weaponCombo = new ComboBox<>();
        weaponCombo.getItems().addAll(WeaponType.values());
        weaponCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(WeaponType object) {
                return object == null ? "" : LocalizationManager.formatWeaponType(object);
            }

            @Override
            public WeaponType fromString(String string) {
                return null;
            }
        });
        grid.add(weaponCombo, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.mood") + ":"), 0, row);
        moodCombo = new ComboBox<>();
        moodCombo.getItems().add(null);
        moodCombo.getItems().addAll(Mood.values());
        moodCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Mood object) {
                return LocalizationManager.formatMood(object);
            }

            @Override
            public Mood fromString(String string) {
                return null;
            }
        });
        grid.add(moodCombo, 1, row++);

        grid.add(new Label(LocalizationManager.getString("col.car_cool") + ":"), 0, row);
        carCoolCombo = createBooleanCombo();
        grid.add(carCoolCombo, 1, row++);

        if (isEditMode) {
            fillFields();
        } else {
            applyDefaults();
        }

        Button saveBtn = new Button(LocalizationManager.getString("save"));
        saveBtn.setDefaultButton(true);
        saveBtn.setOnAction(e -> save());

        Button cancelBtn = new Button(LocalizationManager.getString("cancel"));
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
        VBox root = new VBox(20, grid, buttonBox);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 440, 540);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private ComboBox<Boolean> createBooleanCombo() {
        ComboBox<Boolean> combo = new ComboBox<>();
        combo.getItems().addAll(true, false);
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Boolean object) {
                return LocalizationManager.formatBoolean(object);
            }

            @Override
            public Boolean fromString(String string) {
                return null;
            }
        });
        return combo;
    }

    private void applyDefaults() {
        realHeroCombo.setValue(true);
        hasToothpickCombo.setValue(false);
        weaponCombo.setValue(WeaponType.KNIFE);
        moodCombo.setValue(Mood.APATHY);
        carCoolCombo.setValue(false);
        impactSpeedField.setText(LocalizationManager.formatNumber(1));
    }

    private String resolveTitle() {
        if (isEditMode) {
            return LocalizationManager.getString("edit.title");
        }
        if (isAddIfMin) {
            return LocalizationManager.getString("add_if_min.title");
        }
        if (isAddIfMax) {
            return LocalizationManager.getString("add_if_max.title");
        }
        return LocalizationManager.getString("add.title");
    }

    private void fillFields() {
        nameField.setText(editingHuman.getName());
        xField.setText(LocalizationManager.formatNumber(editingHuman.getCoordinates().getX()));
        yField.setText(LocalizationManager.formatNumber(editingHuman.getCoordinates().getY()));
        realHeroCombo.setValue(editingHuman.getRealHero());
        hasToothpickCombo.setValue(editingHuman.getHasToothpick());
        impactSpeedField.setText(LocalizationManager.formatNumber(editingHuman.getImpactSpeed()));
        soundtrackField.setText(editingHuman.getSoundtrackName());
        weaponCombo.setValue(editingHuman.getWeaponType());
        moodCombo.setValue(editingHuman.getMood());
        carCoolCombo.setValue(editingHuman.getCar().getCool());
    }

    private void save() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showAlert(LocalizationManager.getString("error.validation.name"));
                return;
            }

            double x = LocalizationManager.parseNumber(xField.getText());
            float y = (float) LocalizationManager.parseNumber(yField.getText());
            float impactSpeed = (float) LocalizationManager.parseNumber(impactSpeedField.getText());

            if (impactSpeed > 657) {
                showAlert(LocalizationManager.getString("error.validation.impact_speed"));
                return;
            }

            String soundtrack = soundtrackField.getText().trim();
            if (soundtrack.isEmpty()) {
                showAlert(LocalizationManager.getString("error.validation.soundtrack"));
                return;
            }

            if (realHeroCombo.getValue() == null || hasToothpickCombo.getValue() == null
                    || weaponCombo.getValue() == null || carCoolCombo.getValue() == null) {
                showAlert(LocalizationManager.getString("error.validation.number"));
                return;
            }

            HumanBeing human = new HumanBeing(
                    name,
                    new Coordinates(x, y),
                    realHeroCombo.getValue(),
                    hasToothpickCombo.getValue(),
                    impactSpeed,
                    soundtrack,
                    weaponCombo.getValue(),
                    moodCombo.getValue(),
                    new Car(carCoolCombo.getValue()),
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

        } catch (ParseException e) {
            showAlert(LocalizationManager.getString("error.validation.number"));
        } catch (IOException | ClassNotFoundException e) {
            showAlert(LocalizationManager.getString("error.validation.network") + ": " + e.getMessage());
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
