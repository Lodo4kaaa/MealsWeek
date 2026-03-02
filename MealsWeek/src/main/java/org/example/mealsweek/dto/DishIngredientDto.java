package org.example.mealsweek.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;

public record DishIngredientDto(
        @NotNull
        Long dishId,
        String dishName,

        @NotNull
        Long ingredientId,
        String ingredientName,

        @NotNull(message = "Количество обязательно")
        @Positive(message = "Положительное число")
        BigDecimal amount,

        @NotBlank(message = "Тип должен быть заполнен")
        String unit,

        String note
) implements Serializable { }
