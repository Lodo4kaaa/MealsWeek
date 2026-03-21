package org.example.mealsweek.ui.Week;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.example.mealsweek.dto.DishDto;
import org.example.mealsweek.dto.WeekDishDto;
import org.example.mealsweek.dto.WeekDto;
import org.example.mealsweek.service.DishService;
import org.example.mealsweek.service.WeekService;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WeeksView extends BorderPane {

    private static final List<DayInfo> DAYS = List.of(
            new DayInfo(1, "Понедельник"),
            new DayInfo(2, "Вторник"),
            new DayInfo(3, "Среда"),
            new DayInfo(4, "Четверг"),
            new DayInfo(5, "Пятница"),
            new DayInfo(6, "Суббота"),
            new DayInfo(7, "Воскресенье")
    );

    private final WeekService weekService;
    private final DishService dishService;
    private final Runnable onBack;

    private final ListView<WeekDto> weeksList = new ListView<>();
    private final Label selectedWeekLabel = new Label("Неделя не выбрана");
    private final Label statusLabel = new Label("");

    private final VBox daysContainer = new VBox(14);

    private WeekDto selectedWeek;
    private List<DishDto> allDishes = new ArrayList<>();

    public WeeksView(WeekService weekService, DishService dishService, Runnable onBack) {
        this.weekService = weekService;
        this.dishService = dishService;
        this.onBack = onBack;

        setStyle("-fx-background-color: linear-gradient(to bottom, #f8fafc, #eef2f7);");
        setPadding(new Insets(20));

        setTop(buildHeader());
        setLeft(buildWeeksPanel());
        setCenter(buildMainPanel());

        configureWeeksList();

        reloadDishes();
        reloadWeeks();
    }

    private Node buildHeader() {
        Label title = new Label("Недели");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

        Button backBtn = new Button("← Назад");
        styleSecondary(backBtn);
        backBtn.setOnAction(e -> {
            if (onBack != null) {
                onBack.run();
            }
        });

        Button addWeekBtn = new Button("+ Добавить неделю");
        stylePrimary(addWeekBtn);
        addWeekBtn.setOnAction(e -> createWeek());

        Button hideWeekBtn = new Button("Скрыть неделю");
        styleDanger(hideWeekBtn);
        hideWeekBtn.setOnAction(e -> hideWeek());

        HBox left = new HBox(12, backBtn, title);
        left.setAlignment(Pos.CENTER_LEFT);

        HBox right = new HBox(10, addWeekBtn, hideWeekBtn);
        right.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(left, spacer, right);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 0, 16, 0));
        return box;
    }

    private Node buildWeeksPanel() {
        VBox panel = new VBox(14);
        panel.setPrefWidth(280);
        panel.setMinWidth(260);
        panel.setPadding(new Insets(18));
        panel.setStyle(cardStyle());
        panel.setEffect(new DropShadow(18, Color.rgb(15, 23, 42, 0.10)));

        Label title = new Label("Активные недели");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        Label hint = new Label("Удаление работает как скрытие");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 700;");

        weeksList.setPlaceholder(new Label("Неделей пока нет"));
        weeksList.setPrefHeight(600);
        weeksList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(WeekDto item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label id = new Label("Неделя #" + item.id());
                id.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

                Label status = new Label(item.active() ? "Активна" : "Скрыта");
                status.setStyle("""
                        -fx-background-color: #dcfce7;
                        -fx-text-fill: #166534;
                        -fx-background-radius: 999;
                        -fx-padding: 4 10 4 10;
                        -fx-font-size: 12px;
                        -fx-font-weight: 800;
                        """);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox row = new HBox(8, id, spacer, status);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8, 10, 8, 10));

                setGraphic(row);
            }
        });

        VBox.setVgrow(weeksList, Priority.ALWAYS);
        panel.getChildren().addAll(title, hint, weeksList);
        return panel;
    }

    private Node buildMainPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(18));
        panel.setStyle(cardStyle());
        panel.setEffect(new DropShadow(18, Color.rgb(15, 23, 42, 0.10)));
        HBox.setHgrow(panel, Priority.ALWAYS);

        selectedWeekLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

        Label subtitle = new Label("Добавляй блюда прямо в карточке нужного дня.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: 700;");

        statusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #64748b;");

        ScrollPane scrollPane = new ScrollPane(daysContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        daysContainer.setPadding(new Insets(4, 2, 4, 2));
        buildEmptyDayCards();

        panel.getChildren().addAll(selectedWeekLabel, subtitle, statusLabel, scrollPane);
        return panel;
    }

    private void configureWeeksList() {
        weeksList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            selectedWeek = newV;
            if (newV == null) {
                selectedWeekLabel.setText("Неделя не выбрана");
                buildEmptyDayCards();
            } else {
                selectedWeekLabel.setText("Неделя #" + newV.id());
                reloadWeekPlan();
            }
        });
    }

    private void reloadWeeks() {
        setLoading(true);

        new Thread(() -> {
            try {
                List<WeekDto> weeks = weekService.getAllActiveWeeks();

                Platform.runLater(() -> {
                    weeksList.setItems(FXCollections.observableArrayList(weeks));
                    setLoading(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Не удалось загрузить недели", ex);
                });
            }
        }).start();
    }

    private void reloadDishes() {
        try {
            allDishes = dishService.getAll();
        } catch (Exception ex) {
            allDishes = new ArrayList<>();
            showError("Не удалось загрузить блюда", ex);
        }
    }

    private void reloadWeekPlan() {
        if (selectedWeek == null) {
            buildEmptyDayCards();
            return;
        }

        setLoading(true);

        new Thread(() -> {
            try {
                List<WeekDishDto> plan = weekService.getWeekPlan(selectedWeek.id());
                Map<Integer, List<WeekDishDto>> grouped = plan.stream()
                        .collect(Collectors.groupingBy(WeekDishDto::dayOfWeekId));

                Platform.runLater(() -> {
                    renderDayCards(grouped);
                    setLoading(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Не удалось загрузить план недели", ex);
                });
            }
        }).start();
    }

    private void createWeek() {
        try {
            WeekDto newWeek = weekService.create();
            reloadWeeks();
            showInfo("Неделя #" + newWeek.id() + " добавлена");
        } catch (Exception ex) {
            showError("Не удалось добавить неделю", ex);
        }
    }

    private void hideWeek() {
        WeekDto week = weeksList.getSelectionModel().getSelectedItem();
        if (week == null) {
            showInfo("Сначала выбери неделю.");
            return;
        }

        try {
            weekService.softDelete(week.id());
            selectedWeek = null;
            selectedWeekLabel.setText("Неделя не выбрана");
            buildEmptyDayCards();
            reloadWeeks();
            showInfo("Неделя скрыта");
        } catch (Exception ex) {
            showError("Не удалось скрыть неделю", ex);
        }
    }

    private void openAddDishDialog(DayInfo day) {
        if (selectedWeek == null) {
            showInfo("Сначала выбери неделю.");
            return;
        }

        if (allDishes.isEmpty()) {
            showInfo("Список блюд пуст.");
            return;
        }

        Map<String, DishDto> optionsMap = new LinkedHashMap<>();
        for (DishDto dish : allDishes) {
            String label = "#" + dish.id() + " — " + dish.name();
            optionsMap.put(label, dish);
        }

        List<String> options = new ArrayList<>(optionsMap.keySet());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
        dialog.setTitle("Добавить блюдо");
        dialog.setHeaderText("Выбери блюдо для дня: " + day.name());
        dialog.setContentText("Блюдо:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        DishDto selectedDish = optionsMap.get(result.get());
        if (selectedDish == null) {
            showInfo("Не удалось определить выбранное блюдо.");
            return;
        }

        try {
            weekService.assignDishToDay(selectedWeek.id(), day.id(), selectedDish.id());
            reloadWeekPlan();
            showInfo("Блюдо добавлено в " + day.name());
        } catch (Exception ex) {
            showError("Не удалось добавить блюдо", ex);
        }
    }

    private void buildEmptyDayCards() {
        renderDayCards(new HashMap<>());
    }

    private void renderDayCards(Map<Integer, List<WeekDishDto>> grouped) {
        daysContainer.getChildren().clear();

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);

        int col = 0;
        int row = 0;

        for (DayInfo day : DAYS) {
            List<WeekDishDto> dishes = grouped.getOrDefault(day.id(), List.of());
            VBox card = createDayCard(day, dishes);

            grid.add(card, col, row);
            GridPane.setHgrow(card, Priority.ALWAYS);

            col++;
            if (col == 2) {
                col = 0;
                row++;
            }
        }

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        c2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().setAll(c1, c2);

        daysContainer.getChildren().add(grid);
    }

    private VBox createDayCard(DayInfo day, List<WeekDishDto> dishes) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setMinHeight(170);
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 20;
                -fx-border-color: #e2e8f0;
                -fx-border-radius: 20;
                -fx-border-width: 1;
                """);

        Label dayName = new Label(day.name());
        dayName.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

        Label counter = new Label("Блюд: " + dishes.size());
        counter.setStyle("""
                -fx-background-color: #eff6ff;
                -fx-text-fill: #1d4ed8;
                -fx-background-radius: 999;
                -fx-padding: 4 10 4 10;
                -fx-font-size: 12px;
                -fx-font-weight: 800;
                """);

        Button addBtn = new Button("+ Добавить");
        styleMiniPrimary(addBtn);
        addBtn.setOnAction(e -> openAddDishDialog(day));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(8, dayName, counter, spacer, addBtn);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox dishesBox = new VBox(8);

        if (dishes.isEmpty()) {
            Label empty = new Label("Пока ничего не добавлено");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-weight: 700;");
            dishesBox.getChildren().add(empty);
        } else {
            for (WeekDishDto item : dishes) {
                dishesBox.getChildren().add(createDishChip(day, item));
            }
        }

        VBox.setVgrow(dishesBox, Priority.ALWAYS);
        card.getChildren().addAll(top, dishesBox);
        return card;
    }

    private Node createDishChip(DayInfo day, WeekDishDto item) {
        Label name = new Label(item.dishName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        Label id = new Label("#" + item.dishId());
        id.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 800;");

        VBox textBox = new VBox(2, name, id);

        Button removeBtn = new Button("✕");
        removeBtn.setStyle("""
                -fx-background-color: #fee2e2;
                -fx-text-fill: #b91c1c;
                -fx-background-radius: 999;
                -fx-font-size: 12px;
                -fx-font-weight: 900;
                -fx-cursor: hand;
                -fx-padding: 4 8 4 8;
                """);
        removeBtn.setOnAction(e -> {
            if (selectedWeek == null) {
                return;
            }
            try {
                weekService.removeDishFromDay(selectedWeek.id(), day.id(), item.dishId());
                reloadWeekPlan();
            } catch (Exception ex) {
                showError("Не удалось удалить блюдо", ex);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox chip = new HBox(10, textBox, spacer, removeBtn);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(10, 12, 10, 12));
        chip.setStyle("""
                -fx-background-color: #f8fafc;
                -fx-background-radius: 14;
                -fx-border-color: #e2e8f0;
                -fx-border-radius: 14;
                """);

        return chip;
    }

    private void setLoading(boolean loading) {
        weeksList.setDisable(loading);
        statusLabel.setText(loading ? "Загрузка..." : "");
    }

    private void showInfo(String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Информация");
        a.setHeaderText(null);
        a.setContentText(text);
        a.showAndWait();
    }

    private void showError(String title, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Ошибка");
        a.setHeaderText(title);
        a.setContentText(extractMessage(ex));
        a.showAndWait();
    }

    private String extractMessage(Exception ex) {
        if (ex instanceof ResponseStatusException rse) {
            return rse.getReason() != null ? rse.getReason() : "Ошибка запроса";
        }

        Throwable cause = ex.getCause();
        if (cause instanceof ResponseStatusException rse) {
            return rse.getReason() != null ? rse.getReason() : "Ошибка запроса";
        }

        return ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
    }

    private String cardStyle() {
        return """
                -fx-background-color: white;
                -fx-background-radius: 24;
                -fx-border-color: rgba(15,23,42,0.08);
                -fx-border-radius: 24;
                -fx-border-width: 1;
                """;
    }

    private void stylePrimary(Button b) {
        b.setStyle("""
                -fx-background-color: #0f172a;
                -fx-text-fill: white;
                -fx-background-radius: 14;
                -fx-font-size: 15px;
                -fx-font-weight: 900;
                -fx-cursor: hand;
                -fx-padding: 10 16 10 16;
                """);
    }

    private void styleDanger(Button b) {
        b.setStyle("""
                -fx-background-color: #ef4444;
                -fx-text-fill: white;
                -fx-background-radius: 14;
                -fx-font-size: 15px;
                -fx-font-weight: 900;
                -fx-cursor: hand;
                -fx-padding: 10 16 10 16;
                """);
    }

    private void styleSecondary(Button b) {
        b.setStyle("""
                -fx-background-color: white;
                -fx-text-fill: #0f172a;
                -fx-background-radius: 14;
                -fx-border-color: #cbd5e1;
                -fx-border-radius: 14;
                -fx-font-size: 15px;
                -fx-font-weight: 900;
                -fx-cursor: hand;
                -fx-padding: 10 16 10 16;
                """);
    }

    private void styleMiniPrimary(Button b) {
        b.setStyle("""
                -fx-background-color: #eff6ff;
                -fx-text-fill: #1d4ed8;
                -fx-background-radius: 999;
                -fx-font-size: 12px;
                -fx-font-weight: 900;
                -fx-cursor: hand;
                -fx-padding: 5 12 5 12;
                """);
    }

    private record DayInfo(Integer id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }
}