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
import java.util.Timer;
import java.util.TimerTask;

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
    private Label languageLabel;
    private Label filterLabel;
    private Label crudLabel;
    private Label actionLabel;
    private Label specialLabel;

    private int sortIndex;

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
        tablePlaceholder = new Label();
        tableView.setPlaceholder(tablePlaceholder);

        nameFilterField = new TextField();
        ownerFilterField = new TextField();
        minXFilterField = new TextField();
        maxXFilterField = new TextField();
        minYFilterField = new TextField();
        maxYFilterField = new TextField();
        fromDateFilter = new DatePicker();
        toDateFilter = new DatePicker();

        hasCarFilter = new CheckBox();
        hasToothpickFilter = new CheckBox();

        sortCombo = new ComboBox<>();
        moodFilter = new ComboBox<>();
        weaponFilter = new ComboBox<>();
        langCombo = new ComboBox<>();

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
        setupFiltersAndSort();

        welcomeLabel = new Label();
        tableLabel = new Label();
        arenaLabel = new Label();
        sortLabel = new Label();
        languageLabel = new Label();
        filterLabel = new Label();
        crudLabel = new Label();
        actionLabel = new Label();
        specialLabel = new Label();

        GridPane filterPanel = createFilterPanel();
        HBox buttonPanel = createButtonPanel();
        HBox topPanel = createTopPanel();

        VBox leftPanel = new VBox(8, tableLabel, filterPanel, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        VBox rightPanel = new VBox(8, arenaLabel, arenaCanvas, buttonPanel);
        VBox.setVgrow(arenaCanvas, Priority.ALWAYS);

        HBox mainPanel = new HBox(20, leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        VBox root = new VBox(10, topPanel, mainPanel);
        root.setPadding(new Insets(10));

        tableView.setItems(displayedData);

        Scene scene = new Scene(root, 1400, 800);
        stage.setTitle(LocalizationManager.getString("app.title"));
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> stop());
        stage.show();

        refreshLocalizedTexts();
        loadData();
        startAutoRefresh();
    }

    private HBox createTopPanel() {
        languageLabel = new Label();
        langCombo.getItems().addAll(
                LocalizationManager.LANG_RU,
                LocalizationManager.LANG_EN,
                LocalizationManager.LANG_DE,
                LocalizationManager.LANG_HU,
                LocalizationManager.LANG_ES
        );
        langCombo.setValue(LocalizationManager.getCurrentLanguageName());
        langCombo.setOnAction(e -> {
            LocalizationManager.setLocale(langCombo.getValue());
            refreshLocalizedTexts();
        });

        HBox welcomePanel = new HBox(10, welcomeLabel, new Region(), languageLabel, langCombo);
        HBox.setHgrow(welcomePanel.getChildren().get(1), Priority.ALWAYS);

        return welcomePanel;
    }

    private void setupFiltersAndSort() {
        nameFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());
        ownerFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());
        minXFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());
        maxXFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());
        minYFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());
        maxYFilterField.textProperty().addListener((obs, old, val) -> scheduleFilter());
        fromDateFilter.valueProperty().addListener((obs, old, val) -> scheduleFilter());
        toDateFilter.valueProperty().addListener((obs, old, val) -> scheduleFilter());
        hasCarFilter.setOnAction(e -> scheduleFilter());
        hasToothpickFilter.setOnAction(e -> scheduleFilter());

        moodFilter.getItems().add(null);
        moodFilter.getItems().addAll(Mood.values());
        moodFilter.setConverter(createMoodConverter());
        moodFilter.setOnAction(e -> applyFiltersAndSort());

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
        weaponFilter.setOnAction(e -> applyFiltersAndSort());

        sortCombo.setOnAction(e -> {
            sortIndex = sortCombo.getSelectionModel().getSelectedIndex();
            applyFiltersAndSort();
        });
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
        filterPanel.add(filterLabel, 0, row);
        filterPanel.add(new Label(LocalizationManager.getString("filter.name") + ":"), 0, ++row);
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
        filterPanel.add(sortLabel, 2, row);
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
        crudLabel.setStyle("-fx-font-weight: bold;");
        crudGroup.getChildren().add(crudLabel);
        crudGroup.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn);

        VBox actionGroup = new VBox(5);
        actionGroup.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 5;");
        actionLabel.setStyle("-fx-font-weight: bold;");
        actionGroup.getChildren().add(actionLabel);
        actionGroup.getChildren().addAll(attackBtn, infoBtn, clearBtn, helpBtn, scriptBtn);

        VBox specialGroup = new VBox(5);
        specialGroup.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 5;");
        specialLabel.setStyle("-fx-font-weight: bold;");
        specialGroup.getChildren().add(specialLabel);
        specialGroup.getChildren().addAll(removeFirstBtn, minByIdBtn, addIfMinBtn, addIfMaxBtn);

        buttonPanel.getChildren().addAll(crudGroup, actionGroup, specialGroup);
        return buttonPanel;
    }

    private void createButtons() {
        addBtn = new Button();
        editBtn = new Button();
        deleteBtn = new Button();
        refreshBtn = new Button();
        attackBtn = new Button();
        infoBtn = new Button();
        clearBtn = new Button();
        helpBtn = new Button();
        scriptBtn = new Button();
        removeFirstBtn = new Button();
        minByIdBtn = new Button();
        addIfMinBtn = new Button();
        addIfMaxBtn = new Button();

        addBtn.setOnAction(e -> openEditDialog(null, false, false));
        editBtn.setOnAction(e -> editSelectedFromTable());
        deleteBtn.setOnAction(e -> deleteSelectedFromTable());
        refreshBtn.setOnAction(e -> loadData());
        attackBtn.setOnAction(e -> attackFromTable());
        infoBtn.setOnAction(e -> runCommand(CommandType.INFO, null, false));
        clearBtn.setOnAction(e -> confirmAndClear());
        helpBtn.setOnAction(e -> runCommand(CommandType.HELP, null, false));
        scriptBtn.setOnAction(e -> executeScript());
        removeFirstBtn.setOnAction(e -> runCommand(CommandType.REMOVE_FIRST, null, true));
        minByIdBtn.setOnAction(e -> runCommand(CommandType.MIN_BY_ID, null, false));
        addIfMinBtn.setOnAction(e -> openEditDialog(null, true, false));
        addIfMaxBtn.setOnAction(e -> openEditDialog(null, false, true));
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
        return table;
    }

    private TableColumn<HumanBeing, String> createColumn(String key, java.util.function.Function<HumanBeing, String> extractor) {
        TableColumn<HumanBeing, String> col = new TableColumn<>();
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
            case 1: return Comparator.comparing(HumanBeing::getName, String.CASE_INSENSITIVE_ORDER);
            case 2: return Comparator.comparing(HumanBeing::getImpactSpeed);
            case 3: return Comparator.comparing(HumanBeing::getOwner, String.CASE_INSENSITIVE_ORDER);
            default: return Comparator.comparing(HumanBeing::getId, Comparator.nullsLast(Long::compareTo));
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
                Platform.runLater(() -> showAlert(LocalizationManager.getString("error.connection") + ": " + e.getMessage()));
            }
        }).start();
    }

    private void openEditDialog(HumanBeing human, boolean addIfMin, boolean addIfMax) {
        EditController editController = new EditController(client, currentUser, human, addIfMin, addIfMax);
        editController.showAndWait();
        if (editController.isSaved()) {
            loadData();
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
            ButtonType closeType = new ButtonType(LocalizationManager.getString("dialog.close"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(editType, deleteType, closeType);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == editType) openEditDialog(human, false, false);
                else if (result.get() == deleteType) deleteObject(human.getId());
            }
        } else {
            alert.showAndWait();
        }
    }

    private String buildObjectInfo(HumanBeing human) {
        return LocalizationManager.getString("info.id") + ": " + human.getId() + "\n"
                + LocalizationManager.getString("info.name") + ": " + human.getName() + "\n"
                + LocalizationManager.getString("info.coordinates") + ": (" + LocalizationManager.formatNumber(human.getCoordinates().getX()) + ", " + LocalizationManager.formatNumber(human.getCoordinates().getY()) + ")\n"
                + LocalizationManager.getString("info.impact_speed") + ": " + LocalizationManager.formatNumber(human.getImpactSpeed()) + "\n"
                + LocalizationManager.getString("info.weapon") + ": " + LocalizationManager.formatWeaponType(human.getWeaponType()) + "\n"
                + LocalizationManager.getString("info.mood") + ": " + LocalizationManager.formatMood(human.getMood()) + "\n"
                + LocalizationManager.getString("info.real_hero") + ": " + LocalizationManager.formatBoolean(human.getRealHero()) + "\n"
                + LocalizationManager.getString("info.has_toothpick") + ": " + LocalizationManager.formatBoolean(human.getHasToothpick()) + "\n"
                + LocalizationManager.getString("info.soundtrack") + ": " + human.getSoundtrackName() + "\n"
                + LocalizationManager.getString("info.car_cool") + ": " + LocalizationManager.formatBoolean(human.getCar().getCool()) + "\n"
                + LocalizationManager.getString("info.creation_date") + ": " + LocalizationManager.formatDateTime(human.getCreationDate()) + "\n"
                + LocalizationManager.getString("info.owner") + ": " + human.getOwner();
    }

    private void deleteObject(long id) {
        AnimationHelper.animateFadeOut(arenaCanvas, () -> new Thread(() -> {
            try {
                Request request = new Request(CommandType.REMOVE_BY_ID, new Object[]{id}, currentUser);
                client.sendRequest(request);
                Response response = client.receiveResponse();
                Platform.runLater(() -> {
                    if (response.isSuccess()) loadData();
                    else showAlert(response.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(LocalizationManager.getString("error.connection") + ": " + e.getMessage()));
            }
        }).start());
    }

    private void confirmAndClear() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(LocalizationManager.getString("confirm.clear.title"));
        confirm.setHeaderText(null);
        confirm.setContentText(LocalizationManager.getString("confirm.clear"));
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) runCommand(CommandType.CLEAR, null, true);
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
                    if (response.isSuccess() && reloadOnSuccess) loadData();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert(LocalizationManager.getString("error.connection") + ": " + ex.getMessage()));
            }
        }).start();
    }

    private void executeScript() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(LocalizationManager.getString("select_script"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;
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

    private void refreshLocalizedTexts() {
        welcomeLabel.setText(buildWelcomeText());
        tableLabel.setText(LocalizationManager.getString("label.table"));
        arenaLabel.setText(LocalizationManager.getString("label.arena"));
        sortLabel.setText(LocalizationManager.getString("label.sort_by"));
        tablePlaceholder.setText(LocalizationManager.getString("table.empty"));
        stage.setTitle(LocalizationManager.getString("app.title"));
        languageLabel.setText(LocalizationManager.getString("language"));
        filterLabel.setText(LocalizationManager.getString("filter.title"));
        crudLabel.setText(LocalizationManager.getString("group.crud"));
        actionLabel.setText(LocalizationManager.getString("group.actions"));
        specialLabel.setText(LocalizationManager.getString("group.special"));

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

        sortCombo.getItems().setAll(
                LocalizationManager.getString("sort.id"),
                LocalizationManager.getString("sort.name"),
                LocalizationManager.getString("sort.impact_speed"),
                LocalizationManager.getString("sort.owner")
        );
        sortCombo.getSelectionModel().select(sortIndex);

        moodFilter.setConverter(createMoodConverter());
        moodFilter.setPromptText(LocalizationManager.getString("mood.all"));
        weaponFilter.setPromptText(LocalizationManager.getString("weapon.all"));

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
        if (scheduler != null) scheduler.shutdownNow();
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

        new Thread(() -> {
            try {
                Request request = new Request(CommandType.ATTACK, new Object[]{attacker.getId(), defender.getId()}, currentUser);
                client.sendRequest(request);
                Response response = client.receiveResponse();
                Platform.runLater(() -> {
                    showAlert(response.getMessage());
                    if (response.isSuccess()) loadData();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert(LocalizationManager.getString("error.connection") + ": " + ex.getMessage()));
            }
        }).start();
    }
}