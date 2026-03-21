package org.example.mealsweek.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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

            @NotNull(message = "Количество обязательно")
            @Positive(message = "Положительное число")
            BigDecimal amount,

            @NotBlank(message = "Тип должен быть заполнен")
            Long measurementUnitId,
            String measurementUnitName,

            String note
    ) implements Serializable {}
}
