package org.example.mealsweek.ui.Dish;

import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.example.mealsweek.dto.DishDto;
import org.example.mealsweek.dto.IngredientDto;
import org.example.mealsweek.dto.MeasurementUnitDto;
import org.example.mealsweek.dto.filter.IngredientFilter;
import org.example.mealsweek.service.DishService;
import org.example.mealsweek.service.IngredientService;
import org.example.mealsweek.service.MeasurementUnitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DishesView extends BorderPane {

    private final DishService dishService;
    private final IngredientService ingredientService;
    private final MeasurementUnitService measurementUnitService;
    private final Runnable onBack;

    private final TableView<DishDto> dishesTable = new TableView<>();
    private final VBox ingredientRowsBox = new VBox(10);

    private final TextField nameField = new TextField();
    private final TextArea descriptionArea = new TextArea();

    private final Label formTitle = new Label("Новое блюдо");
    private final Label statusLabel = new Label("");

    private List<MeasurementUnitDto> measurementUnits = new ArrayList<>();
    private DishDto selectedDish;

    public DishesView(DishService dishService,
                      IngredientService ingredientService,
                      MeasurementUnitService measurementUnitService,
                      Runnable onBack) {
        this.dishService = dishService;
        this.ingredientService = ingredientService;
        this.measurementUnitService = measurementUnitService;
        this.onBack = onBack;

        setStyle("-fx-background-color: linear-gradient(to bottom, #f7f8fb, #eef2f7);");
        setPadding(new Insets(18));

        setTop(buildHeader());
        setCenter(buildContent());

        configureDishesTable();
        loadMeasurementUnits();
        configureForm();
        reloadDishes();
    }

    private Node buildHeader() {
        var title = new Label("Блюда");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        var backBtn = new Button("← Назад");
        backBtn.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });
        backBtn.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 14;
                -fx-border-radius: 14;
                -fx-border-color: rgba(17,24,39,0.18);
                -fx-border-width: 1;
                -fx-font-weight: 800;
                -fx-font-size: 16px;
                -fx-cursor: hand;
                -fx-padding: 10 14 10 14;
                """);

        var newBtn = new Button("+ Новое блюдо");
        stylePrimary(newBtn);
        newBtn.setOnAction(e -> clearForm());

        var saveBtn = new Button("Сохранить");
        stylePrimary(saveBtn);
        saveBtn.setOnAction(e -> saveDish());

        var deleteBtn = new Button("🗑 Удалить");
        styleDanger(deleteBtn);
        deleteBtn.setOnAction(e -> deleteDish());

        var left = new HBox(12, backBtn, title);
        left.setAlignment(Pos.CENTER_LEFT);

        var right = new HBox(10, newBtn, saveBtn, deleteBtn);
        right.setAlignment(Pos.CENTER_RIGHT);

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        var header = new HBox(left, spacer, right);
        header.setPadding(new Insets(4, 4, 10, 4));
        header.setAlignment(Pos.CENTER_LEFT);

        return header;
    }

    private Node buildContent() {
        Node leftCard = buildLeftCard();
        Node rightCard = buildRightCard();

        HBox root = new HBox(18, leftCard, rightCard);
        HBox.setHgrow(rightCard, Priority.ALWAYS);
        return root;
    }

    private Node buildLeftCard() {
        var card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setPrefWidth(380);
        card.setMinWidth(320);
        card.setStyle(cardStyle());
        card.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, 0.10)));

        var title = new Label("Список блюд");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        dishesTable.setPlaceholder(new Label("Блюда пока не добавлены"));
        dishesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        dishesTable.setStyle("-fx-font-size: 16px;");
        VBox.setVgrow(dishesTable, Priority.ALWAYS);

        card.getChildren().addAll(title, dishesTable);
        return card;
    }

    private Node buildRightCard() {
        var card = new VBox(14);
        card.setPadding(new Insets(18));
        card.setStyle(cardStyle());
        card.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, 0.10)));
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setFillWidth(true);

        formTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        nameField.setPromptText("Название блюда");
        nameField.setEditable(true);
        nameField.setFocusTraversable(true);
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setStyle("-fx-font-size: 16px; -fx-padding: 10 12 10 12;");

        descriptionArea.setPromptText("Описание");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(true);
        descriptionArea.setFocusTraversable(true);
        descriptionArea.setMaxWidth(Double.MAX_VALUE);
        descriptionArea.setStyle("-fx-font-size: 16px;");

        var ingredientsTitle = new Label("Ингредиенты");
        ingredientsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        var addIngredientBtn = new Button("+ Добавить ингредиент");
        styleActionButton(addIngredientBtn, false);
        addIngredientBtn.setOnAction(e -> ingredientRowsBox.getChildren().add(new IngredientRow()));

        var ingredientsHeader = new HBox(12, ingredientsTitle, addIngredientBtn);
        ingredientsHeader.setAlignment(Pos.CENTER_LEFT);

        ingredientRowsBox.setFillWidth(true);

        ScrollPane ingredientsScroll = new ScrollPane(ingredientRowsBox);
        ingredientsScroll.setFitToWidth(true);
        ingredientsScroll.setPrefHeight(420);
        ingredientsScroll.setMaxWidth(Double.MAX_VALUE);
        ingredientsScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(ingredientsScroll, Priority.ALWAYS);

        statusLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 15px; -fx-font-weight: 700;");

        card.getChildren().addAll(
                formTitle,
                labeledField("Название", nameField),
                labeledField("Описание", descriptionArea),
                ingredientsHeader,
                ingredientsScroll,
                statusLabel
        );

        return card;
    }

    private Node labeledField(String labelText, Node field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #374151; -fx-font-weight: 800; -fx-font-size: 15px;");

        VBox box = new VBox(6, label, field);
        box.setFillWidth(true);

        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        return box;
    }

    private void configureDishesTable() {
        TableColumn<DishDto, Number> idCol = new TableColumn<>("ID");
        idCol.setMinWidth(80);
        idCol.setCellValueFactory(cd -> new SimpleLongProperty(cd.getValue().id() == null ? 0 : cd.getValue().id()));

        TableColumn<DishDto, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().name()));

        dishesTable.getColumns().addAll(idCol, nameCol);

        dishesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) loadDishToForm(newV);
        });

        dishesTable.setRowFactory(tv -> {
            TableRow<DishDto> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    loadDishToForm(row.getItem());
                }
            });
            return row;
        });
    }

    private void configureForm() {
        clearForm();
    }

    private void loadMeasurementUnits() {
        try {
            measurementUnits = measurementUnitService.getAll();
        } catch (Exception ex) {
            measurementUnits = new ArrayList<>();
            showError("Не удалось загрузить единицы измерения", ex);
        }
    }

    private void reloadDishes() {
        setLoading(true);

        new Thread(() -> {
            try {
                List<DishDto> dishes = dishService.getAll();

                Platform.runLater(() -> {
                    dishesTable.setItems(FXCollections.observableArrayList(dishes));
                    setLoading(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Не удалось загрузить блюда", ex);
                });
            }
        }, "dishes-loader").start();
    }

    private void loadDishToForm(DishDto dish) {
        selectedDish = dish;
        formTitle.setText("Редактирование блюда #" + dish.id());

        nameField.setText(dish.name() != null ? dish.name() : "");
        descriptionArea.setText(dish.description() != null ? dish.description() : "");

        ingredientRowsBox.getChildren().clear();

        if (dish.ingredients() != null) {
            for (DishDto.IngredientLineDto line : dish.ingredients()) {
                ingredientRowsBox.getChildren().add(new IngredientRow(line));
            }
        }

        if (ingredientRowsBox.getChildren().isEmpty()) {
            ingredientRowsBox.getChildren().add(new IngredientRow());
        }

        statusLabel.setText("");
    }

    private void clearForm() {
        selectedDish = null;
        formTitle.setText("Новое блюдо");
        nameField.clear();
        descriptionArea.clear();
        ingredientRowsBox.getChildren().clear();
        ingredientRowsBox.getChildren().add(new IngredientRow());
        statusLabel.setText("");
        dishesTable.getSelectionModel().clearSelection();
    }

    private void saveDish() {
        try {
            List<DishDto.IngredientLineDto> ingredients = collectIngredientLines();

            DishDto dto = new DishDto(
                    selectedDish != null ? selectedDish.id() : null,
                    nameField.getText() != null ? nameField.getText().trim() : null,
                    descriptionArea.getText() != null ? descriptionArea.getText().trim() : null,
                    ingredients
            );

            if (selectedDish == null) {
                dishService.create(dto);
                showInfo("Блюдо добавлено");
            } else {
                dishService.update(selectedDish.id(), dto);
                showInfo("Блюдо обновлено");
            }

            reloadDishes();
            clearForm();
        } catch (Exception ex) {
            showError("Не удалось сохранить блюдо", ex);
        }
    }

    private List<DishDto.IngredientLineDto> collectIngredientLines() {
        List<DishDto.IngredientLineDto> result = new ArrayList<>();

        for (Node node : ingredientRowsBox.getChildren()) {
            if (node instanceof IngredientRow row) {
                if (row.isEmpty()) {
                    continue;
                }
                result.add(row.toDto());
            }
        }

        return result;
    }

    private void deleteDish() {
        DishDto selected = dishesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Выбери блюдо в таблице.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление");
        confirm.setHeaderText("Удалить блюдо?");
        confirm.setContentText("Будет удалено блюдо и его состав.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) {
            return;
        }

        try {
            dishService.delete(selected.id());
            clearForm();
            reloadDishes();
        } catch (Exception ex) {
            showError("Не удалось удалить блюдо", ex);
        }
    }

    private void setLoading(boolean loading) {
        dishesTable.setDisable(loading);
        statusLabel.setText(loading ? "Загрузка..." : "");
    }

    private class IngredientRow extends VBox {

        private Long selectedIngredientId;

        private final TextField ingredientField = new TextField();
        private final TextField amountField = new TextField();
        private final ComboBox<MeasurementUnitDto> unitComboBox = new ComboBox<>();
        private final TextField noteField = new TextField();

        IngredientRow() {
            build(null);
        }

        IngredientRow(DishDto.IngredientLineDto dto) {
            build(dto);
        }

        private void build(DishDto.IngredientLineDto dto) {
            setPadding(new Insets(12));
            setSpacing(10);
            setFillWidth(true);
            setMaxWidth(Double.MAX_VALUE);
            setStyle("""
                    -fx-background-color: #f9fafb;
                    -fx-background-radius: 16;
                    -fx-border-radius: 16;
                    -fx-border-color: rgba(17,24,39,0.08);
                    -fx-border-width: 1;
                    """);

            ingredientField.setPromptText("Ингредиент");
            ingredientField.setEditable(true);
            ingredientField.setStyle("-fx-font-size: 15px; -fx-padding: 9 12 9 12;");
            ingredientField.setPrefWidth(260);
            ingredientField.setMaxWidth(Double.MAX_VALUE);

            amountField.setPromptText("Количество");
            amountField.setStyle("-fx-font-size: 15px; -fx-padding: 9 12 9 12;");
            amountField.setPrefWidth(120);

            unitComboBox.setPromptText("Единица");
            unitComboBox.setItems(FXCollections.observableArrayList(measurementUnits));
            unitComboBox.setPrefWidth(160);
            unitComboBox.setMaxWidth(Double.MAX_VALUE);
            unitComboBox.setStyle("-fx-font-size: 15px;");

            unitComboBox.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(MeasurementUnitDto item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.name());
                }
            });

            unitComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(MeasurementUnitDto item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.name());
                }
            });

            noteField.setPromptText("Комментарий");
            noteField.setStyle("-fx-font-size: 15px; -fx-padding: 9 12 9 12;");
            noteField.setMaxWidth(Double.MAX_VALUE);

            Button searchBtn = new Button("Выбрать");
            styleActionButton(searchBtn, false);
            searchBtn.setOnAction(e -> openIngredientSearch());

            Button clearBtn = new Button("Сброс");
            styleActionButton(clearBtn, true);
            clearBtn.setOnAction(e -> {
                selectedIngredientId = null;
                ingredientField.clear();
            });

            Button removeBtn = new Button("Удалить");
            styleDanger(removeBtn);
            removeBtn.setOnAction(e -> ingredientRowsBox.getChildren().remove(this));

            HBox row1 = new HBox(10, ingredientField, searchBtn, clearBtn, removeBtn);
            row1.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(ingredientField, Priority.ALWAYS);

            HBox row2 = new HBox(10, amountField, unitComboBox, noteField);
            row2.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(unitComboBox, Priority.ALWAYS);
            HBox.setHgrow(noteField, Priority.ALWAYS);

            getChildren().addAll(row1, row2);

            if (dto != null) {
                selectedIngredientId = dto.ingredientId();
                ingredientField.setText(dto.ingredientName());
                amountField.setText(dto.amount() != null ? dto.amount().toPlainString() : "");
                noteField.setText(dto.note() != null ? dto.note() : "");

                if (dto.measurementUnitId() != null) {
                    unitComboBox.getSelectionModel().select(
                            measurementUnits.stream()
                                    .filter(it -> dto.measurementUnitId().equals(it.id()))
                                    .findFirst()
                                    .orElse(null)
                    );
                }
            }
        }

        private void openIngredientSearch() {
            IngredientPickerDialog dialog = new IngredientPickerDialog(ingredientService);
            Optional<IngredientDto> result = dialog.showAndWait();
            result.ifPresent(ingredient -> {
                selectedIngredientId = ingredient.id();
                ingredientField.setText(ingredient.name());
            });
        }

        boolean isEmpty() {
            return ingredientField.getText() == null || ingredientField.getText().trim().isBlank();
        }

        DishDto.IngredientLineDto toDto() {
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountField.getText().trim().replace(",", "."));
            } catch (Exception ex) {
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Некорректное количество у ингредиента: " + ingredientField.getText()
                );
            }

            MeasurementUnitDto selectedUnit = unitComboBox.getValue();
            if (selectedUnit == null) {
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Выберите единицу измерения для ингредиента: " + ingredientField.getText()
                );
            }

            String ingredientName = ingredientField.getText() != null ? ingredientField.getText().trim() : null;

            return new DishDto.IngredientLineDto(
                    selectedIngredientId,
                    ingredientName,
                    amount,
                    selectedUnit.id(),
                    selectedUnit.name(),
                    noteField.getText() != null ? noteField.getText().trim() : null
            );
        }
    }

    private static class IngredientPickerDialog extends Dialog<IngredientDto> {

        private final IngredientService ingredientService;

        private final TextField searchField = new TextField();
        private final TableView<IngredientDto> table = new TableView<>();

        IngredientPickerDialog(IngredientService ingredientService) {
            this.ingredientService = ingredientService;

            setTitle("Выбор ингредиента");
            setHeaderText("Выбери существующий ингредиент");

            ButtonType selectBtnType = new ButtonType("Выбрать", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(selectBtnType, ButtonType.CANCEL);

            Node selectBtn = getDialogPane().lookupButton(selectBtnType);
            selectBtn.setDisable(true);

            searchField.setPromptText("Поиск по названию...");
            searchField.setStyle("-fx-font-size: 16px; -fx-padding: 10 12 10 12;");

            Button findBtn = new Button("Искать");
            findBtn.setStyle("""
                    -fx-background-color: white;
                    -fx-text-fill: #111827;
                    -fx-background-radius: 14;
                    -fx-border-radius: 14;
                    -fx-border-color: rgba(17,24,39,0.18);
                    -fx-border-width: 1;
                    -fx-font-weight: 900;
                    -fx-font-size: 16px;
                    -fx-cursor: hand;
                    -fx-padding: 10 14 10 14;
                    """);

            TableColumn<IngredientDto, Number> idCol = new TableColumn<>("ID");
            idCol.setMinWidth(80);
            idCol.setCellValueFactory(cd -> new SimpleLongProperty(cd.getValue().id()));

            TableColumn<IngredientDto, String> nameCol = new TableColumn<>("Название");
            nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().name()));

            table.getColumns().addAll(idCol, nameCol);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setPlaceholder(new Label("Ничего не найдено"));
            table.setPrefHeight(420);
            table.setStyle("-fx-font-size: 16px;");

            table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                selectBtn.setDisable(n == null);
            });

            table.setRowFactory(tv -> {
                TableRow<IngredientDto> row = new TableRow<>();
                row.setOnMouseClicked(ev -> {
                    if (ev.getClickCount() == 2 && !row.isEmpty()) {
                        setResult(row.getItem());
                        close();
                    }
                });
                return row;
            });

            findBtn.setOnAction(e -> reloadTable());
            searchField.setOnAction(e -> reloadTable());

            VBox root = new VBox(10,
                    new HBox(10, searchField, findBtn),
                    table
            );
            root.setPadding(new Insets(10));
            root.setPrefSize(700, 520);
            VBox.setVgrow(table, Priority.ALWAYS);
            HBox.setHgrow(searchField, Priority.ALWAYS);

            getDialogPane().setContent(root);

            setResultConverter(bt -> bt == selectBtnType ? table.getSelectionModel().getSelectedItem() : null);

            reloadTable();
        }

        private void reloadTable() {
            try {
                IngredientFilter filter = IngredientFilter.builder()
                        .name(blankToNull(searchField.getText()))
                        .page(0)
                        .size(50)
                        .sort("name")
                        .direction(Sort.Direction.ASC)
                        .build();

                Page<IngredientDto> page = ingredientService.getAllByFilter(filter);
                table.setItems(FXCollections.observableArrayList(page.getContent()));
            } catch (Exception ex) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Ошибка");
                a.setHeaderText("Не удалось загрузить ингредиенты");
                a.setContentText(ex.getMessage());
                a.showAndWait();
            }
        }

        private static String blankToNull(String s) {
            if (s == null) return null;
            String t = s.trim();
            return t.isBlank() ? null : t;
        }
    }

    private void showInfo(String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Информация");
        a.setHeaderText(null);
        a.setContentText(text);
        a.showAndWait();
    }

    private void showError(String title, Exception ex) {
        String message = extractMessage(ex);

        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Ошибка");
        a.setHeaderText(title);
        a.setContentText(message);
        a.showAndWait();
    }

    private String extractMessage(Exception ex) {
        if (ex instanceof ResponseStatusException rse) {
            return rse.getReason() != null ? rse.getReason() : "Ошибка запроса";
        }

        Throwable c = ex.getCause();
        if (c instanceof ResponseStatusException rse) {
            return rse.getReason() != null ? rse.getReason() : "Ошибка запроса";
        }

        return ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
    }

    private String cardStyle() {
        return """
                -fx-background-color: white;
                -fx-background-radius: 22;
                -fx-border-radius: 22;
                -fx-border-color: rgba(0,0,0,0.08);
                -fx-border-width: 1;
                """;
    }

    private void stylePrimary(Button b) {
        b.setStyle("""
                -fx-background-color: #111827;
                -fx-text-fill: white;
                -fx-background-radius: 14;
                -fx-font-weight: 900;
                -fx-font-size: 16px;
                -fx-cursor: hand;
                -fx-padding: 10 14 10 14;
                """);
    }

    private void styleDanger(Button b) {
        b.setStyle("""
                -fx-background-color: #ef4444;
                -fx-text-fill: white;
                -fx-background-radius: 14;
                -fx-font-weight: 900;
                -fx-font-size: 16px;
                -fx-cursor: hand;
                -fx-padding: 10 14 10 14;
                """);
    }

    private void styleActionButton(Button b, boolean subtle) {
        b.setStyle(subtle ? """
                -fx-background-color: #f3f4f6;
                -fx-text-fill: #111827;
                -fx-background-radius: 14;
                -fx-border-radius: 14;
                -fx-border-color: rgba(17,24,39,0.12);
                -fx-border-width: 1;
                -fx-font-weight: 900;
                -fx-font-size: 16px;
                -fx-cursor: hand;
                -fx-padding: 10 14 10 14;
                """ : """
                -fx-background-color: white;
                -fx-text-fill: #111827;
                -fx-background-radius: 14;
                -fx-border-radius: 14;
                -fx-border-color: rgba(17,24,39,0.18);
                -fx-border-width: 1;
                -fx-font-weight: 900;
                -fx-font-size: 16px;
                -fx-cursor: hand;
                -fx-padding: 10 14 10 14;
                """);
    }
}