package client.gui;

import client.SimpleClient;
import client.gui.drawing.AnimationHelper;
import client.gui.drawing.ArenaCanvas;
import client.gui.localization.LocalizationManager;
import client.script.ScriptExecutor;
import common.model.HumanBeing;
import common.model.Mood;
import common.model.User;
import common.model.WeaponType;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import javafx.util.StringConverter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Timer;
import java.util.TimerTask;
import java.time.format.DateTimeFormatter;

public class MainController {

    private final SimpleClient client;
    private final User currentUser;
    private Stage stage;
    private Timer filterTimer;

    private final ObservableList<HumanBeing> allData = FXCollections.observableArrayList();
    private final ObservableList<HumanBeing> displayedData = FXCollections.observableArrayList();
    private TableView<HumanBeing> tableView;
    private ArenaCanvas arenaCanvas;
    private ScheduledExecutorService scheduler;

    private TextField nameFilterField;
    private TextField ownerFilterField;
    private TextField minXFilterField;
    private TextField maxXFilterField;
    private TextField minYFilterField;
    private TextField maxYFilterField;
    private DatePicker fromDateFilter;
    private DatePicker toDateFilter;
    private ComboBox<String> sortCombo;
    private ComboBox<Mood> moodFilter;
    private ComboBox<String> langCombo;
    private ComboBox<WeaponType> weaponFilter;
    private CheckBox hasCarFilter;
    private CheckBox hasToothpickFilter;

    private Label welcomeLabel;
    private Label tableLabel;
    private Label arenaLabel;
    private Label sortLabel;
    private Label tablePlaceholder;
    private int sortIndex;

    private VBox crudButtons;
    private VBox actionButtons;
    private VBox specialButtons;

    private Button addBtn;
    private Button editBtn;
    private Button deleteBtn;
    private Button refreshBtn;
    private Button attackBtn;
    private Button infoBtn;
    private Button clearBtn;
    private Button helpBtn;
    private Button scriptBtn;
    private Button removeFirstBtn;
    private Button minByIdBtn;
    private Button addIfMinBtn;
    private Button addIfMaxBtn;

    private final List<TableColumn<HumanBeing, ?>> tableColumns = new ArrayList<>();

    public MainController(SimpleClient client, User currentUser) {
        this.client = client;
        this.currentUser = currentUser;
    }

    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        tableView = createTableView();
        tablePlaceholder = new Label(LocalizationManager.getString("table.empty"));
        tableView.setPlaceholder(tablePlaceholder);

