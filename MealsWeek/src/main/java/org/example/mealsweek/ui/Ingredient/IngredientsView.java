package org.example.mealsweek.ui.Ingredient;

import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.example.mealsweek.dto.IngredientBulkSaveResult;
import org.example.mealsweek.dto.IngredientDto;
import org.example.mealsweek.dto.filter.IngredientFilter;
import org.example.mealsweek.service.IngredientService;
import org.example.mealsweek.ui.BulkAddIngredientsDialog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

public class IngredientsView extends BorderPane {

    private final IngredientService ingredientService;
    private final Runnable onBack;

    private final TableView<IngredientDto> table = new TableView<>();
    private final TextField nameFilterField = new TextField();
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();
    private final Pagination pagination = new Pagination(1, 0);
    private final Label totalLabel = new Label("Всего: 0");
    private final Label statusLabel = new Label("");

    private IngredientFilter currentFilter = IngredientFilter.builder()
            .name(null)
            .page(0)
            .size(20)
            .sort("id")
            .direction(Sort.Direction.ASC)
            .build();

    public IngredientsView(IngredientService ingredientService, Runnable onBack) {
        this.ingredientService = ingredientService;
        this.onBack = onBack;

        setStyle("-fx-background-color: linear-gradient(to bottom, #f7f8fb, #eef2f7);");
        setPadding(new Insets(18));

        setTop(buildHeader());
        setCenter(buildCardWithTable());
        setBottom(buildFooter());

        configureTable();
        configureFiltersAndPagination();

        reloadPage(0);
    }

    // ---------------- UI ----------------

