package org.example.mealsweek.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record DishDto(
        Long id,

        @NotBlank(message = "Название блюда обязательно")
        String name,

        String description,

        List<IngredientLineDto> ingredients
) implements Serializable {

    public record IngredientLineDto(
            Long ingredientId,

            String ingredientName,

            @NotBlank(message = "Количество обязательно")
            BigDecimal amount,

            @NotBlank(message = "Тип должен быть заполнен")
            String unit,

            String note
    ) implements Serializable {}
}