        nameFilterField = new TextField();
        nameFilterField.setPromptText(LocalizationManager.getString("filter.name"));
        nameFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());

        ownerFilterField = new TextField();
        ownerFilterField.setPromptText(LocalizationManager.getString("filter.owner"));
        ownerFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());

        minXFilterField = new TextField();
        minXFilterField.setPromptText(LocalizationManager.getString("filter.min_x"));
        maxXFilterField = new TextField();
        maxXFilterField.setPromptText(LocalizationManager.getString("filter.max_x"));
        minXFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());
        maxXFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());

        minYFilterField = new TextField();
        minYFilterField.setPromptText(LocalizationManager.getString("filter.min_y"));
        maxYFilterField = new TextField();
        maxYFilterField.setPromptText(LocalizationManager.getString("filter.max_y"));
        minYFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());
        maxYFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());

        fromDateFilter = new DatePicker();
        fromDateFilter.setPromptText(LocalizationManager.getString("filter.from_date"));
        toDateFilter = new DatePicker();
        toDateFilter.setPromptText(LocalizationManager.getString("filter.to_date"));
        fromDateFilter.valueProperty().addListener((obs, old, val) -> scheduleFilter());
        toDateFilter.valueProperty().addListener((obs, old, val) -> scheduleFilter());

        hasCarFilter = new CheckBox(LocalizationManager.getString("filter.has_car"));
        hasToothpickFilter = new CheckBox(LocalizationManager.getString("filter.has_toothpick"));
        hasCarFilter.setOnAction(e -> scheduleFilter());
        hasToothpickFilter.setOnAction(e -> scheduleFilter());

        sortCombo = new ComboBox<>();
        refreshSortComboItems();
        sortCombo.setOnAction(e -> {
            sortIndex = sortCombo.getSelectionModel().getSelectedIndex();
            applyFiltersAndSort();
        });

        moodFilter = new ComboBox<>();
        moodFilter.getItems().add(null);
        moodFilter.getItems().addAll(Mood.values());
        moodFilter.setConverter(createMoodConverter());
        moodFilter.setPromptText(LocalizationManager.getString("mood.all"));
        moodFilter.setValue(null);
        moodFilter.setOnAction(e -> applyFiltersAndSort());

        weaponFilter = new ComboBox<>();
        weaponFilter.getItems().add(null);
        weaponFilter.getItems().addAll(WeaponType.values());
        weaponFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(WeaponType object) {
                return object == null ? LocalizationManager.getString("weapon.all") : LocalizationManager.formatWeaponType(object);
            }
            @Override
            public WeaponType fromString(String string) {
                return null;
            }
        });
        weaponFilter.setPromptText(LocalizationManager.getString("weapon.filter"));
        weaponFilter.setValue(null);
        weaponFilter.setOnAction(e -> applyFiltersAndSort());

        arenaCanvas = new ArenaCanvas(800, 600, allData, currentUser);
        arenaCanvas.setOnMouseClicked(event -> {
            Long id = arenaCanvas.getObjectAt(event.getX(), event.getY());
            if (id != null) {
                allData.stream()
                        .filter(h -> h.getId().equals(id))
                        .findFirst()
                        .ifPresent(this::handleCanvasObjectClick);
            }
        });

        createButtons();

        langCombo = new ComboBox<>();
        langCombo.getItems().addAll(
                LocalizationManager.LANG_RU,
                LocalizationManager.LANG_DE,
                LocalizationManager.LANG_HU,
                LocalizationManager.LANG_ES
        );
        langCombo.setValue(LocalizationManager.getCurrentLanguageName());
        langCombo.setOnAction(e -> {
            LocalizationManager.setLocale(langCombo.getValue());
            refreshLocalizedTexts();
        });

        welcomeLabel = new Label(buildWelcomeText());
        tableLabel = new Label(LocalizationManager.getString("label.table"));
        arenaLabel = new Label(LocalizationManager.getString("label.arena"));
        sortLabel = new Label(LocalizationManager.getString("label.sort_by"));

        GridPane filterPanel = createFilterPanel();

        HBox buttonPanel = createButtonPanel();

        VBox leftPanel = new VBox(8, tableLabel, filterPanel, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        VBox rightPanel = new VBox(8, arenaLabel, arenaCanvas, buttonPanel);
        VBox.setVgrow(arenaCanvas, Priority.ALWAYS);

        HBox mainPanel = new HBox(20, leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        VBox topPanel = new VBox(5);
        HBox welcomePanel = new HBox(10, welcomeLabel, new Region(), langCombo);
        HBox.setHgrow(welcomePanel.getChildren().get(1), Priority.ALWAYS);
        topPanel.getChildren().addAll(welcomePanel, new Separator());

        VBox root = new VBox(10, topPanel, mainPanel);
        root.setPadding(new Insets(10));

        tableView.setItems(displayedData);

        Scene scene = new Scene(root, 1400, 800);
        stage.setTitle(LocalizationManager.getString("app.title"));
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> stop());
        stage.show();

        loadData();
        startAutoRefresh();
    }

    private void scheduleFilter() {
        if (filterTimer != null) filterTimer.cancel();
        filterTimer = new Timer();
        filterTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> applyFiltersAndSort());
            }
        }, 300);
    }

    private GridPane createFilterPanel() {
        GridPane filterPanel = new GridPane();
        filterPanel.setHgap(10);
        filterPanel.setVgap(5);
        filterPanel.setPadding(new Insets(5));
        filterPanel.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 5;");

        int row = 0;

        filterPanel.add(new Label(LocalizationManager.getString("filter.name") + ":"), 0, row);
        filterPanel.add(nameFilterField, 1, row);
        filterPanel.add(new Label(LocalizationManager.getString("filter.owner") + ":"), 2, row);
        filterPanel.add(ownerFilterField, 3, row);

        row++;
        filterPanel.add(new Label(LocalizationManager.getString("filter.x") + ":"), 0, row);
        filterPanel.add(minXFilterField, 1, row);
        filterPanel.add(new Label("-"), 2, row);
        filterPanel.add(maxXFilterField, 3, row);

        row++;
        filterPanel.add(new Label(LocalizationManager.getString("filter.y") + ":"), 0, row);
        filterPanel.add(minYFilterField, 1, row);
        filterPanel.add(new Label("-"), 2, row);
        filterPanel.add(maxYFilterField, 3, row);

        row++;
        filterPanel.add(new Label(LocalizationManager.getString("filter.date") + ":"), 0, row);
        filterPanel.add(fromDateFilter, 1, row);
        filterPanel.add(new Label("-"), 2, row);
        filterPanel.add(toDateFilter, 3, row);

        row++;
        filterPanel.add(hasCarFilter, 0, row);
        filterPanel.add(hasToothpickFilter, 1, row);
        filterPanel.add(new Label(LocalizationManager.getString("label.sort_by") + ":"), 2, row);
        filterPanel.add(sortCombo, 3, row);
        filterPanel.add(new Label(LocalizationManager.getString("label.mood") + ":"), 4, row);
        filterPanel.add(moodFilter, 5, row);
        filterPanel.add(new Label(LocalizationManager.getString("label.weapon") + ":"), 6, row);
        filterPanel.add(weaponFilter, 7, row);

        return filterPanel;
    }

    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(10, 0, 0, 0));

        VBox crudGroup = new VBox(5);
        crudGroup.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 5;");
        Label crudLabel = new Label(LocalizationManager.getString("group.crud"));
        crudLabel.setStyle("-fx-font-weight: bold;");
        crudGroup.getChildren().add(crudLabel);
        crudGroup.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn);

        VBox actionGroup = new VBox(5);
        actionGroup.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 5;");
        Label actionLabel = new Label(LocalizationManager.getString("group.actions"));
        actionLabel.setStyle("-fx-font-weight: bold;");
        actionGroup.getChildren().add(actionLabel);
        actionGroup.getChildren().addAll(attackBtn, infoBtn, clearBtn, helpBtn, scriptBtn);

        VBox specialGroup = new VBox(5);
        specialGroup.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 5;");
        Label specialLabel = new Label(LocalizationManager.getString("group.special"));
        specialLabel.setStyle("-fx-font-weight: bold;");
        specialGroup.getChildren().add(specialLabel);
        specialGroup.getChildren().addAll(removeFirstBtn, minByIdBtn, addIfMinBtn, addIfMaxBtn);

        Stream.of(crudGroup, actionGroup, specialGroup)
                .flatMap(group -> group.getChildren().stream())
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .forEach(btn -> {
                    btn.setMaxWidth(Double.MAX_VALUE);
                    btn.setPrefWidth(120);
                });

        buttonPanel.getChildren().addAll(crudGroup, actionGroup, specialGroup);
        return buttonPanel;
    }

    private void createButtons() {
        addBtn = createStyledButton("add", e -> openEditDialog(null, false, false));
        editBtn = createStyledButton("edit", e -> editSelectedFromTable());
        deleteBtn = createStyledButton("delete", e -> deleteSelectedFromTable());
        refreshBtn = createStyledButton("refresh", e -> loadData());
        attackBtn = createStyledButton("attack", e -> attackFromTable());
        infoBtn = createStyledButton("info", e -> runCommand(CommandType.INFO, null, false));
        clearBtn = createStyledButton("clear", e -> confirmAndClear());
        helpBtn = createStyledButton("help", e -> runCommand(CommandType.HELP, null, false));
        scriptBtn = createStyledButton("execute_script", e -> executeScript());
        removeFirstBtn = createStyledButton("remove_first", e -> runCommand(CommandType.REMOVE_FIRST, null, true));
        minByIdBtn = createStyledButton("min_by_id", e -> runCommand(CommandType.MIN_BY_ID, null, false));
        addIfMinBtn = createStyledButton("add_if_min", e -> openEditDialog(null, true, false));
        addIfMaxBtn = createStyledButton("add_if_max", e -> openEditDialog(null, false, true));
    }

    private Button createStyledButton(String key, EventHandler<ActionEvent> handler) {
        Button button = new Button(LocalizationManager.getString(key));
        button.setWrapText(true);
        button.setPrefWidth(120);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;");
        button.setOnAction(handler);

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;"));

        return button;
    }

    private Button createButton(String key, EventHandler<ActionEvent> handler) {
        Button button = new Button(LocalizationManager.getString(key));
        button.setWrapText(true);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(handler);
        return button;
    }

    private StringConverter<Mood> createMoodConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Mood mood) {
                return mood == null ? LocalizationManager.getString("mood.all") : LocalizationManager.formatMood(mood);
            }

            @Override
            public Mood fromString(String string) {
                return null;
            }
        };
    }

    private TableView<HumanBeing> createTableView() {
        TableView<HumanBeing> table = new TableView<>();
        tableColumns.clear();

        tableColumns.add(createColumn("col.id", h -> String.valueOf(h.getId())));
        tableColumns.add(createColumn("col.name", HumanBeing::getName));
        tableColumns.add(createColumn("col.x", h -> LocalizationManager.formatNumber(h.getCoordinates().getX())));
        tableColumns.add(createColumn("col.y", h -> LocalizationManager.formatNumber(h.getCoordinates().getY())));
        tableColumns.add(createColumn("col.real_hero", h -> LocalizationManager.formatBoolean(h.getRealHero())));
        tableColumns.add(createColumn("col.has_toothpick", h -> LocalizationManager.formatBoolean(h.getHasToothpick())));
        tableColumns.add(createColumn("col.impact_speed", h -> LocalizationManager.formatNumber(h.getImpactSpeed())));
        tableColumns.add(createColumn("col.soundtrack", HumanBeing::getSoundtrackName));
        tableColumns.add(createColumn("col.weapon_type", h -> LocalizationManager.formatWeaponType(h.getWeaponType())));
        tableColumns.add(createColumn("col.mood", h -> LocalizationManager.formatMood(h.getMood())));
        tableColumns.add(createColumn("col.car_cool", h -> LocalizationManager.formatBoolean(h.getCar().getCool())));
        tableColumns.add(createColumn("col.creation_date", h -> LocalizationManager.formatDateTime(h.getCreationDate())));
        tableColumns.add(createColumn("col.owner", HumanBeing::getOwner));

        for (TableColumn<HumanBeing, ?> col : tableColumns) {
            col.setSortable(false);
            col.setReorderable(false);
        }

        table.getColumns().addAll(tableColumns);

        table.setRowFactory(tv -> {
            TableRow<HumanBeing> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    HumanBeing selected = row.getItem();
                    if (selected.getOwner().equals(currentUser.getUsername())) {
                        openEditDialog(selected, false, false);
                    } else {
                        showAlert(LocalizationManager.getString("error.auth"));
                    }
                }
            });
            return row;
        });

        return table;
    }

    private TableColumn<HumanBeing, String> createColumn(String key,
                                                         java.util.function.Function<HumanBeing, String> extractor) {
        TableColumn<HumanBeing, String> col = new TableColumn<>(LocalizationManager.getString(key));
        col.setCellValueFactory(cell -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cell.getValue() == null ? "" : extractor.apply(cell.getValue())));
        col.setUserData(key);
        return col;
    }

    private void applyFiltersAndSort() {
        String nameText = nameFilterField.getText() == null ? "" : nameFilterField.getText().trim().toLowerCase();
        String ownerText = ownerFilterField.getText() == null ? "" : ownerFilterField.getText().trim().toLowerCase();

        Double minX = parseDoubleSafe(minXFilterField.getText());
        Double maxX = parseDoubleSafe(maxXFilterField.getText());
        Double minY = parseDoubleSafe(minYFilterField.getText());
        Double maxY = parseDoubleSafe(maxYFilterField.getText());

        LocalDateTime fromDate = fromDateFilter.getValue() != null ? fromDateFilter.getValue().atStartOfDay() : null;
        LocalDateTime toDate = toDateFilter.getValue() != null ? toDateFilter.getValue().plusDays(1).atStartOfDay() : null;

        Boolean hasCar = hasCarFilter.isSelected() ? true : null;
        Boolean hasToothpick = hasToothpickFilter.isSelected() ? true : null;

        Mood mood = moodFilter.getValue();
        WeaponType weapon = weaponFilter.getValue();

        List<HumanBeing> result = allData.stream()
                .filter(h -> nameText.isEmpty() || h.getName().toLowerCase().contains(nameText))
                .filter(h -> ownerText.isEmpty() || h.getOwner().toLowerCase().contains(ownerText))
                .filter(h -> minX == null || h.getCoordinates().getX() >= minX)
                .filter(h -> maxX == null || h.getCoordinates().getX() <= maxX)
                .filter(h -> minY == null || h.getCoordinates().getY() >= minY)
                .filter(h -> maxY == null || h.getCoordinates().getY() <= maxY)
                .filter(h -> fromDate == null || h.getCreationDate().isAfter(fromDate) || h.getCreationDate().isEqual(fromDate))
                .filter(h -> toDate == null || h.getCreationDate().isBefore(toDate))
                .filter(h -> hasCar == null || h.getCar().getCool() == hasCar)
                .filter(h -> hasToothpick == null || h.getHasToothpick() == hasToothpick)
                .filter(h -> mood == null || h.getMood() == mood)
                .filter(h -> weapon == null || h.getWeaponType() == weapon)
                .sorted(getComparator())
                .collect(Collectors.toList());

        displayedData.setAll(result);
    }

    private Double parseDoubleSafe(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Comparator<HumanBeing> getComparator() {
        switch (sortIndex) {
            case 1:
                return Comparator.comparing(HumanBeing::getName, String.CASE_INSENSITIVE_ORDER);
            case 2:
                return Comparator.comparing(HumanBeing::getImpactSpeed);
            case 3:
                return Comparator.comparing(HumanBeing::getOwner, String.CASE_INSENSITIVE_ORDER);
            default:
                return Comparator.comparing(HumanBeing::getId, Comparator.nullsLast(Long::compareTo));
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        new Thread(() -> {
            try {
                Request request = new Request(CommandType.SHOW, null, currentUser);
                client.sendRequest(request);
                Response response = client.receiveResponse();

                if (response.isSuccess() && response.getData() != null) {
                    List<HumanBeing> list = (List<HumanBeing>) response.getData();
                    Platform.runLater(() -> {
                        allData.setAll(list);
                        applyFiltersAndSort();
                        arenaCanvas.redraw();
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(
                        LocalizationManager.getString("error.connection") + ": " + e.getMessage()));
            }
        }).start();
    }

    private void openEditDialog(HumanBeing human, boolean addIfMin, boolean addIfMax) {
        EditController editController = new EditController(client, currentUser, human, addIfMin, addIfMax);
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

    private void editSelectedFromTable() {
        HumanBeing selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(LocalizationManager.getString("error.select_own"));
            return;
        }
        if (!selected.getOwner().equals(currentUser.getUsername())) {
            showAlert(LocalizationManager.getString("error.auth"));
            return;
        }
        openEditDialog(selected, false, false);
    }

    private void deleteSelectedFromTable() {
        HumanBeing selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(LocalizationManager.getString("error.select_own"));
            return;
        }
        if (!selected.getOwner().equals(currentUser.getUsername())) {
            showAlert(LocalizationManager.getString("error.auth"));
            return;
        }
        deleteObject(selected.getId());
    }

    private void attackFromTable() {
        HumanBeing selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getOwner().equals(currentUser.getUsername())) {
            showAlert(LocalizationManager.getString("error.select_enemy"));
            return;
        }
        attack(selected);
    }

    private void handleCanvasObjectClick(HumanBeing human) {
        tableView.getSelectionModel().select(human);
        tableView.scrollTo(human);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LocalizationManager.getString("info.title"));
        alert.setHeaderText(null);
        alert.setContentText(buildObjectInfo(human));

        if (human.getOwner().equals(currentUser.getUsername())) {
            ButtonType editType = new ButtonType(LocalizationManager.getString("dialog.edit"));
            ButtonType deleteType = new ButtonType(LocalizationManager.getString("dialog.delete"));
            ButtonType closeType = new ButtonType(LocalizationManager.getString("dialog.close"),
                    ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(editType, deleteType, closeType);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == editType) {
                    openEditDialog(human, false, false);
                } else if (result.get() == deleteType) {
                    deleteObject(human.getId());
                }
            }
        } else {
            alert.showAndWait();
        }
    }

    private String buildObjectInfo(HumanBeing human) {
        return LocalizationManager.getString("info.id") + ": " + human.getId() + "\n"
                + LocalizationManager.getString("info.name") + ": " + human.getName() + "\n"
                + LocalizationManager.getString("info.coordinates") + ": ("
                + LocalizationManager.formatNumber(human.getCoordinates().getX()) + ", "
                + LocalizationManager.formatNumber(human.getCoordinates().getY()) + ")\n"
                + LocalizationManager.getString("info.impact_speed") + ": "
                + LocalizationManager.formatNumber(human.getImpactSpeed()) + "\n"
                + LocalizationManager.getString("info.weapon") + ": "
                + LocalizationManager.formatWeaponType(human.getWeaponType()) + "\n"
                + LocalizationManager.getString("info.mood") + ": "
                + LocalizationManager.formatMood(human.getMood()) + "\n"
                + LocalizationManager.getString("info.real_hero") + ": "
                + LocalizationManager.formatBoolean(human.getRealHero()) + "\n"
                + LocalizationManager.getString("info.has_toothpick") + ": "
                + LocalizationManager.formatBoolean(human.getHasToothpick()) + "\n"
                + LocalizationManager.getString("info.soundtrack") + ": " + human.getSoundtrackName() + "\n"
                + LocalizationManager.getString("info.car_cool") + ": "
                + LocalizationManager.formatBoolean(human.getCar().getCool()) + "\n"
                + LocalizationManager.getString("info.creation_date") + ": "
                + LocalizationManager.formatDateTime(human.getCreationDate()) + "\n"
                + LocalizationManager.getString("info.owner") + ": " + human.getOwner();
    }

    private void deleteObject(long id) {
        HumanBeing toDelete = allData.stream()
                .filter(h -> h.getId() == id)
                .findFirst()
                .orElse(null);

        if (toDelete == null) {
            return;
        }

        AnimationHelper.animateFadeOut(arenaCanvas, () -> new Thread(() -> {
            try {
                Request request = new Request(CommandType.REMOVE_BY_ID, new Object[]{id}, currentUser);
                client.sendRequest(request);
                Response response = client.receiveResponse();
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        loadData();
                    } else {
                        showAlert(response.getMessage());
                        arenaCanvas.redraw();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(LocalizationManager.getString("error.connection") + ": " + e.getMessage());
                    arenaCanvas.redraw();
                });
            }
        }).start());
    }

    private void confirmAndClear() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(LocalizationManager.getString("confirm.clear.title"));
        confirm.setHeaderText(null);
        confirm.setContentText(LocalizationManager.getString("confirm.clear"));
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                runCommand(CommandType.CLEAR, null, true);
            }
        });
    }

    private void runCommand(CommandType type, Object[] args, boolean reloadOnSuccess) {
        new Thread(() -> {
            try {
                Request request = new Request(type, args, currentUser);
                client.sendRequest(request);
                Response response = client.receiveResponse();
                Platform.runLater(() -> {
                    showAlert(response.getMessage());
                    if (response.isSuccess() && reloadOnSuccess) {
                        loadData();
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert(
                        LocalizationManager.getString("error.connection") + ": " + ex.getMessage()));
            }
        }).start();
    }

    private void executeScript() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(LocalizationManager.getString("select_script"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        new Thread(() -> {
            ScriptExecutor executor = new ScriptExecutor(client);
            executor.execute(file.getAbsolutePath());
            Platform.runLater(() -> {
                showAlert(LocalizationManager.getString("status.script_done"));
                loadData();
            });
        }).start();
    }

    private void startAutoRefresh() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "auto-refresh");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::loadData, 2, 2, TimeUnit.SECONDS);
    }

    private String buildWelcomeText() {
        return LocalizationManager.getString("welcome") + ", " + currentUser.getUsername();
    }

    private void refreshSortComboItems() {
        sortCombo.getItems().setAll(
                LocalizationManager.getString("sort.id"),
                LocalizationManager.getString("sort.name"),
                LocalizationManager.getString("sort.impact_speed"),
                LocalizationManager.getString("sort.owner")
        );
        if (sortIndex >= 0 && sortIndex < sortCombo.getItems().size()) {
            sortCombo.getSelectionModel().select(sortIndex);
        } else {
            sortCombo.getSelectionModel().select(0);
            sortIndex = 0;
        }
    }

    private void refreshLocalizedTexts() {
        welcomeLabel.setText(buildWelcomeText());
        tableLabel.setText(LocalizationManager.getString("label.table"));
        arenaLabel.setText(LocalizationManager.getString("label.arena"));
        sortLabel.setText(LocalizationManager.getString("label.sort_by"));
        tablePlaceholder.setText(LocalizationManager.getString("table.empty"));
        stage.setTitle(LocalizationManager.getString("app.title"));

        nameFilterField.setPromptText(LocalizationManager.getString("filter.name"));
        ownerFilterField.setPromptText(LocalizationManager.getString("filter.owner"));
        minXFilterField.setPromptText(LocalizationManager.getString("filter.min_x"));
        maxXFilterField.setPromptText(LocalizationManager.getString("filter.max_x"));
        minYFilterField.setPromptText(LocalizationManager.getString("filter.min_y"));
        maxYFilterField.setPromptText(LocalizationManager.getString("filter.max_y"));
        fromDateFilter.setPromptText(LocalizationManager.getString("filter.from_date"));
        toDateFilter.setPromptText(LocalizationManager.getString("filter.to_date"));
        hasCarFilter.setText(LocalizationManager.getString("filter.has_car"));
        hasToothpickFilter.setText(LocalizationManager.getString("filter.has_toothpick"));

        moodFilter.setConverter(createMoodConverter());
        moodFilter.setPromptText(LocalizationManager.getString("mood.all"));

        addBtn.setText(LocalizationManager.getString("add"));
        editBtn.setText(LocalizationManager.getString("edit"));
        deleteBtn.setText(LocalizationManager.getString("delete"));
        refreshBtn.setText(LocalizationManager.getString("refresh"));
        attackBtn.setText(LocalizationManager.getString("attack"));
        infoBtn.setText(LocalizationManager.getString("info"));
        clearBtn.setText(LocalizationManager.getString("clear"));
        helpBtn.setText(LocalizationManager.getString("help"));
        scriptBtn.setText(LocalizationManager.getString("execute_script"));
        removeFirstBtn.setText(LocalizationManager.getString("remove_first"));
        minByIdBtn.setText(LocalizationManager.getString("min_by_id"));
        addIfMinBtn.setText(LocalizationManager.getString("add_if_min"));
        addIfMaxBtn.setText(LocalizationManager.getString("add_if_max"));

        refreshSortComboItems();

        for (TableColumn<HumanBeing, ?> col : tableColumns) {
            if (col.getUserData() instanceof String) {
                col.setText(LocalizationManager.getString((String) col.getUserData()));
            }
        }

        tableView.refresh();
        applyFiltersAndSort();
        arenaCanvas.redraw();
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
            scheduler.shutdownNow();
        }
        client.disconnect();
    }

    private void attack(HumanBeing defender) {
        HumanBeing attacker = allData.stream()
                .filter(h -> h.getOwner().equals(currentUser.getUsername()))
                .findFirst()
                .orElse(null);

        if (attacker == null) {
            showAlert(LocalizationManager.getString("error.no_attacker"));
            return;
        }

        final long attackerId = attacker.getId();
        final long defenderId = defender.getId();

        AnimationHelper.animateHit(arenaCanvas,
                defender.getCoordinates().getX() * 50,
                defender.getCoordinates().getY() * 50,
                null);

        new Thread(() -> {
            try {
                Request request = new Request(CommandType.ATTACK,
                        new Object[]{attackerId, defenderId}, currentUser);
                client.sendRequest(request);
                Response response = client.receiveResponse();

                Platform.runLater(() -> {
                    showAlert(response.getMessage());

                    if (response.isSuccess()) {
                        loadData();
                        arenaCanvas.redraw();
                    }
                });

            } catch (Exception ex) {
                Platform.runLater(() -> showAlert(
                        LocalizationManager.getString("error.connection") + ": " + ex.getMessage()));
            }
        }).start();
    }
}