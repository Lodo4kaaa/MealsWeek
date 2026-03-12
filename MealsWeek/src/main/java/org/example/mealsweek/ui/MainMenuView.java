package org.example.mealsweek.ui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.example.mealsweek.ui.config.SceneManager;

public class MainMenuView extends StackPane {

    private final SceneManager sceneManager;

    private final VBox card = new VBox(28);
    private final Label title = new Label("Блюда на неделю");

    private final Button weeksBtn = new Button("Недели");
    private final Button dishesBtn = new Button("Блюда");
    private final Button ingredientsBtn = new Button("Ингредиенты");
    private final Button shoppingBtn = new Button("Закупка");

    public MainMenuView(SceneManager sceneManager) {
        this.sceneManager = sceneManager;

        ingredientsBtn.setOnAction(e -> sceneManager.showIngredients());
        weeksBtn.setOnAction(e -> sceneManager.showWeeks());
        dishesBtn.setOnAction(e -> sceneManager.showDishes());
        shoppingBtn.setOnAction(e -> sceneManager.showShopping());

        setStyle("-fx-background-color: linear-gradient(to bottom, #f7f8fb, #eef2f7);");

        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(40, 55, 40, 55));
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 26;
                -fx-border-radius: 26;
                -fx-border-color: rgba(0,0,0,0.08);
                -fx-border-width: 1;
                """);
        card.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, 0.10)));

        // Карточка адаптивная: занимает до 92% ширины и до 90% высоты окна, но не больше max
        card.maxWidthProperty().bind(widthProperty().multiply(0.92));
        card.maxHeightProperty().bind(heightProperty().multiply(0.90));

        // Заголовок: размер шрифта от ширины
        title.fontProperty().bind(Bindings.createObjectBinding(() -> {
            double w = getWidth();
            double size = clamp(w / 22.0, 26, 48);
            return Font.font("System", size);
        }, widthProperty()));
        title.setStyle("-fx-text-fill: #111827; -fx-font-weight: 800;");

        // Верхний ряд кнопок
        HBox topRow = new HBox(18);
        topRow.setAlignment(Pos.CENTER);
        topRow.setFillHeight(true);

        // Каждая кнопка в своём контейнере — чтобы красиво тянулась
        topRow.getChildren().addAll(wrapGrow(weeksBtn), wrapGrow(dishesBtn), wrapGrow(ingredientsBtn));

        // Нижняя кнопка на всю ширину
        shoppingBtn.setMaxWidth(Double.MAX_VALUE);

        // Общие стили + hover
        styleMenuButton(weeksBtn);
        styleMenuButton(dishesBtn);
        styleMenuButton(ingredientsBtn);
        styleBigButton(shoppingBtn);

        // Адаптивные размеры кнопок (через binding)
        bindButtonsSizes(topRow);

        card.getChildren().addAll(title, spacer(8), topRow, spacer(14), shoppingBtn);

        getChildren().add(card);
        StackPane.setMargin(card, new Insets(18));
    }

    private void bindButtonsSizes(HBox topRow) {
        // ширина карточки минус отступы, делим на 3
        topRow.prefWidthProperty().bind(card.widthProperty().subtract(20));

        // высота верхних кнопок от высоты окна
        weeksBtn.prefHeightProperty().bind(heightProperty().multiply(0.18).map(v -> clamp(v.doubleValue(), 90, 150)));
        dishesBtn.prefHeightProperty().bind(weeksBtn.prefHeightProperty());
        ingredientsBtn.prefHeightProperty().bind(weeksBtn.prefHeightProperty());

        // ширина верхних кнопок: 1/3 доступной ширины
        weeksBtn.prefWidthProperty().bind(topRow.widthProperty().subtract(36).divide(3));
        dishesBtn.prefWidthProperty().bind(weeksBtn.prefWidthProperty());
        ingredientsBtn.prefWidthProperty().bind(weeksBtn.prefWidthProperty());

        // нижняя кнопка: по ширине карточки
        shoppingBtn.prefWidthProperty().bind(card.widthProperty().subtract(0));

        // высота нижней кнопки
        shoppingBtn.prefHeightProperty().bind(heightProperty().multiply(0.22).map(v -> clamp(v.doubleValue(), 110, 190)));

        // шрифт кнопок — адаптивный
        var smallFontBinding = Bindings.createObjectBinding(() ->
                Font.font("System", clamp(getWidth() / 45.0, 16, 24)), widthProperty());

        weeksBtn.fontProperty().bind(smallFontBinding);
        dishesBtn.fontProperty().bind(smallFontBinding);
        ingredientsBtn.fontProperty().bind(smallFontBinding);

        shoppingBtn.fontProperty().bind(Bindings.createObjectBinding(() ->
                Font.font("System", clamp(getWidth() / 38.0, 18, 28)), widthProperty()));
    }

    private StackPane wrapGrow(Button b) {
        StackPane box = new StackPane(b);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private Region spacer(double h) {
        Region r = new Region();
        r.setMinHeight(h);
        return r;
    }

    private void styleMenuButton(Button b) {
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle(baseButtonStyle());
        installHover(b, baseButtonStyle(), hoverButtonStyle());
    }

    private void styleBigButton(Button b) {
        b.setStyle(baseButtonStyle() + "-fx-font-weight: 800;");
        installHover(b, baseButtonStyle() + "-fx-font-weight: 800;", hoverButtonStyle() + "-fx-font-weight: 800;");
    }

    private String baseButtonStyle() {
        return """
                -fx-background-color: #ffffff;
                -fx-background-radius: 22;
                -fx-border-radius: 22;
                -fx-border-color: rgba(17,24,39,0.18);
                -fx-border-width: 2;
                -fx-font-weight: 700;
                -fx-text-fill: #111827;
                -fx-cursor: hand;
                -fx-padding: 14 18 14 18;
                """;
    }

    private String hoverButtonStyle() {
        return """
                -fx-background-color: #f3f4f6;
                -fx-background-radius: 22;
                -fx-border-radius: 22;
                -fx-border-color: rgba(17,24,39,0.28);
                -fx-border-width: 2;
                -fx-font-weight: 700;
                -fx-text-fill: #111827;
                -fx-cursor: hand;
                -fx-padding: 14 18 14 18;
                """;
    }

    private void installHover(Button b, String normal, String hover) {
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(normal));
        b.setOnMousePressed(e -> b.setStyle(hover + "-fx-translate-y: 1;"));
        b.setOnMouseReleased(e -> b.setStyle(hover));
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}