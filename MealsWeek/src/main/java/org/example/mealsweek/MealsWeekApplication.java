package org.example.mealsweek;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(
        pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
)
public class MealsWeekApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApp.class, args);
    }

}
