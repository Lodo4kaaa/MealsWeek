package org.example.mealsweek.ui.config;

import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.mealsweek.service.IngredientService;
import org.example.mealsweek.ui.Ingredient.IngredientsView;
import org.example.mealsweek.ui.MainMenuView;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SceneManager {

    private final IngredientService ingredientService;

    private Stage stage;
    private Scene scene;

    public void init(Stage stage) {
        this.stage = stage;

        scene = new Scene(new MainMenuView(this), 1100, 650);

        stage.setTitle("Блюда на неделю");
        stage.setMinWidth(900);
        stage.setMinHeight(550);
        stage.setScene(scene);
        stage.show();
    }

    public void showMainMenu() {
        scene.setRoot(new MainMenuView(this));
    }

    public void showIngredients() {
        scene.setRoot(new IngredientsView(ingredientService, this::showMainMenu));
    }

    // Заглушки на будущее:
    public void showWeeks() { /* позже */ }
    public void showDishes() { /* позже */ }
    public void showShopping() { /* позже */ }
}