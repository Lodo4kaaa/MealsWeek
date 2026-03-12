package org.example.mealsweek;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.example.mealsweek.ui.config.SceneManager;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApp extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(MealsWeekApplication.class).run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) {
        var sceneManager = context.getBean(SceneManager.class);
        sceneManager.init(stage);
    }

    @Override
    public void stop() {
        if (context != null) context.close();
        Platform.exit();
    }
}