    private Node buildHeader() {
        var title = new Label("Ингредиенты");
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

        var left = new HBox(12, backBtn, title);
        left.setAlignment(Pos.CENTER_LEFT);

        // Поиск
        nameFilterField.setPromptText("Поиск по названию…");
        nameFilterField.setPrefWidth(320);
        nameFilterField.setStyle("-fx-font-size: 16px; -fx-padding: 10 12 10 12;");

        var searchBtn = new Button("Искать");
        var clearBtn = new Button("Сброс");

        styleActionButton(searchBtn, false);
        styleActionButton(clearBtn, true);

        searchBtn.setOnAction(e -> applyFilters());
        clearBtn.setOnAction(e -> {
            nameFilterField.clear();
            applyFilters();
        });

        // CRUD
        var addBtn = new Button("+ Добавить");
        var addBulkBtn = new Button("+ Добавить несколько");
        var editBtn = new Button("✎ Изменить");
        var delBtn = new Button("🗑 Удалить");

        stylePrimary(addBtn);
        stylePrimary(addBulkBtn);
        styleActionButton(editBtn, false);
        styleDanger(delBtn);

        addBtn.setOnAction(e -> onAdd());
        addBulkBtn.setOnAction(e -> onAddBulk());
        editBtn.setOnAction(e -> onEdit());
        delBtn.setOnAction(e -> onDelete());

        var right = new HBox(10,
                nameFilterField, searchBtn, clearBtn,
                new Separator(),
                addBtn, addBulkBtn, editBtn, delBtn
        );
        right.setAlignment(Pos.CENTER_RIGHT);

        // enter в поле = поиск
        nameFilterField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) applyFilters();
        });

        var header = new HBox();
        header.getChildren().addAll(left, new Region(), right);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setPadding(new Insets(4, 4, 10, 4));
        header.setAlignment(Pos.CENTER_LEFT);

        return header;
    }

    private Node buildCardWithTable() {
        var card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 22;
                -fx-border-radius: 22;
                -fx-border-color: rgba(0,0,0,0.08);
                -fx-border-width: 1;
                """);
        card.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, 0.10)));

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Ничего не найдено"));

        // ✅ увеличили шрифт таблицы
        table.setStyle("-fx-font-size: 16px;");

        // двойной клик = редактировать
        table.setRowFactory(tv -> {
            TableRow<IngredientDto> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) onEdit();
            });
            return row;
        });

        card.prefWidthProperty().bind(widthProperty().multiply(0.98));
        card.prefHeightProperty().bind(heightProperty().multiply(0.78));

        VBox.setVgrow(table, Priority.ALWAYS);
        card.getChildren().add(table);
        return card;
    }

    private Node buildFooter() {
        pageSizeBox.setItems(FXCollections.observableArrayList(10, 20, 50, 100));
        pageSizeBox.setValue(currentFilter.getSize());
        pageSizeBox.setPrefWidth(110);

        // ✅ увеличили комбобокс
        pageSizeBox.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: 800;
            """);
        pageSizeBox.setMinHeight(40);

        var sizeLabel = new Label("На странице:");
        sizeLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 900; -fx-font-size: 18px;");

        // ✅ увеличили “Всего” и “Страница”
        totalLabel.setStyle("-fx-text-fill: #111827; -fx-font-weight: 900; -fx-font-size: 18px;");
        statusLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 18px; -fx-font-weight: 700;");

        // ✅ увеличили Pagination (цифры/кнопки)
        pagination.setMaxPageIndicatorCount(7);
        pagination.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: 800;
            """);
        pagination.setMinHeight(56);

        var left = new HBox(12, totalLabel, new Separator(), statusLabel);
        left.setAlignment(Pos.CENTER_LEFT);

        var right = new HBox(12, sizeLabel, pageSizeBox);
        right.setAlignment(Pos.CENTER_RIGHT);

        var footerTop = new HBox();
        footerTop.getChildren().addAll(left, new Region(), right);
        HBox.setHgrow(footerTop.getChildren().get(1), Priority.ALWAYS);

        var footer = new VBox(12, footerTop, pagination);
        footer.setPadding(new Insets(14, 10, 0, 10));
        return footer;
    }

    private void configureTable() {
        TableColumn<IngredientDto, Number> idCol = new TableColumn<>("ID");
        idCol.setMinWidth(90);
        idCol.setCellValueFactory(cd -> new SimpleLongProperty(cd.getValue().id()));

        TableColumn<IngredientDto, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().name()));

        table.getColumns().addAll(idCol, nameCol);
    }

    private void configureFiltersAndPagination() {
        pageSizeBox.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            currentFilter = currentFilter.toBuilder()
                    .size(newV)
                    .page(0)
                    .build();
            reloadPage(0);
        });

        pagination.currentPageIndexProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            reloadPage(newV.intValue());
        });
    }

    // ---------------- Actions ----------------

    private void applyFilters() {
        currentFilter = currentFilter.toBuilder()
                .name(blankToNull(nameFilterField.getText()))
                .page(0)
                .build();
        reloadPage(0);
    }

    private void onAdd() {
        Optional<String> nameOpt = showNameDialog("Добавить ингредиент", null);
        if (nameOpt.isEmpty()) return;

        try {
            ingredientService.create(new IngredientDto(null, nameOpt.get()));
            reloadPage(pagination.getCurrentPageIndex());
        } catch (Exception ex) {
            showError("Не удалось добавить ингредиент", ex);
        }
    }

    private void onAddBulk() {
        var dialog = new BulkAddIngredientsDialog();
        Optional<List<String>> namesOpt = dialog.showAndWait();
        if (namesOpt.isEmpty() || namesOpt.get().isEmpty()) return;

        List<IngredientDto> dtos = namesOpt.get().stream()
                .map(name -> new IngredientDto(null, name))
                .toList();

        try {
            IngredientBulkSaveResult result = ingredientService.saveAllBulk(dtos);

            // обновим список 1 раз
            reloadPage(pagination.getCurrentPageIndex());

            showInfo("Готово.\n"
                    + "Добавлено новых: " + result.created() + "\n"
                    + "Уже было в базе: " + result.existed() + "\n"
                    + "Дубликатов в списке: " + result.duplicatesInRequest());
        } catch (ResponseStatusException rse) {
            String msg = (rse.getReason() != null && !rse.getReason().isBlank())
                    ? rse.getReason()
                    : "Ошибка сохранения списка ингредиентов";

            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Ошибка");
            a.setHeaderText("Не удалось сохранить список");
            a.setContentText(msg);
            a.showAndWait();
        } catch (Exception ex) {
            showError("Не удалось добавить список ингредиентов", ex);
        }
    }

    private void onEdit() {
        IngredientDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Выберите ингредиент в таблице.");
            return;
        }

        Optional<String> nameOpt = showNameDialog("Редактировать ингредиент", selected.name());
        if (nameOpt.isEmpty()) return;

        try {
            ingredientService.update(selected.id(), new IngredientDto(selected.id(), nameOpt.get()));
            reloadPage(pagination.getCurrentPageIndex());
        } catch (Exception ex) {
            showError("Не удалось обновить ингредиент", ex);
        }
    }

    private void onDelete() {
        IngredientDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Выберите ингредиент в таблице.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление");
        confirm.setHeaderText("Удалить ингредиент?");
        confirm.setContentText("Будут удалены связи с блюдами (если есть).");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        try {
            ingredientService.delete(selected.id());
            int page = pagination.getCurrentPageIndex();
            reloadPage(Math.max(0, page));
        } catch (Exception ex) {
            showError("Не удалось удалить ингредиент", ex);
        }
    }

    // ---------------- Data loading ----------------

    private void reloadPage(int pageIndex) {
        setLoading(true);

        new Thread(() -> {
            try {
                IngredientFilter filter = currentFilter.toBuilder()
                        .page(pageIndex)
                        .build();

                Page<IngredientDto> page = ingredientService.getAllByFilter(filter);

                Platform.runLater(() -> {
                    table.setItems(FXCollections.observableArrayList(page.getContent()));
                    totalLabel.setText("Всего: " + page.getTotalElements());

                    int totalPages = Math.max(page.getTotalPages(), 1);
                    pagination.setPageCount(totalPages);
                    pagination.setCurrentPageIndex(Math.min(pageIndex, totalPages - 1));

                    statusLabel.setText("Страница " + (pagination.getCurrentPageIndex() + 1) + " из " + totalPages);
                    setLoading(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Ошибка загрузки списка ингредиентов", ex);
                });
            }
        }, "ingredients-loader").start();
    }

    private void setLoading(boolean loading) {
        table.setDisable(loading);
        pagination.setDisable(loading);
        statusLabel.setText(loading ? "Загрузка..." : statusLabel.getText());
    }

    // ---------------- Dialogs & helpers ----------------

    private Optional<String> showNameDialog(String title, String initialValue) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType saveBtnType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        TextField tf = new TextField();
        tf.setPromptText("Название");
        tf.setStyle("-fx-font-size: 16px; -fx-padding: 10 12 10 12;");
        if (initialValue != null) tf.setText(initialValue);

        VBox box = new VBox(10, new Label("Название ингредиента:"), tf);
        box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);

        Node saveBtn = dialog.getDialogPane().lookupButton(saveBtnType);
        saveBtn.disableProperty().bind(tf.textProperty().isEmpty());

        dialog.setResultConverter(bt -> (bt == saveBtnType) ? tf.getText() : null);

        Platform.runLater(tf::requestFocus);
        return dialog.showAndWait().map(String::trim).filter(s -> !s.isBlank());
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
        // если прямо ResponseStatusException
        if (ex instanceof ResponseStatusException rse) {
            return rse.getReason() != null ? rse.getReason() : "Ошибка запроса";
        }

        // если причина (cause) = ResponseStatusException
        Throwable c = ex.getCause();
        if (c instanceof ResponseStatusException rse) {
            return rse.getReason() != null ? rse.getReason() : "Ошибка запроса";
        }

        return ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
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