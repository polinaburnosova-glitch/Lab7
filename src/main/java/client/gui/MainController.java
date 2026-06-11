package client.gui;

import client.SimpleClient;
import client.gui.drawing.ArenaCanvas;
import client.gui.localization.LocalizationManager;
import client.gui.drawing.AnimationHelper;
import common.model.HumanBeing;
import common.model.User;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import common.network.ResponseStatus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainController {

    private final SimpleClient client;
    private final User currentUser;
    private Stage stage;

    private ObservableList<HumanBeing> data;
    private TableView<HumanBeing> tableView;
    private ArenaCanvas arenaCanvas;
    private ScheduledExecutorService scheduler;

    public MainController(SimpleClient client, User currentUser) {
        this.client = client;
        this.currentUser = currentUser;
    }

    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        data = FXCollections.observableArrayList();
        loadData();

        tableView = createTableView();

        TextField filterField = new TextField();
        filterField.setPromptText(LocalizationManager.getString("filter"));

        FilteredList<HumanBeing> filteredData = new FilteredList<>(data, p -> true);
        filterField.textProperty().addListener((obs, old, newVal) -> {
            filteredData.setPredicate(human -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lowerFilter = newVal.toLowerCase();
                return human.getName().toLowerCase().contains(lowerFilter) ||
                        human.getOwner().toLowerCase().contains(lowerFilter);
            });
        });

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("ID", "Name", "ImpactSpeed", "Owner");
        sortCombo.setValue("ID");

        SortedList<HumanBeing> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);

        arenaCanvas = new ArenaCanvas(600, 400, data, currentUser);
        arenaCanvas.setOnMouseClicked(event -> {
            Long id = arenaCanvas.getObjectAt(event.getX(), event.getY());
            if (id != null) {
                HumanBinding selected = data.stream()
                        .filter(h -> h.getId().equals(id))
                        .findFirst()
                        .orElse(null);
                if (selected != null) {
                    showObjectInfo(selected);
                }
            }
        });

        Button addBtn = new Button(LocalizationManager.getString("add"));
        addBtn.setOnAction(e -> openEditDialog(null));

        Button editBtn = new Button(LocalizationManager.getString("edit"));
        editBtn.setOnAction(e -> {
            HumanBeing selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getOwner().equals(currentUser.getUsername())) {
                openEditDialog(selected);
            } else if (selected != null) {
                showAlert(LocalizationManager.getString("error.auth"));
            }
        });

        Button deleteBtn = new Button(LocalizationManager.getString("delete"));
        deleteBtn.setOnAction(e -> {
            HumanBeing selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getOwner().equals(currentUser.getUsername())) {
                deleteObject(selected.getId());
            } else if (selected != null) {
                showAlert(LocalizationManager.getString("error.auth"));
            }
        });

        Button refreshBtn = new Button(LocalizationManager.getString("refresh"));
        refreshBtn.setOnAction(e -> loadData());

        Button attackBtn = new Button(LocalizationManager.getString("attack"));
        attackBtn.setOnAction(e -> {
            HumanBinding selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.getOwner().equals(currentUser.getUsername())) {
                attack(selected);
            } else if (selected != null) {
                showAlert("Нельзя атаковать своего героя");
            }
        });

        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll("Русский", "Deutsch", "Magyar", "Español");
        langCombo.setValue("Русский");
        langCombo.setOnAction(e -> {
            String selected = langCombo.getValue();
            LocalizationManager.setLocale(selected);
            updateUITexts(filterField, sortCombo, addBtn, editBtn, deleteBtn, refreshBtn);
            tableView.refresh();
            arenaCanvas.redraw();
        });

        HBox topPanel = new HBox(10,
                new Label(LocalizationManager.getString("welcome") + ", " + currentUser.getUsername()),
                new Region(), langCombo);
        HBox.setHgrow(topPanel.getChildren().get(1), Priority.ALWAYS);

        HBox filterPanel = new HBox(10, filterField, sortCombo);

        HBox buttonPanel = new HBox(10, addBtn, editBtn, deleteBtn, refreshBtn);

        VBox leftPanel = new VBox(10, filterPanel, tableView);
        VBox rightPanel = new VBox(10, arenaCanvas, buttonPanel);

        HBox mainPanel = new HBox(20, leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        VBox root = new VBox(10, topPanel, mainPanel);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 1200, 700);
        stage.setTitle(LocalizationManager.getString("app.title"));
        stage.setScene(scene);
        stage.show();

        startAutoRefresh();
    }

    private TableView<HumanBeing> createTableView() {
        TableView<HumanBeing> table = new TableView<>();

        TableColumn<HumanBeing, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(cell.getValue()::getId));

        TableColumn<HumanBeing, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(cell.getValue()::getName));

        TableColumn<HumanBeing, Double> xCol = new TableColumn<>("X");
        xCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() ->
                        cell.getValue().getCoordinates().getX()));

        TableColumn<HumanBeing, Float> yCol = new TableColumn<>("Y");
        yCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() ->
                        cell.getValue().getCoordinates().getY()));

        TableColumn<HumanBeing, Float> speedCol = new TableColumn<>("ImpactSpeed");
        speedCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(cell.getValue()::getImpactSpeed));

        TableColumn<HumanBeing, String> weaponCol = new TableColumn<>("WeaponType");
        weaponCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() ->
                        cell.getValue().getWeaponType().name()));

        TableColumn<HumanBeing, String> moodCol = new TableColumn<>("Mood");
        moodCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> {
                    Mood mood = cell.getValue().getMood();
                    return mood == null ? "null" : mood.name();
                }));

        TableColumn<HumanBeing, Boolean> carCol = new TableColumn<>("CarCool");
        carCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() ->
                        cell.getValue().getCar().getCool()));

        TableColumn<HumanBeing, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(cell.getValue()::getOwner));

        table.getColumns().addAll(idCol, nameCol, xCol, yCol, speedCol, weaponCol, moodCol, carCol, ownerCol);

        return table;
    }

    private void loadData() {
        new Thread(() -> {
            try {
                Request request = new Request(CommandType.SHOW, null, currentUser);
                client.sendRequest(request);
                Response response = client.receiveResponse();

                if (response.isSuccess() && response.getData() != null) {
                    List<HumanBeing> list = (List<HumanBeing>) response.getData();
                    Platform.runLater(() -> {
                        data.setAll(list);
                        arenaCanvas.redraw();

                        List<HumanBeing> top3 = data.stream()
                                .sorted((a, b) -> Float.compare(b.getImpactSpeed(), a.getImpactSpeed()))
                                .limit(3)
                                .collect(Collectors.toList());

                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void openEditDialog(HumanBeing human) {
        EditController editController = new EditController(client, currentUser, human);
        editController.showAndWait();
        if (editController.isSaved()) {
            loadData();

            if (human == null) {
                AnimationHelper.animateAdd(arenaCanvas);
            } else {
                AnimationHelper.animateUpdate(arenaCanvas);
            }
        }
    }

    private void deleteObject(long id) {
        HumanBeing toDelete = data.stream()
                .filter(h -> h.getId() == id)
                .findFirst()
                .orElse(null);

        if (toDelete == null) return;

        double x = toDelete.getCoordinates().getX() * 50;
        double y = toDelete.getCoordinates().getY() * 50;
        AnimationHelper.animateHit(arenaCanvas, x, y);

        new Thread(() -> {
            try {
                Request request = new Request(CommandType.REMOVE_BY_ID, new Object[]{id}, currentUser);
                client.sendRequest(request);
                Response response = client.receiveResponse();
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        loadData();
                    } else {
                        showAlert(response.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showObjectInfo(HumanBeing human) {
        String info = "ID: " + human.getId() + "\n" +
                "Name: " + human.getName() + "\n" +
                "Coordinates: (" + human.getCoordinates().getX() + ", " + human.getCoordinates().getY() + ")\n" +
                "ImpactSpeed: " + human.getImpactSpeed() + "\n" +
                "Weapon: " + human.getWeaponType().name() + "\n" +
                "Mood: " + (human.getMood() == null ? "null" : human.getMood().name()) + "\n" +
                "Owner: " + human.getOwner();
        showAlert(info);
    }

    private void startAutoRefresh() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            loadData();
        }, 2, 2, TimeUnit.SECONDS);
    }

    private void updateUITexts(TextField filter, ComboBox<String> sort, Button add, Button edit, Button delete, Button refresh) {
        filter.setPromptText(LocalizationManager.getString("filter"));
        add.setText(LocalizationManager.getString("add"));
        edit.setText(LocalizationManager.getString("edit"));
        delete.setText(LocalizationManager.getString("delete"));
        refresh.setText(LocalizationManager.getString("refresh"));
        stage.setTitle(LocalizationManager.getString("app.title"));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LocalizationManager.getString("app.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private void attack(HumanBeing defender) {
        HumanBeing attacker = null;
        for (HumanBeing h : data) {
            if (h.getOwner().equals(currentUser.getUsername())) {
                attacker = h;
                break;
            }
        }

        if (attacker == null) {
            showAlert("У вас нет героя для атаки");
            return;
        }

        double x = defender.getCoordinates().getX() * 50;
        double y = defender.getCoordinates().getY() * 50;
        AnimationHelper.animateHit(arenaCanvas, x, y);

        new Thread(() -> {
            try {
                Request request = new Request(CommandType.ATTACK,
                        new Object[]{attacker.getId(), defender.getId()}, currentUser);
                client.sendRequest(request);
                Response response = client.receiveResponse();

                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showAlert(response.getMessage());
                        loadData();
                    } else {
                        showAlert(response.getMessage());
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}